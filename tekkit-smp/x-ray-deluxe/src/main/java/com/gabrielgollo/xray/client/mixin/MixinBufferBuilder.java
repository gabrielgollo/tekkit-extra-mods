package com.gabrielgollo.xray.client.mixin;

import com.gabrielgollo.xray.client.XRayRenderHooks;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder {
    @ModifyVariable(method = "color(IIII)Lnet/minecraft/client/renderer/BufferBuilder;", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int gabrielgollo$hiddenOpacity(int alpha) {
        return XRayRenderHooks.alpha(alpha);
    }
}
