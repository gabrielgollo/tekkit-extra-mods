package com.gabrielgollo.controllerassistant.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiButtonExt;

final class AssistantSettingsScreen extends GuiScreen {
    private final ClientProxy proxy;
    private int panelLeft;
    private int panelTop;

    AssistantSettingsScreen(ClientProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void initGui() {
        panelLeft = (width - 300) / 2;
        panelTop = (height - 220) / 2;
        buttonList.clear();
        buttonList.add(new GuiButtonExt(0, panelLeft + 10, panelTop + 35, 280, 24, optionText(0)));
        buttonList.add(new GuiButtonExt(1, panelLeft + 10, panelTop + 65, 280, 24, optionText(1)));
        buttonList.add(new GuiButtonExt(2, panelLeft + 10, panelTop + 95, 280, 24, optionText(2)));
        buttonList.add(new GuiButtonExt(3, panelLeft + 10, panelTop + 163, 280, 22, net.minecraft.client.resources.I18n.format("gui.ggab_controllerassistant.scan")));
        buttonList.add(new GuiButtonExt(4, panelLeft + 10, panelTop + 190, 280, 20, net.minecraft.client.resources.I18n.format("gui.ggab_controllerassistant.done")));
    }

    private String optionText(int option) {
        String[] names = {"gui.ggab_controllerassistant.auto_tool", "gui.ggab_controllerassistant.auto_jump", "gui.ggab_controllerassistant.auto_treetap"};
        return net.minecraft.client.resources.I18n.format(names[option]) + ": " + (proxy.option(option) ? "ON" : "OFF");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 0 && button.id <= 2) {
            proxy.toggleOption(button.id);
            button.displayString = optionText(button.id);
        } else if (button.id == 3) {
            proxy.reconnect();
        } else if (button.id == 4) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(null);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(panelLeft, panelTop, panelLeft + 300, panelTop + 220, 0xEE202530);
        drawString(fontRenderer, net.minecraft.client.resources.I18n.format("gui.ggab_controllerassistant.title"), panelLeft + 10, panelTop + 10, 0xFFFFFF);
        drawString(fontRenderer, net.minecraft.client.resources.I18n.format("gui.ggab_controllerassistant.controller") + ": " + proxy.controllerStatus(), panelLeft + 10, panelTop + 132, 0xFFFF70);
        drawString(fontRenderer, net.minecraft.client.resources.I18n.format("gui.ggab_controllerassistant.axes") + ": " + proxy.axesStatus(), panelLeft + 10, panelTop + 147, 0xBCC7D4);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
