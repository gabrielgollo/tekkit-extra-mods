package com.gabrielgollo.genericassistant.client;

import java.util.Collections;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public final class AssistantGuiFactory implements IModGuiFactory {
    public void initialize(Minecraft minecraft) {
    }

    public boolean hasConfigGui() { return true; }

    public GuiScreen createConfigGui(GuiScreen parent) {
        ClientProxy proxy = ClientProxy.instance();
        return proxy == null ? parent : new AssistantSettingsScreen(proxy, parent);
    }

    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return Collections.emptySet();
    }
}
