package com.gabrielgollo.genericassistant.client;

import java.io.IOException;
import net.minecraft.client.Minecraft;
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
        panelTop = (height - 240) / 2;
        buttonList.clear();
        for (int option = 0; option < 5; option++) {
            buttonList.add(new GuiButtonExt(option, panelLeft + 10, panelTop + 30 + option * 24, 280, 20, optionText(option)));
        }
        buttonList.add(new GuiButtonExt(5, panelLeft + 10, panelTop + 184, 280, 20, text("scan")));
        buttonList.add(new GuiButtonExt(6, panelLeft + 10, panelTop + 212, 280, 20, text("done")));
    }

    private String optionText(int option) {
        String[] names = {"auto_tool", "auto_jump", "auto_treetap", "refill_hotbar", "replace_broken_tool"};
        return text(names[option]) + ": " + (proxy.option(option) ? text("on") : text("off"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 0 && button.id <= 4) {
            proxy.toggleOption(button.id);
            button.displayString = optionText(button.id);
        } else if (button.id == 5) {
            proxy.reconnect();
        } else if (button.id == 6) {
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
        drawRect(panelLeft, panelTop, panelLeft + 300, panelTop + 240, 0xEE202530);
        drawString(fontRenderer, text("title"), panelLeft + 10, panelTop + 10, 0xFFFFFF);
        drawString(fontRenderer, text("controller") + ": " + proxy.controllerStatus(), panelLeft + 10, panelTop + 155, 0xFFFF70);
        drawString(fontRenderer, text("axes") + ": " + proxy.axesStatus(), panelLeft + 10, panelTop + 169, 0xBCC7D4);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    static String text(String key) {
        boolean portuguese = "pt_br".equalsIgnoreCase(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
        if (portuguese) {
            switch (key) {
                case "title": return "gabrielgollo_Generic Assistant";
                case "refill_hotbar": return "Repor hotbar";
                case "replace_broken_tool": return "Trocar ferramenta quebrada";
                case "auto_tool": return "Ferramenta automatica";
                case "auto_jump": return "Pulo automatico";
                case "auto_treetap": return "Treetap automatico";
                case "controller": return "Controle";
                case "axes": return "Eixos";
                case "scan": return "Buscar controles novamente";
                case "done": return "Concluir";
                case "on": return "LIGADO";
                case "off": return "DESLIGADO";
                default: return key;
            }
        }
        switch (key) {
            case "title": return "gabrielgollo_Generic Assistant";
            case "refill_hotbar": return "Refill hotbar";
            case "replace_broken_tool": return "Replace broken tool";
            case "auto_tool": return "Automatic tool";
            case "auto_jump": return "Automatic jump";
            case "auto_treetap": return "Automatic treetap";
            case "controller": return "Controller";
            case "axes": return "Axes";
            case "scan": return "Scan controllers again";
            case "done": return "Done";
            case "on": return "ON";
            case "off": return "OFF";
            default: return key;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
