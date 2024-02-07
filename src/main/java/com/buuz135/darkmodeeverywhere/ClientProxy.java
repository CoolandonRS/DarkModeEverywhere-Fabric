package com.buuz135.darkmodeeverywhere;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientProxy implements ClientModInitializer {

    public static ShaderConfig CONFIG = new ShaderConfig();
    public static HashMap<ResourceLocation, ShaderInstance> REGISTERED_SHADERS = new HashMap<>();
    public static HashMap<ResourceLocation, ShaderConfig.Value> SHADER_VALUES = new HashMap<>();
    public static ResourceLocation SELECTED_SHADER = null;

    public void onInitializeClient() {
        ShaderConfig.load();
        ScreenEvents.AFTER_INIT.register(this::openGui);
    }

    public void openGui(Minecraft client, Screen screen, int scaledWidth, int scaledHeight){
       if (screen instanceof AbstractContainerScreen || (DarkConfig.CLIENT.SHOW_IN_MAIN.get() && screen instanceof TitleScreen)){
           int x = DarkConfig.CLIENT.X.get();
           int y = DarkConfig.CLIENT.Y.get();
           if (screen instanceof TitleScreen){
               x = DarkConfig.CLIENT.MAIN_X.get();
               y = DarkConfig.CLIENT.MAIN_Y.get();
           }
           var builder = new Button.Builder(Component.literal(screen instanceof TitleScreen ? DarkConfig.CLIENT.MAIN_NAME.get() : DarkConfig.CLIENT.NAME.get()), (btn) -> {
               if (Screen.hasShiftDown()) {
                   SELECTED_SHADER = null;
               } else if (SELECTED_SHADER == null) {
                   SELECTED_SHADER = (ResourceLocation) REGISTERED_SHADERS.keySet().toArray()[0];
               } else {
                   int nextShader = new ArrayList<>(REGISTERED_SHADERS.keySet()).indexOf(SELECTED_SHADER) + 1;
                   if (nextShader > REGISTERED_SHADERS.size() - 1) {
                       SELECTED_SHADER = null;
                   } else {
                       SELECTED_SHADER = new ArrayList<>(REGISTERED_SHADERS.keySet()).get(nextShader);
                   }
               }
               CONFIG.setSelectedShader(SELECTED_SHADER);
           });

           builder.pos(x, screen.height - 24 - y);
           builder.size(60, 20);
           // FIXME second line doesn't appear and doesn't update
           builder.tooltip(Tooltip.create(SELECTED_SHADER == null ? Component.literal("Light Mode") : Component.literal(SHADER_VALUES.get(SELECTED_SHADER).displayName), Component.literal(" * Use shift to change it to Light Mode").withStyle(ChatFormatting.GRAY)));
           Screens.getButtons(screen).add(builder.build());
       }
    }
}
