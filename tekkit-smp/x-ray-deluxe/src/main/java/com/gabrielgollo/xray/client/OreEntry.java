package com.gabrielgollo.xray.client;

import net.minecraft.item.ItemStack;

public final class OreEntry implements Comparable<OreEntry> {
    private final String key;
    private final String label;
    private final ItemStack icon;

    public OreEntry(String key, String label, ItemStack icon) {
        this.key = key;
        this.label = label;
        this.icon = icon;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public ItemStack getIcon() { return icon; }

    @Override
    public int compareTo(OreEntry other) {
        return label.compareToIgnoreCase(other.label);
    }
}
