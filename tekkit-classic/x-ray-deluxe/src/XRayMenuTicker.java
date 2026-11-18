import java.util.EnumSet;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;

final class XRayMenuTicker implements ITickHandler {
    public void tickStart(EnumSet<TickType> types, Object... data) {
        addButton(ModLoader.getMinecraftInstance());
    }

    @SuppressWarnings("unchecked")
    static void addButton(Minecraft mc) {
        if (mc == null || !(mc.s instanceof xt)) return;
        for (Object button : mc.s.s) if (button instanceof XRayTitleButton) return;
        mc.s.s.add(new XRayTitleButton(mc.s));
    }

    public void tickEnd(EnumSet<TickType> types, Object... data) {
    }

    public EnumSet<TickType> ticks() { return EnumSet.of(TickType.RENDER); }

    public String getLabel() { return "gabrielgollo_XRayDeluxe_Menu"; }
}
