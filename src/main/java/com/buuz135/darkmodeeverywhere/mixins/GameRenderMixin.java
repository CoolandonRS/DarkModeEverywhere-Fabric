package com.buuz135.darkmodeeverywhere.mixins;

import com.buuz135.darkmodeeverywhere.ClientProxy;
import com.buuz135.darkmodeeverywhere.DarkModeEverywhere;
import com.buuz135.darkmodeeverywhere.ShaderConfig;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public class GameRenderMixin {

    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;shutdownShaders()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void darkmode$registerShaders(ResourceManager manager, CallbackInfo ci, List<Program> list, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderRegistry) {
        ClientProxy.REGISTERED_SHADERS = new HashMap<>();
        ClientProxy.SHADER_VALUES = new HashMap<>();
        List<String> loaderShaders = new ArrayList<>();
        for (ShaderConfig.Value shader : ClientProxy.CONFIG.getShaders()) {
            ClientProxy.SHADER_VALUES.put(new ResourceLocation(shader.resourceLocation), shader);
            if (loaderShaders.contains(shader.resourceLocation)) continue;
            try {
                shaderRegistry.add(Pair.of(new ShaderInstance(manager, new ResourceLocation(shader.resourceLocation).toString(), DefaultVertexFormat.POSITION_TEX), shaderInstance -> {
                    ClientProxy.REGISTERED_SHADERS.put(new ResourceLocation(shader.resourceLocation), shaderInstance);
                }));
                DarkModeEverywhere.LOGGER.info("Registered shader " + shader.resourceLocation);
                loaderShaders.add(shader.resourceLocation);
            } catch (IOException e) {
                DarkModeEverywhere.LOGGER.trace(e);
            }
        }
        if (ClientProxy.CONFIG.getSelectedShader() != null){
            ClientProxy.SELECTED_SHADER = new ResourceLocation(ClientProxy.CONFIG.getSelectedShader());
        }
    }
    @Inject(method = "getPositionTexShader", at = @At("HEAD"), cancellable = true)
    private static void getPositionTexShader(CallbackInfoReturnable<ShaderInstance> cir) {
        if (ClientProxy.SELECTED_SHADER != null){
            cir.setReturnValue(ClientProxy.REGISTERED_SHADERS.get(ClientProxy.SELECTED_SHADER));
        }
    }
}
