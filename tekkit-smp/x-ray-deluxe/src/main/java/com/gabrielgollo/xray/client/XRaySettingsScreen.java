package com.gabrielgollo.xray.client;

import com.gabrielgollo.xray.XRayDeluxeMod;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

public final class XRaySettingsScreen extends GuiScreen {
    private int left;
    private int top;
    private int firstRow;

    @Override
    public void initGui() {
        left = (width - 370) / 2;
        top = (height - 310) / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, left + 370, top + 310, 0xEE202530);
        drawString(fontRenderer, text("gui.ggab_xray.title", "gabrielgollo_X-Ray Deluxe"), left + 10, top + 9, 0xFFFFFF);
        drawButton(left + 10, top + 28, 145, 20, text("gui.ggab_xray.toggle", "X-Ray") + ": " + (XRayDeluxeMod.enabled ? "ON" : "OFF"), XRayDeluxeMod.enabled);
        drawButton(left + 165, top + 28, 195, 20, text("gui.ggab_xray.fullbright", "Fullbright") + ": " + (XRayDeluxeMod.fullbright ? "ON" : "OFF"), XRayDeluxeMod.fullbright);
        drawString(fontRenderer, text("gui.ggab_xray.ores", "Ore selection"), left + 10, top + 53, 0xFFD36A);
        List<OreEntry> entries = XRayDeluxeMod.getCategories();
        int visibleEntries = 14;
        int start = Math.min(firstRow * 2, Math.max(0, entries.size() - visibleEntries));
        for (int i = 0; i < visibleEntries && start + i < entries.size(); i++) {
            OreEntry entry = entries.get(start + i);
            int column = i % 2;
            int row = i / 2;
            int x = left + 10 + column * 175;
            int y = top + 69 + row * 23;
            boolean selected = XRayDeluxeMod.isSelected(entry.getKey());
            boolean hovered = mouseX >= x && mouseX < x + 170 && mouseY >= y && mouseY < y + 21;
            drawRect(x, y, x + 170, y + 21, selected ? 0xFF365E48 : 0xFF303A48);
            drawRect(x, y, x + 2, y + 21, hovered ? 0xFFFFFFFF : selected ? 0xFF8DFF9D : 0xFF687687);
            ItemStack icon = entry.getIcon();
            if (!icon.isEmpty()) itemRender.renderItemAndEffectIntoGUI(icon, x + 5, y + 2);
            String marker = selected ? "[x] " : "[ ] ";
            String label = entry.getLabel();
            while (fontRenderer.getStringWidth(marker + label) > 136 && label.length() > 3) label = label.substring(0, label.length() - 2) + "…";
            drawString(fontRenderer, marker + label, x + 27, y + 7, 0xFFFFFF);
        }
        String page = entries.isEmpty() ? "0 / 0" : (firstRow + 1) + " / " + Math.max(1, (entries.size() + visibleEntries - 1) / visibleEntries);
        drawCenteredString(fontRenderer, page, left + 185, top + 232, 0xBCC7D4);
        drawButton(left + 10, top + 246, 88, 19, text("gui.ggab_xray.range", "Range") + " -", false);
        drawCenteredString(fontRenderer, String.valueOf(XRayDeluxeMod.radius), left + 145, top + 251, 0xFFD36A);
        drawButton(left + 192, top + 246, 88, 19, text("gui.ggab_xray.range", "Range") + " +", false);
        drawButton(left + 284, top + 246, 76, 19, text("gui.ggab_xray.reset", "Reset defaults"), false);
        drawButton(left + 10, top + 270, 170, 19, text("gui.ggab_xray.opacity", "Hidden opacity") + " -", false);
        drawCenteredString(fontRenderer, XRayDeluxeMod.hiddenOpacity + "%", left + 185, top + 275, 0xFFD36A);
        drawButton(left + 200, top + 270, 170, 19, text("gui.ggab_xray.opacity", "Hidden opacity") + " +", false);
        drawCenteredString(fontRenderer, text("gui.ggab_xray.help", "See Controls for key bindings | ESC closes"), left + 185, top + 294, 0xBCC7D4);
    }

    private static String text(String key, String fallback) {
        String translated = I18n.format(key);
        return key.equals(translated) ? fallback : translated;
    }

    private void drawButton(int x, int y, int buttonWidth, int buttonHeight, String text, boolean active) {
        drawRect(x, y, x + buttonWidth, y + buttonHeight, active ? 0xFF365E48 : 0xFF34485D);
        drawCenteredString(fontRenderer, text, x + buttonWidth / 2, y + (buttonHeight - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;
        if (inside(mouseX, mouseY, left + 10, top + 28, 145, 20)) {
            XRayDeluxeMod.setEnabled(mc, !XRayDeluxeMod.enabled);
            return;
        }
        if (inside(mouseX, mouseY, left + 165, top + 28, 195, 20)) {
            XRayDeluxeMod.toggleFullbright(mc);
            return;
        }
        List<OreEntry> entries = XRayDeluxeMod.getCategories();
        int visibleEntries = 14;
        int start = Math.min(firstRow * 2, Math.max(0, entries.size() - visibleEntries));
        for (int i = 0; i < visibleEntries && start + i < entries.size(); i++) {
            int x = left + 10 + (i % 2) * 175;
            int y = top + 69 + (i / 2) * 23;
            if (inside(mouseX, mouseY, x, y, 170, 21)) {
                XRayDeluxeMod.toggleCategory(entries.get(start + i).getKey());
                return;
            }
        }
        if (inside(mouseX, mouseY, left + 10, top + 246, 88, 19)) XRayDeluxeMod.changeRadius(-4);
        else if (inside(mouseX, mouseY, left + 192, top + 246, 88, 19)) XRayDeluxeMod.changeRadius(4);
        else if (inside(mouseX, mouseY, left + 284, top + 246, 76, 19)) XRayDeluxeMod.resetSelection();
        else if (inside(mouseX, mouseY, left + 10, top + 270, 170, 19)) XRayDeluxeMod.changeOpacity(-10);
        else if (inside(mouseX, mouseY, left + 200, top + 270, 170, 19)) XRayDeluxeMod.changeOpacity(10);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getDWheel();
        super.handleMouseInput();
        if (wheel < 0) firstRow++;
        if (wheel > 0) firstRow = Math.max(0, firstRow - 1);
        int maxRows = Math.max(0, (XRayDeluxeMod.getCategories().size() + 13) / 14 - 1);
        firstRow = Math.min(firstRow, maxRows);
    }

    private boolean inside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(null);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
