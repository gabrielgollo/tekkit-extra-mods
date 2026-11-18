import java.util.EnumSet;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

final class AssistantRenderTicker implements ITickHandler {
    private final AssistantPad pad;

    AssistantRenderTicker(AssistantPad pad) { this.pad = pad; }

    public void tickStart(EnumSet<TickType> types, Object... data) {
        pad.renderFrame(ModLoader.getMinecraftInstance());
    }

    public void tickEnd(EnumSet<TickType> types, Object... data) {
    }

    public EnumSet<TickType> ticks() { return EnumSet.of(TickType.RENDER); }

    public String getLabel() { return "gabrielgollo_GenericAssistant_Render"; }
}
