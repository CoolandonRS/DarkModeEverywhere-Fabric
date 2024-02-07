package com.buuz135.darkmodeeverywhere;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientProxy implements ClientModInitializer {
    public static Object2BooleanMap<String> BLACKLISTED_ELEMENTS = new Object2BooleanOpenHashMap<>();
    public static List<String> MODDED_BLACKLIST = new ArrayList<>();
    public static ShaderConfig CONFIG = new ShaderConfig();
    public static Map<ShaderConfig.ShaderValue, ShaderInstance> TEX_SHADERS = new HashMap<>();
    public static Map<ShaderConfig.ShaderValue, ShaderInstance> TEX_COLOR_SHADERS = new HashMap<>();
    public static List<ShaderConfig.ShaderValue> SHADER_VALUES = new ArrayList<>();
    public static ShaderConfig.ShaderValue SELECTED_SHADER_VALUE = null;

    public void onInitializeClient() {
        ShaderConfig.load();
        ScreenEvents.AFTER_INIT.register(this::openGui);
    }

    public static ShaderInstance getSelectedTexShader() {
        return TEX_SHADERS.get(SELECTED_SHADER_VALUE);
    }

    public static ShaderInstance getSelectedTexColorShader() {
        return TEX_COLOR_SHADERS.get(SELECTED_SHADER_VALUE);
    }

    public static ShaderConfig.ShaderValue getSelectedShaderValue() {
        return SELECTED_SHADER_VALUE;
    }

    private static boolean blacklistContains(List<String> blacklist, String elementName) {
        return blacklist.stream().anyMatch(elementName::contains);
    }

    public static boolean isElementNameBlacklisted(String elementName) {
        return BLACKLISTED_ELEMENTS.computeIfAbsent(elementName, (String name) -> {
            DarkModeEverywhere.LOGGER.debug("Considering {} for element blacklist", name);
            RenderedClassesTracker.add(name);
            return blacklistContains(MODDED_BLACKLIST, name) || blacklistContains(DarkConfig.CLIENT.METHOD_SHADER_BLACKLIST.get(), name);
        });
    }

    private int getNextShaderValueIndex() {
        if (Screen.hasShiftDown()) {
            return 0;
        }

        int nextShaderIndex = SHADER_VALUES.indexOf(SELECTED_SHADER_VALUE) + 1;
        if (nextShaderIndex >= SHADER_VALUES.size()){
            return 0;
        }

        return nextShaderIndex;
    }

    private Tooltip getShaderSwitchButtonTooltip() {
        MutableComponent tooltipComponent = (SELECTED_SHADER_VALUE == null ? Component.translatable("gui." + DarkModeEverywhere.MODID + ".light_mode") : SELECTED_SHADER_VALUE.displayName).plainCopy();
        tooltipComponent.append(Component.literal("\n"));
        tooltipComponent.append(Component.translatable("gui.tooltip." + DarkModeEverywhere.MODID + ".shader_switch_tooltip").withStyle(ChatFormatting.GRAY));

        return Tooltip.create(tooltipComponent);
    }

    public void openGui(Minecraft client, Screen screen, int scaledWidth, int scaledHeight){
       if (screen instanceof AbstractContainerScreen || (DarkConfig.CLIENT.SHOW_BUTTON_IN_TITLE_SCREEN.get() && screen instanceof TitleScreen)){
           int x = DarkConfig.CLIENT.GUI_BUTTON_X_OFFSET.get();
           int y = DarkConfig.CLIENT.GUI_BUTTON_Y_OFFSET.get();
           if (screen instanceof TitleScreen){
               x = DarkConfig.CLIENT.TITLE_SCREEN_BUTTON_X_OFFSET.get();
               y = DarkConfig.CLIENT.TITLE_SCREEN_BUTTON_Y_OFFSET.get();
       }
           var builder = new Button.Builder(Component.translatable("gui." + DarkModeEverywhere.MODID + ".dark_mode"), (btn) -> {
               var idx = getNextShaderValueIndex();
               CONFIG.setSelectedShaderIndex(idx);
               SELECTED_SHADER_VALUE = SHADER_VALUES.get(idx);
               btn.setTooltip(getShaderSwitchButtonTooltip());
           });

           builder.pos(x, screen.height - 24 - y);
           builder.size(60, 20);
           builder.tooltip(getShaderSwitchButtonTooltip());
           Screens.getButtons(screen).add(builder.build());
       }
    }
}
