final class MenuInput {
    private vp screen;
    private boolean armed;
    private boolean leftHeld, rightHeld, backHeld;

    boolean armed() { return armed; }

    void reset() {
        if (screen != null) {
            if (leftHeld) screen.b(0, 0, 0);
            if (rightHeld) screen.b(0, 0, 1);
        }
        screen = null;
        armed = leftHeld = rightHeld = backHeld = false;
    }

    void update(net.minecraft.client.Minecraft mc, vp current, int x, int y,
            boolean left, boolean right, boolean back) {
        if (current != screen) {
            reset();
            screen = current;
        }
        if (screen == null) return;
        if (!armed) {
            if (!left && !right && !back) armed = true;
            return;
        }
        if (left != leftHeld) {
            leftHeld = left;
            if (left) current.a(x, y, 0); else current.b(x, y, 0);
            if (mc.s != current) return;
        }
        if (right != rightHeld) {
            rightHeld = right;
            if (right) current.a(x, y, 1); else current.b(x, y, 1);
            if (mc.s != current) return;
        }
        boolean close = back && !backHeld;
        backHeld = back;
        if (close) current.a((char)0, 1);
    }
}
