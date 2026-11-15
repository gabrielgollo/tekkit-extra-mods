package com.gabrielgollo.genericassistant.client;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.ClickType;

final class HotbarAssistant {
    private Object player;
    private Object world;
    private int selected = -1;
    private ItemStack previous = ItemStack.EMPTY;
    private boolean action;

    void reset() {
        player = null;
        world = null;
        previous = ItemStack.EMPTY;
        selected = -1;
        action = false;
    }

    void update(Minecraft mc, boolean refill, boolean replace, boolean interacting, boolean dropping) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null
                || mc.player.openContainer != mc.player.inventoryContainer
                || !mc.player.inventory.getItemStack().isEmpty() || mc.player.isDead) { reset(); return; }
        int slot = mc.player.inventory.currentItem;
        ItemStack current = mc.player.inventory.getStackInSlot(slot);
        if (player == mc.player && world == mc.world && selected == slot && !previous.isEmpty()
                && action && !dropping && current.isEmpty()) {
            if ((previous.isItemStackDamageable() && replace) || (!previous.isItemStackDamageable() && refill)) {
                int source = replacement(mc.player.inventory.mainInventory, previous);
                if (source >= 0) mc.playerController.windowClick(mc.player.inventoryContainer.windowId,
                        source, slot, ClickType.SWAP, mc.player);
            }
        }
        player = mc.player;
        world = mc.world;
        selected = slot;
        previous = mc.player.inventory.getStackInSlot(slot).copy();
        action = interacting && !dropping;
    }

    static int replacement(java.util.List<ItemStack> inventory, ItemStack wanted) {
        for (int slot = 9; slot < Math.min(36, inventory.size()); slot++) {
            ItemStack candidate = inventory.get(slot);
            if (candidate.isEmpty() || candidate.getItem() != wanted.getItem()) continue;
            if (wanted.isItemStackDamageable()) {
                if (candidate.getItemDamage() < candidate.getMaxDamage()) return slot;
            } else if (candidate.getMetadata() == wanted.getMetadata()
                    && ItemStack.areItemStackTagsEqual(candidate, wanted)) return slot;
        }
        return -1;
    }
}
