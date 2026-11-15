package com.gabrielgollo.xray.client;

import com.gabrielgollo.xray.XRayDeluxeMod;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class XRayRenderHooks {
    private static final ThreadLocal<Deque<Integer>> ALPHAS = new ThreadLocal<Deque<Integer>>() {
        @Override protected Deque<Integer> initialValue() { return new ArrayDeque<Integer>(); }
    };

    private XRayRenderHooks() {
    }

    public static boolean shouldHide(IBlockState state, BlockPos pos, IBlockAccess access) {
        return XRayDeluxeMod.shouldHide(state, pos, access);
    }

    public static void begin(IBlockState state) {
        if (!XRayDeluxeMod.isEnabled()) return;
        int alpha = XRayDeluxeMod.isLava(state) || XRayDeluxeMod.isSelectedOre(state) ? 255 : XRayDeluxeMod.getHiddenOpacity() * 255 / 100;
        ALPHAS.get().push(Integer.valueOf(alpha));
    }

    public static void end() {
        Deque<Integer> values = ALPHAS.get();
        if (!values.isEmpty()) values.pop();
        if (values.isEmpty()) ALPHAS.remove();
    }

    public static int alpha(int original) {
        Deque<Integer> values = ALPHAS.get();
        if (values.isEmpty()) return original;
        return original * values.peek().intValue() / 255;
    }
}
