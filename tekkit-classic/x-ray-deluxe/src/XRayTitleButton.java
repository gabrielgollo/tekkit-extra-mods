import net.minecraft.client.Minecraft;

final class XRayTitleButton extends abp {
    private final vp parent;

    XRayTitleButton(vp parent) {
        super(28418, parent.q - 108, 8, 100, 20, "X-Ray Deluxe");
        this.parent = parent;
    }

    public boolean c(Minecraft mc, int x, int y) {
        if (!super.c(mc, x, y)) return false;
        // The legacy title screen has no event for handling injected buttons.
        mc.a(new XRayDeluxeGui(parent));
        return true;
    }
}
