import java.util.EnumSet;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

final class AssistantRenderTicker implements ITickHandler {
    private final mod_GenericAssistant mod;

    AssistantRenderTicker(mod_GenericAssistant mod) { this.mod = mod; }

    public void tickStart(EnumSet<TickType> types, Object... data) {
        net.minecraft.client.Minecraft mc=ModLoader.getMinecraftInstance();
        mod.updateTitleScreen(mc);
        mod.pad.renderFrame(mc);
    }

    public void tickEnd(EnumSet<TickType> types, Object... data) {
    }

    public EnumSet<TickType> ticks() { return EnumSet.of(TickType.RENDER); }

    public String getLabel() { return "gabrielgollo_GenericAssistant_Render"; }
}
