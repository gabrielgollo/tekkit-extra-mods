package com.gabrielgollo.worldsync;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

final class WorldSyncScreen extends GuiScreen {
    private final WorldSyncMod mod;
    private final GuiScreen parent;
    private GuiTextField endpoint;
    private GuiTextField bucket;
    private GuiTextField region;
    private GuiTextField accessKey;
    private GuiTextField secretKey;
    private GuiTextField userId;

    WorldSyncScreen(WorldSyncMod mod) {
        this(mod, new GuiMainMenu());
    }

    WorldSyncScreen(WorldSyncMod mod, GuiScreen parent) {
        this.mod = mod;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int margin = 18;
        int gap = 14;
        int columnWidth = (width - margin * 2 - gap) / 2;
        int right = margin + columnWidth + gap;
        WorldSyncConfig config = mod.config();
        endpoint = field(0, margin, 38, columnWidth, config.endpoint);
        bucket = field(1, right, 38, columnWidth, config.bucket);
        region = field(2, margin, 82, columnWidth, config.region);
        accessKey = field(3, right, 82, columnWidth, config.accessKey);
        secretKey = field(4, margin, 126, columnWidth, "");
        userId = field(5, right, 126, columnWidth, config.userId);
        int center = width / 2;
        int buttonWidth = Math.min(150, (width - 28) / 2);
        int buttonGap = 8;
        int firstButtonX = center - buttonWidth - buttonGap / 2;
        int secondButtonX = center + buttonGap / 2;
        buttonList.add(new GuiButton(1, firstButtonX, height - 52, buttonWidth, 20, text("upload")));
        buttonList.add(new GuiButton(2, secondButtonX, height - 52, buttonWidth, 20, text("download")));
        buttonList.add(new GuiButton(3, firstButtonX, height - 28, buttonWidth, 20, text("save_sync")));
        buttonList.add(new GuiButton(0, secondButtonX, height - 28, buttonWidth, 20, text("continue")));
    }

    private GuiTextField field(int id, int x, int y, int fieldWidth, String value) {
        GuiTextField field = new GuiTextField(id, fontRenderer, x, y + 11, fieldWidth, 17);
        field.setMaxStringLength(512);
        field.setText(value);
        return field;
    }

