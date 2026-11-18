final class FrameMotion {
    private long previous = Long.MIN_VALUE;
    private float residualX;
    private float residualY;

    float step(long nowNanos) {
        if (previous == Long.MIN_VALUE) { previous = nowNanos; return 0f; }
        long elapsed = nowNanos - previous;
        previous = nowNanos;
        return Math.max(0f, Math.min(.05f, elapsed / 1000000000f)) * 20f;
    }

    int pixelsX(float movement) {
        if (movement == 0f) { residualX = 0f; return 0; }
        residualX += movement;
        int pixels = (int)residualX;
        residualX -= pixels;
        return pixels;
    }

    int pixelsY(float movement) {
        if (movement == 0f) { residualY = 0f; return 0; }
        residualY += movement;
        int pixels = (int)residualY;
        residualY -= pixels;
        return pixels;
    }

    void reset() {
        previous = Long.MIN_VALUE;
        residualX = residualY = 0f;
    }
}
