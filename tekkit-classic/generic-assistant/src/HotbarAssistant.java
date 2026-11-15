import net.minecraft.client.Minecraft;

final class HotbarAssistant {
    private Object player;
    private Object world;
    private int selected = -1;
    private aan previous;
    private boolean action;

    void reset() {
        player = null;
        world = null;
        previous = null;
        selected = -1;
        action = false;
    }

    void update(Minecraft mc, boolean interacting, boolean dropping) {
        if (mc == null || mc.h == null || mc.f == null || mc.s != null
                || mc.h.ar != mc.h.aq || mc.h.ap.k() != null) { reset(); return; }
        int slot = mc.h.ap.c;
        aan current = mc.h.ap.a[slot];
        if (player == mc.h && world == mc.f && selected == slot && previous != null
                && action && !dropping && (current == null || current.a <= 0)) {
            boolean tool = previous.e();
            if ((tool && mod_GenericAssistant.replaceBrokenTool) || (!tool && mod_GenericAssistant.refillHotbar)) {
                int source = replacement(mc.h.ap.a, previous);
                if (source >= 0) {
                    mc.c.a(mc.h.aq.f, source, 0, false, mc.h);
                    mc.c.a(mc.h.aq.f, 36 + slot, 0, false, mc.h);
                    if (mc.h.ap.k() != null) mc.c.a(mc.h.aq.f, source, 0, false, mc.h);
                }
            }
        }
        player = mc.h;
        world = mc.f;
        selected = slot;
        current = mc.h.ap.a[slot];
        previous = current == null || current.a <= 0 ? null : current.k();
        action = interacting && !dropping;
    }

    static int replacement(aan[] inventory, aan wanted) {
        for (int slot = 9; slot < Math.min(36, inventory.length); slot++) {
            aan candidate = inventory[slot];
            if (candidate == null || candidate.a <= 0 || candidate.c != wanted.c) continue;
            if (wanted.e()) {
                if (candidate.i() < candidate.j()) return slot;
            } else if (candidate.i() == wanted.i() && aan.a(candidate, wanted)) return slot;
        }
        return -1;
    }
}
