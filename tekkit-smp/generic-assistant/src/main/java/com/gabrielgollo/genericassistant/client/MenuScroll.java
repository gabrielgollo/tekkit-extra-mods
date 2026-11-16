package com.gabrielgollo.genericassistant.client;

final class MenuScroll {
    private int direction;
    private long nextStep;

    int poll(float vertical, long nowMillis) {
        if (!Float.isFinite(vertical) || Math.abs(vertical) <= .25f) {
            reset();
            return 0;
        }
        int requested = vertical > 0f ? 1 : -1;
        if (requested == direction && nowMillis < nextStep) return 0;
        direction = requested;
        long interval = Math.round(300f - Math.min(1f, Math.abs(vertical)) * 220f);
        nextStep = nowMillis + interval;
        return direction;
    }

    void reset() {
        direction = 0;
        nextStep = 0L;
    }
}
