package com.gabrielgollo.xray.client.mixin;

import com.gabrielgollo.xray.client.XRayRenderHooks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher {
    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void gabrielgollo$filterBlock(IBlockState state, BlockPos pos, IBlockAccess access, BufferBuilder buffer, CallbackInfoReturnable<Boolean> callback) {
        if (XRayRenderHooks.shouldHide(state, pos, access)) {
            callback.setReturnValue(Boolean.FALSE);
            return;
        }
        XRayRenderHooks.begin(state);
    }

    @Inject(method = "renderBlock", at = @At("RETURN"))
    private void gabrielgollo$finishBlock(IBlockState state, BlockPos pos, IBlockAccess access, BufferBuilder buffer, CallbackInfoReturnable<Boolean> callback) {
        XRayRenderHooks.end();
    }
}
