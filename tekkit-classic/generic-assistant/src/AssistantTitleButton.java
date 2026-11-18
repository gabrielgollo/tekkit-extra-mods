import net.minecraft.client.Minecraft;

final class AssistantTitleButton extends abp {
    private final mod_GenericAssistant mod;

    AssistantTitleButton(mod_GenericAssistant mod, int y) {
        super(28417, 8, y, 150, 20, "Generic Assistant");
        this.mod = mod;
    }

    public boolean c(Minecraft mc, int x, int y) {
        if (!super.c(mc, x, y)) return false;
        // The legacy title screen has no event for handling an injected button.
        mod.openSettings(mc);
        return true;
    }
}
