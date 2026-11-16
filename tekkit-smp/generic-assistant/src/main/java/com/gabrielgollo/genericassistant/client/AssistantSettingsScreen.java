package com.gabrielgollo.genericassistant.client;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiButtonExt;

final class AssistantSettingsScreen extends GuiScreen {
    private final ClientProxy proxy;
    private final GuiScreen parent;
    private boolean controllerPage;
    private int panelLeft;
    private int panelTop;

    AssistantSettingsScreen(ClientProxy proxy, GuiScreen parent) {
        this.proxy = proxy;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        panelLeft = (width - 300) / 2;
        panelTop = (height - 240) / 2;
        buttonList.clear();
        if (controllerPage) {
            for (int setting = 0; setting < 3; setting++) {
                int y = panelTop + 35 + setting * 28;
                buttonList.add(new GuiButtonExt(10 + setting * 2, panelLeft + 10, y, 30, 20, "-"));
                buttonList.add(new GuiButtonExt(11 + setting * 2, panelLeft + 260, y, 30, 20, "+"));
            }
            buttonList.add(new GuiButtonExt(16, panelLeft + 10, panelTop + 122, 280, 20,
                    text("invert_y") + ": " + text(proxy.invertedCamera() ? "on" : "off")));
            buttonList.add(new GuiButtonExt(5, panelLeft + 10, panelTop + 150, 280, 20, text("scan")));
        } else {
            for (int option = 0; option < 5; option++) {
                buttonList.add(new GuiButtonExt(option, panelLeft + 10, panelTop + 30 + option * 24, 280, 20, optionText(option)));
            }
        }
        buttonList.add(new GuiButtonExt(7, panelLeft + 10, panelTop + 184, 280, 20, text(controllerPage ? "assistance" : "controller_settings")));
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
        } else if (button.id >= 10 && button.id <= 15) {
            proxy.adjustCameraSetting((button.id - 10) / 2, button.id % 2 == 0 ? -1 : 1);
        } else if (button.id == 16) {
            proxy.toggleCameraInversion();
            initGui();
        } else if (button.id == 7) {
            controllerPage = !controllerPage;
            initGui();
        } else if (button.id == 5) {
            proxy.reconnect();
        } else if (button.id == 6) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(panelLeft, panelTop, panelLeft + 300, panelTop + 240, 0xEE202530);
        drawString(fontRenderer, text("title"), panelLeft + 10, panelTop + 10, 0xFFFFFF);
        if (controllerPage) {
            String[] labels = {"sensitivity", "deadzone", "cursor_speed"};
            for (int setting = 0; setting < 3; setting++) {
                float value = proxy.cameraSetting(setting);
                String shown = setting == 1 ? Math.round(value * 100f) + "%" : String.format(java.util.Locale.ROOT, "%.1f", value);
                drawCenteredString(fontRenderer, text(labels[setting]) + ": " + shown, panelLeft + 150, panelTop + 41 + setting * 28, 0xFFFFFF);
            }
        } else {
            drawString(fontRenderer, text("controller") + ": " + proxy.controllerStatus(), panelLeft + 10, panelTop + 155, 0xFFFF70);
            drawString(fontRenderer, text("axes") + ": " + proxy.axesStatus(), panelLeft + 10, panelTop + 169, 0xBCC7D4);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    static String text(String key) {
        boolean portuguese = "pt_br".equalsIgnoreCase(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
        if (portuguese) {
            switch (key) {
                case "sensitivity": return "Sensibilidade da camera";
                case "deadzone": return "Zona morta";
                case "cursor_speed": return "Velocidade do cursor";
                case "invert_y": return "Inverter camera vertical";
                case "controller_settings": return "Configurar controle";
                case "assistance": return "Assistencias";
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
            case "sensitivity": return "Camera sensitivity";
            case "deadzone": return "Stick deadzone";
            case "cursor_speed": return "Menu cursor speed";
            case "invert_y": return "Invert vertical camera";
            case "controller_settings": return "Controller settings";
            case "assistance": return "Assistance settings";
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