    @Override
    public void updateScreen() {
        endpoint.updateCursorCounter();
        bucket.updateCursorCounter();
        region.updateCursorCounter();
        accessKey.updateCursorCounter();
        secretKey.updateCursorCounter();
        userId.updateCursorCounter();
        boolean fieldsEnabled = !mod.isBusy() && mod.conflict() == null;
        endpoint.setEnabled(fieldsEnabled);
        bucket.setEnabled(fieldsEnabled);
        region.setEnabled(fieldsEnabled);
        accessKey.setEnabled(fieldsEnabled);
        secretKey.setEnabled(fieldsEnabled);
        userId.setEnabled(fieldsEnabled);
        for (GuiButton button : buttonList) {
            button.enabled = !mod.isBusy();
            if (button.id == 1 || button.id == 2) {
                button.visible = mod.conflict() != null;
                button.enabled = !mod.isBusy() && mod.conflict() != null;
            }
            if (button.id == 3) button.enabled = !mod.isBusy() && mod.conflict() == null;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0 && !mod.isBusy() && mod.conflict() == null) {
            returnToParent();
        } else if (button.id == 1 && !mod.isBusy()) {
            mod.resolveConflict(true);
        } else if (button.id == 2 && !mod.isBusy()) {
            mod.resolveConflict(false);
        } else if (button.id == 3 && !mod.isBusy() && mod.conflict() == null) {
            try {
                mod.updateConnection(endpoint.getText(), bucket.getText(), region.getText(), accessKey.getText(), secretKey.getText(), userId.getText());
                secretKey.setText("");
                mod.retrySync();
            } catch (Exception error) {
                System.err.println("[World Sync] Could not save connection settings: " + error.getMessage());
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        endpoint.mouseClicked(mouseX, mouseY, mouseButton);
        bucket.mouseClicked(mouseX, mouseY, mouseButton);
        region.mouseClicked(mouseX, mouseY, mouseButton);
        accessKey.mouseClicked(mouseX, mouseY, mouseButton);
        secretKey.mouseClicked(mouseX, mouseY, mouseButton);
        userId.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE && !mod.isBusy() && mod.conflict() == null) {
            returnToParent();
            return;
        }
        if (endpoint.textboxKeyTyped(typedChar, keyCode)) return;
        if (bucket.textboxKeyTyped(typedChar, keyCode)) return;
        if (region.textboxKeyTyped(typedChar, keyCode)) return;
        if (accessKey.textboxKeyTyped(typedChar, keyCode)) return;
        if (secretKey.textboxKeyTyped(typedChar, keyCode)) return;
        if (userId.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, text("title"), width / 2, 10, 0xFFFFFF);
        drawField(text("endpoint"), endpoint);
        drawField(text("bucket"), bucket);
        drawField(text("region"), region);
        drawField(text("access_key"), accessKey);
        drawString(fontRenderer, text("secret_key"), secretKey.x, secretKey.y - 11, 0xCCCCCC);
        secretKey.drawTextBox();
        drawRect(secretKey.x + 1, secretKey.y + 1, secretKey.x + secretKey.width - 1, secretKey.y + secretKey.height - 1, 0xFF000000);
        String masked = repeat('*', secretKey.getText().length());
        fontRenderer.drawString(masked, secretKey.x + 4, secretKey.y + 5, 0xE0E0E0);
        if (secretKey.isFocused() && (secretKey.getCursorPosition() / 6) % 2 == 0) {
            int cursorX = secretKey.x + 4 + fontRenderer.getStringWidth(repeat('*', secretKey.getCursorPosition()));
            drawRect(cursorX, secretKey.y + 4, cursorX + 1, secretKey.y + 14, 0xFFE0E0E0);
        }
        drawField(text("user_id"), userId);
        WorldSyncMod.Conflict conflict = mod.conflict();
        if (conflict != null) {
            drawCenteredString(fontRenderer, text("conflict"), width / 2, height - 76, 0xFFFF55);
            drawCenteredString(fontRenderer, conflict.remote.name, width / 2, height - 65, 0xFFFFFF);
        } else {
            String status = mod.status();
            if (fontRenderer.getStringWidth(status) > width - 24) status = fontRenderer.trimStringToWidth(status, width - 24);
            drawCenteredString(fontRenderer, status, width / 2, height - 39, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawField(String label, GuiTextField field) {
        drawString(fontRenderer, label, field.x, field.y - 10, 0xCCCCCC);
        field.drawTextBox();
    }

    private String text(String key) {
        boolean portuguese = "pt_br".equalsIgnoreCase(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
        if (portuguese) {
            switch (key) {
                case "title": return "gabrielgollo_World Sync";
                case "endpoint": return "Endereco do endpoint";
                case "bucket": return "Nome do bucket";
                case "region": return "Regiao";
                case "access_key": return "Access key ID";
                case "secret_key": return "Secret key (vazio mantem a salva)";
                case "user_id": return "ID de sincronizacao";
                case "save_sync": return "Salvar e sincronizar";
                case "continue": return "Continuar";
                case "upload": return "Usar mundo local";
                case "download": return "Usar mundo da nuvem";
                case "conflict": return "O mundo local e mais recente que a copia remota";
                default: return key;
            }
        }
        switch (key) {
            case "title": return "gabrielgollo_World Sync";
            case "endpoint": return "Bucket endpoint URL";
            case "bucket": return "Bucket name";
            case "region": return "Region";
            case "access_key": return "Access key ID";
            case "secret_key": return "Secret key (blank keeps saved key)";
            case "user_id": return "Sync user ID";
            case "save_sync": return "Save and sync";
            case "continue": return "Continue";
            case "upload": return "Use local world";
            case "download": return "Use cloud world";
            case "conflict": return "Local world is newer than the remote copy";
            default: return key;
        }
    }

    private void returnToParent() {
        mc.displayGuiScreen(parent);
    }

    private static String repeat(char character, int count) {
        StringBuilder value = new StringBuilder(Math.max(0, count));
        for (int i = 0; i < count; i++) value.append(character);
        return value.toString();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
