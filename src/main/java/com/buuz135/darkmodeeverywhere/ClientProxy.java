package com.buuz135.darkmodeeverywhere;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientProxy {
    public static HashMap<String, Boolean> BLACKLISTED_ELEMENTS = new HashMap<>();
    public static List<String> MODDED_BLACKLIST = new ArrayList<>();

    public static ShaderConfig CONFIG = new ShaderConfig();
    public static HashMap<ResourceLocation, ShaderInstance> REGISTERED_SHADERS = new HashMap<>();
    public static ArrayList<ResourceLocation> REGISTERED_SHADER_LOCATIONS = new ArrayList<>();
    public static HashMap<ResourceLocation, ShaderConfig.ShaderValue> SHADER_VALUES = new HashMap<>();
    public static ResourceLocation SELECTED_SHADER = null;

    public ClientProxy() {
        ShaderConfig.load();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::shaderRegister);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imcCallback);
        MinecraftForge.EVENT_BUS.addListener(this::openGui);
    }

    @SubscribeEvent
    public void shaderRegister(RegisterShadersEvent event){
        REGISTERED_SHADERS = new HashMap<>();
        REGISTERED_SHADER_LOCATIONS = new ArrayList<>();
        SHADER_VALUES = new HashMap<>();
        List<ResourceLocation> alreadyPendingShaders = new ArrayList<>();
        for (ShaderConfig.ShaderValue shaderValue : CONFIG.getShaders()) {
            SHADER_VALUES.put(shaderValue.resourceLocation, shaderValue);
            if (alreadyPendingShaders.contains(shaderValue.resourceLocation)) continue;
            try {
                event.registerShader(new ShaderInstance(event.getResourceProvider(), shaderValue.resourceLocation, DefaultVertexFormat.POSITION_TEX), shaderInstance -> {
                    DarkModeEverywhere.LOGGER.debug("Registered shader " + shaderValue.resourceLocation);
                    REGISTERED_SHADERS.put(shaderValue.resourceLocation, shaderInstance);
                    REGISTERED_SHADER_LOCATIONS.add(shaderValue.resourceLocation);
                });
                alreadyPendingShaders.add(shaderValue.resourceLocation);
            } catch (IOException e) {
                DarkModeEverywhere.LOGGER.trace(e);
            }
        }
        if (CONFIG.getSelectedShader() != null){
            SELECTED_SHADER = new ResourceLocation(CONFIG.getSelectedShader());
        }
        RenderedClassesTracker.start();
    }

    @SubscribeEvent
    public void onConfigReload(ModConfigEvent.Reloading reloading){ BLACKLISTED_ELEMENTS.clear(); }

    private static boolean considerElementNameForBlacklist(String elementName) {
        DarkModeEverywhere.LOGGER.debug("Considering " + elementName + " for element blacklist");
        boolean result = DarkConfig.CLIENT.METHOD_SHADER_BLACKLIST.get().stream().anyMatch(elementName::contains);
        BLACKLISTED_ELEMENTS.put(elementName, result);
        return result;
    }

    public static boolean isElementNameBlacklisted(String elementName) {
        try {
            return BLACKLISTED_ELEMENTS.get(elementName);
        } catch (NullPointerException error) {
            return considerElementNameForBlacklist(elementName);
        }
    }

    @SubscribeEvent
    public void imcCallback(InterModProcessEvent event) {
        event.getIMCStream(string -> string.equals("dme-shaderblacklist")).forEach(imcMessage -> {
            String classMethodBlacklist = (String) imcMessage.messageSupplier().get();
            MODDED_BLACKLIST.add(classMethodBlacklist);
        });
    }

    private ResourceLocation getNextShaderResourceLocation() {
        if (Screen.hasShiftDown()) {
            return null;
        }

        if (SELECTED_SHADER == null){
            return REGISTERED_SHADER_LOCATIONS.get(0);
        }

        int nextShaderIndex = REGISTERED_SHADER_LOCATIONS.indexOf(SELECTED_SHADER) + 1;
        if (nextShaderIndex >= REGISTERED_SHADERS.size()){
            return null;
        }

        return REGISTERED_SHADER_LOCATIONS.get(nextShaderIndex);
    }

    private Tooltip getShaderSwitchButtonTooltip() {
        MutableComponent tooltipComponent = SELECTED_SHADER == null ? Component.translatable("gui." + DarkModeEverywhere.MODID + ".light_mode") : SHADER_VALUES.get(SELECTED_SHADER).displayName.copy();
        tooltipComponent.append(Component.literal("\n"));
        tooltipComponent.append(Component.translatable("gui.tooltip." + DarkModeEverywhere.MODID + ".shader_switch_tooltip").withStyle(ChatFormatting.GRAY));

        return Tooltip.create(tooltipComponent);
    }

    @SubscribeEvent
    public void openGui(ScreenEvent.Init event){
       if (event.getScreen() instanceof AbstractContainerScreen || (DarkConfig.CLIENT.SHOW_BUTTON_IN_TITLE_SCREEN.get() && event.getScreen() instanceof TitleScreen)){
           int x = DarkConfig.CLIENT.GUI_BUTTON_X_OFFSET.get();
           int y = DarkConfig.CLIENT.GUI_BUTTON_Y_OFFSET.get();
           if (event.getScreen() instanceof TitleScreen){
               x = DarkConfig.CLIENT.TITLE_SCREEN_BUTTON_X_OFFSET.get();
               y = DarkConfig.CLIENT.TITLE_SCREEN_BUTTON_Y_OFFSET.get();
           }

           Button.Builder buttonBuilder = Button.builder(
               Component.translatable("gui." + DarkModeEverywhere.MODID + ".dark_mode"),
               button -> {
                   SELECTED_SHADER = getNextShaderResourceLocation();
                   CONFIG.setSelectedShader(SELECTED_SHADER);
                   button.setTooltip(getShaderSwitchButtonTooltip());
               });

           buttonBuilder.pos(x, event.getScreen().height - 24 - y);
           buttonBuilder.size(60, 20);

           buttonBuilder.tooltip(getShaderSwitchButtonTooltip());
           Button button = buttonBuilder.build();
           event.addListener(button);
       }
    }
}
