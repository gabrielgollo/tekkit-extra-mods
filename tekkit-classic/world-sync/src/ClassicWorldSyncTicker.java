import java.util.EnumSet;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;

final class ClassicWorldSyncTicker implements ITickHandler {
    private final mod_WorldSync mod;

    ClassicWorldSyncTicker(mod_WorldSync mod) {
        this.mod = mod;
    }

    public void tickStart(EnumSet<TickType> types, Object... data) {
    }

    public void tickEnd(EnumSet<TickType> types, Object... data) {
        Minecraft mc = ModLoader.getMinecraftInstance();
        if (mc == null) return;
        mod.onTickInGame(0F, mc);
        mod.onTickInGUI(0F, mc, mc.s);
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.RENDER);
    }

    public String getLabel() {
        return "gabrielgollo_WorldSync";
    }
}
