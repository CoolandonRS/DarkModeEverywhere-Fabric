package com.buuz135.darkmodeeverywhere;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.ModInitializer;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeModConfigEvents;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DarkModeEverywhere implements ModInitializer {

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register("darkmodeeverywhere", ModConfig.Type.CLIENT, DarkConfig.CLIENT.SPEC);
        ForgeModConfigEvents.reloading("darkmodeeverywhere").register(DarkConfig.CLIENT::onConfigReload);

        HashSet<ResourceLocation> registered = new HashSet<>();
        CoreShaderRegistrationCallback.EVENT.register((context) -> {
            for (var shader : ClientProxy.CONFIG.getShaders()) {
                var resource = shader.resourceLocation;

                if (!registered.add(resource)) continue;
                ClientProxy.SHADER_VALUES.put(resource, shader);

                context.register(resource, DefaultVertexFormat.POSITION_TEX, (instance) -> {
                    ClientProxy.REGISTERED_SHADERS.put(resource, instance);
                });
            }

            if (ClientProxy.CONFIG.getSelectedShader() != null) {
                ClientProxy.SELECTED_SHADER = new ResourceLocation(ClientProxy.CONFIG.getSelectedShader());
            }
        });
    }

}