# Generic Assistant for Tekkit SMP

Client-side gamepad support, menu navigation, automatic tool selection, native auto-jump control, rubber treetap selection, hotbar refill, and broken-tool replacement for Minecraft 1.12.2 / Forge. Open settings with F8 or the title-screen button.

Settings: `config/generic-assistant-smp.properties`. English UI is the default; Portuguese is used for `pt_BR`.

## Inventory assistance

`refillHotbar` and `replaceBrokenTool` are independent options, enabled by default and available in the settings screen. They work with keyboard/mouse and gamepad input.

When the selected stack is exhausted through use, the assistant moves a matching stack from inventory slots 9–35 into the same hotbar slot. Consumables must match item, variant, and tags. A broken damageable item is replaced by another usable copy of that same item, allowing different durability. Other hotbar slots, armor, offhand, and chests are not searched. Inventory changes use normal Minecraft inventory clicks so the server receives the update.

Opening an inventory, switching worlds or players, changing the selected slot, or dropping an item resets or suppresses replenishment. Existing settings from the previous mod name are copied to the new configuration file on first use if no new configuration exists. Remove the old mod JAR when upgrading to Generic Assistant.
