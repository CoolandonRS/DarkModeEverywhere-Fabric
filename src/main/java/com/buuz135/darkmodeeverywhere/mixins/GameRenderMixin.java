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
import net.minecraft.server.packs.resources.ResourceProvider;
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
    @Inject(method = "getPositionTexShader", at = @At("HEAD"), cancellable = true)
    private static void getPositionTexShader(CallbackInfoReturnable<ShaderInstance> cir) {
        if (ClientProxy.SELECTED_SHADER != null){
            cir.setReturnValue(ClientProxy.REGISTERED_SHADERS.get(ClientProxy.SELECTED_SHADER));
        }
    }
}
