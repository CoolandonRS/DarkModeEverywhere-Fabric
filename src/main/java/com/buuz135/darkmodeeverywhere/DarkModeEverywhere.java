package com.buuz135.darkmodeeverywhere;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

public class DarkModeEverywhere implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "darkmodeeverywhere";

    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register("darkmodeeverywhere", ModConfig.Type.CLIENT, DarkConfig.CLIENT.SPEC);
        ForgeModConfigEvents.reloading("darkmodeeverywhere").register(DarkConfig.CLIENT::onConfigReload);

        HashSet<ShaderConfig.ShaderValue> registered = new HashSet<>();
        CoreShaderRegistrationCallback.EVENT.register((context) -> {
            for (var shader : ClientProxy.CONFIG.getShaders()) {
                if (ClientProxy.SHADER_VALUES.contains(shader)) continue;
                ClientProxy.SHADER_VALUES.add(shader);
                context.register(shader.texShaderLocation, DefaultVertexFormat.POSITION_TEX, (instance) -> {
                    ClientProxy.TEX_SHADERS.put(shader, instance);
                });
                context.register(shader.texColorShaderLocation, DefaultVertexFormat.POSITION_TEX_COLOR, (instance) -> {
                    ClientProxy.TEX_COLOR_SHADERS.put(shader, instance);
                });
            }

            // TODO SET SELECTED SHADER
        });
    }
}
