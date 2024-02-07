package com.buuz135.darkmodeeverywhere.mixins;

import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Exceptionally cursed
@Mixin(value = FabricShaderProgram.class, remap = false)
public class FabricShaderProgramMixin {
    @Inject(method = "rewriteAsId", at = @At("HEAD"), cancellable = true, remap = false)
    private static void something(String input, String containedId, CallbackInfoReturnable<String> cir) {
        if (input.matches("^[^:/]+:.*")) cir.setReturnValue(input);
    }
}
