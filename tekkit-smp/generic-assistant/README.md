# Generic Assistant for Tekkit SMP

Client-side gamepad support, menu navigation, automatic tool selection, native auto-jump control, rubber treetap selection, hotbar refill, and broken-tool replacement for Minecraft 1.12.2 / Forge. Open settings with F8 or the title-screen button.

Settings: `config/generic-assistant-smp.properties`. English UI is the default; Portuguese is used for `pt_BR`.

## Inventory assistance

`refillHotbar` and `replaceBrokenTool` are independent options, enabled by default and available in the settings screen. They work with keyboard/mouse and gamepad input.

When the selected stack is exhausted through use, the assistant moves a matching stack from inventory slots 9–35 into the same hotbar slot. Consumables must match item, variant, and tags. A broken damageable item is replaced by another usable copy of that same item, allowing different durability. Other hotbar slots, armor, offhand, and chests are not searched. Inventory changes use normal Minecraft inventory clicks so the server receives the update.

Opening an inventory, switching worlds or players, changing the selected slot, or dropping an item resets or suppresses replenishment. Existing settings from the previous mod name are copied to the new configuration file on first use if no new configuration exists. Remove the old mod JAR when upgrading to Generic Assistant.

## Controller tuning

Open **Controller settings** in the assistant screen to adjust camera sensitivity (`lookSpeed`, 0.1–30), stick deadzone (`deadzone`, 5–80%), menu cursor speed (`cursorSpeed`, 1–40), and vertical camera inversion (`invertLookY`). Changes apply immediately and are saved automatically. The camera retains its default sensitivity of 5; try 10 for a faster turn rate. Existing profile values are preserved.

The same screen is available through **Mods → gabrielgollo_Generic Assistant → Config**. Done and Escape return to the parent menu.

In menus, the right stick scrolls vertically while the left stick moves the pointer. Push farther to scroll faster; centering the stick stops scrolling. LB/RB scrolling remains available. Scrolling follows the configured `axis.lookY` and is independent of camera inversion.
