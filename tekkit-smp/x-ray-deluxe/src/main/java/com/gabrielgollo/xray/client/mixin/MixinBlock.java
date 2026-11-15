package com.gabrielgollo.xray.client.mixin;

import com.gabrielgollo.xray.XRayDeluxeMod;
import net.minecraft.block.Block;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class MixinBlock {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private void gabrielgollo$xrayLayer(CallbackInfoReturnable<BlockRenderLayer> callback) {
        if (XRayDeluxeMod.isEnabled()) callback.setReturnValue(BlockRenderLayer.TRANSLUCENT);
    }
}
