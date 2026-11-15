package com.gabrielgollo.genericassistant.client;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {
    private static final int TITLE_BUTTON_ID = 28417;
    private static final int F8 = Keyboard.KEY_F8;
    private static ClientProxy instance;
    private final Properties config = new Properties();
    private final Set<Integer> heldKeys = new HashSet<Integer>();
    private KeyBinding settingsKey;
    private Controller controller;
    private Robot robot;
    private long nextScan;
    private boolean autoTool = true;
    private boolean refillHotbar = true;
    private boolean replaceBrokenTool = true;
    private final HotbarAssistant hotbar = new HotbarAssistant();
    private boolean autoTreetap = true;
    private boolean menuWasOpen;
    private boolean menuArmed;
    private boolean menuLeft;
    private boolean menuRight;
    private boolean controllerLook;
    private float expectedYaw;
    private float expectedPitch;
    private boolean haveExpectedRotation;
    private boolean titleMouseDown;
    private float deadzone = .2f;
    private float lookSpeed = 5f;
    private String moveX = "x";
    private String moveY = "y";
    private String lookX = "rx";
    private String lookY = "ry";
    private String triggerAxis = "z";
    private String triggerMode = "combined";
    private String attackAxis = "rz";
    private String useAxis = "z";
    private float triggerRest = -1f;
    private String deviceName = "";
    private final int[] buttonMap = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final boolean[] previousButtons = new boolean[10];

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        loadConfig(event.getModConfigurationDirectory());
        settingsKey = new KeyBinding("key.ggab_genericassistant.settings", F8, "key.categories.ggab_genericassistant");
        ClientRegistry.registerKeyBinding(settingsKey);
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void loadConfig(File directory) {
        File file = new File(directory, "generic-assistant-smp.properties");
        try {
            File legacy = new File(directory, "controller-assistant-smp.properties");
            if (!file.isFile() && legacy.isFile()) java.nio.file.Files.copy(legacy.toPath(), file.toPath());
            if (file.isFile()) {
                FileInputStream input = new FileInputStream(file);
                config.load(input);
                input.close();
            }
            refillHotbar = Boolean.parseBoolean(config.getProperty("refillHotbar", "true"));
            replaceBrokenTool = Boolean.parseBoolean(config.getProperty("replaceBrokenTool", "true"));
            autoTool = Boolean.parseBoolean(config.getProperty("autoTool", "true"));
            autoTreetap = Boolean.parseBoolean(config.getProperty("autoTreetap", "true"));
            deadzone = clamp(parseFloat("deadzone", .2f), .05f, .8f);
            lookSpeed = clamp(parseFloat("lookSpeed", 5f), .1f, 20f);
            moveX = value("axis.moveX", "x");
            moveY = value("axis.moveY", "y");
            lookX = value("axis.lookX", "rx");
            lookY = value("axis.lookY", "ry");
            triggerAxis = value("axis.triggers", "z");
            triggerMode = value("triggerMode", "combined");
            attackAxis = value("axis.attack", "rz");
            useAxis = value("axis.use", "z");
            deviceName = value("deviceName", "");
            triggerRest = "0".equals(value("triggerRest", "-1")) ? 0f : -1f;
            for (int i = 0; i < buttonMap.length; i++) {
                buttonMap[i] = parseInt("button." + i, i);
            }
            saveConfig(file);
        } catch (Exception error) {
            System.err.println("[Generic Assistant] Could not load config: " + error.getMessage());
        }
    }

    private String value(String key, String fallback) {
        String value = config.getProperty(key, fallback);
        config.setProperty(key, value);
        return value;
    }

    private float parseFloat(String key, float fallback) {
        try { return Float.parseFloat(value(key, String.valueOf(fallback))); }
        catch (NumberFormatException error) { return fallback; }
    }

    private int parseInt(String key, int fallback) {
        try { return Integer.parseInt(value(key, String.valueOf(fallback))); }
        catch (NumberFormatException error) { return fallback; }
    }

    private void saveConfig(File file) throws Exception {
        config.setProperty("refillHotbar", String.valueOf(refillHotbar));
        config.setProperty("replaceBrokenTool", String.valueOf(replaceBrokenTool));
        config.setProperty("autoTool", String.valueOf(autoTool));
        config.setProperty("autoTreetap", String.valueOf(autoTreetap));
        file.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(file);
        config.store(output, "Generic Assistant for Tekkit SMP");
        output.close();
    }

    void saveConfig() {
        try { saveConfig(new File(Minecraft.getMinecraft().mcDataDir, "config/generic-assistant-smp.properties")); }
        catch (Exception error) { System.err.println("[Generic Assistant] Could not save config: " + error.getMessage()); }
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        hotbar.update(mc, refillHotbar, replaceBrokenTool,
                mc.gameSettings.keyBindAttack.isKeyDown() || mc.gameSettings.keyBindUseItem.isKeyDown(),
                mc.gameSettings.keyBindDrop.isKeyDown());
        if (mc.currentScreen == null && mc.player != null && mc.world != null) {
            if (autoTool && Mouse.isButtonDown(0)) selectBestTool(mc);
            if (autoTreetap && Mouse.isButtonDown(1)) selectTreetap(mc);
        }
        pollController();
        if (mc.currentScreen instanceof GuiMainMenu) handleTitleScreen(mc);
        else titleMouseDown = false;
        if (settingsKey.isPressed()) openSettings(mc);
        if (controller == null || !poll()) {
            releaseAll();
            controller = null;
            return;
        }
        if (mc.currentScreen != null) {
            updateMenu(mc);
            return;
        }
        releaseMenuButtons();
        menuWasOpen = false;
        if (!Display.isActive() || mc.player == null || mc.world == null) {
            releaseAll();
            return;
        }
        updateGameplay(mc);
    }

    private void pollController() {
        if (controller != null || System.currentTimeMillis() < nextScan) return;
        nextScan = System.currentTimeMillis() + 2500L;
        try {
            for (Controller candidate : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
                Controller.Type type = candidate.getType();
                if ((type == Controller.Type.GAMEPAD || type == Controller.Type.STICK)
                        && (deviceName.length() == 0 || candidate.getName().toLowerCase(Locale.ROOT).contains(deviceName.toLowerCase(Locale.ROOT)))) {
                    controller = candidate;
                    System.out.println("[Generic Assistant] Connected: " + candidate.getName());
                    break;
                }
            }
        } catch (Throwable error) {
            System.err.println("[Generic Assistant] Controller scan failed: " + error.getMessage());
        }
    }

    private boolean poll() {
        try { return controller != null && controller.poll(); }
        catch (Throwable error) { return false; }
    }

    private void handleTitleScreen(Minecraft mc) {
        GuiMainMenu screen = (GuiMainMenu) mc.currentScreen;
        boolean down = Mouse.isButtonDown(0);
        if (down && !titleMouseDown) {
            int x = Mouse.getX() * screen.width / mc.displayWidth;
            int y = screen.height - Mouse.getY() * screen.height / mc.displayHeight - 1;
            if (x >= 8 && x <= 158 && y >= screen.height - 31 && y <= screen.height - 8) openSettings(mc);
        }
        titleMouseDown = down;
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void addTitleButton(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiMainMenu) {
            event.getButtonList().add(new net.minecraft.client.gui.GuiButton(TITLE_BUTTON_ID, 8, event.getGui().height - 28, 150, 20, AssistantSettingsScreen.text("title")));
        }
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void clickTitleButton(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getGui() instanceof GuiMainMenu && event.getButton().id == TITLE_BUTTON_ID) {
            event.setCanceled(true);
            openSettings(Minecraft.getMinecraft());
        }
    }

    private void updateMenu(Minecraft mc) {
        releaseGameplayKeys();
        if (!menuWasOpen) {
            menuWasOpen = true;
            menuArmed = false;
        }
        boolean a = button(0), b = button(1), x = button(2);
        if (!a && !b && !x) menuArmed = true;
        if (!menuArmed) {
            updatePreviousButtons();
            return;
        }
        try {
            if (robot == null) robot = new Robot();
            Point point = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(point.x + Math.round(axis(moveX) * 14f), point.y + Math.round(axis(moveY) * 14f));
            if (a != menuLeft) {
                if (a) robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); else robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                menuLeft = a;
            }
            if (x != menuRight) {
                if (x) robot.mousePress(InputEvent.BUTTON3_DOWN_MASK); else robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                menuRight = x;
            }
            if (b && !previousButtons[1]) tapEscape();
            if (button(4) && !previousButtons[4]) robot.mouseWheel(-1);
            if (button(5) && !previousButtons[5]) robot.mouseWheel(1);
        } catch (Exception error) {
            System.err.println("[Generic Assistant] Menu input failed: " + error.getMessage());
        }
        updatePreviousButtons();
    }

    private void updateGameplay(Minecraft mc) {
        EntityPlayer player = mc.player;
        float moveHorizontal = axis(moveX), moveVertical = axis(moveY);
        setKey(mc.gameSettings.keyBindForward, moveVertical < -.25f);
        setKey(mc.gameSettings.keyBindBack, moveVertical > .25f);
        setKey(mc.gameSettings.keyBindLeft, moveHorizontal < -.25f);
        setKey(mc.gameSettings.keyBindRight, moveHorizontal > .25f);
        setKey(mc.gameSettings.keyBindJump, button(0));
        setKey(mc.gameSettings.keyBindSneak, button(1));

        boolean mouseLook = haveExpectedRotation && (Math.abs(angleDelta(player.rotationYaw, expectedYaw)) > .01f || Math.abs(player.rotationPitch - expectedPitch) > .01f);
        float horizontalLook = axis(lookX), verticalLook = axis(lookY);
        if (mouseLook) controllerLook = false;
        if (Math.abs(horizontalLook) > .03f || Math.abs(verticalLook) > .03f) controllerLook = true;
        if (controllerLook && !mouseLook) {
            player.rotationYaw += horizontalLook * lookSpeed;
            player.rotationPitch = Math.max(-90f, Math.min(90f, player.rotationPitch + verticalLook * lookSpeed));
        }
        expectedYaw = player.rotationYaw;
        expectedPitch = player.rotationPitch;
        haveExpectedRotation = true;

        boolean split = "split".equalsIgnoreCase(triggerMode);
        float trigger = axis(triggerAxis);
        boolean attack = split ? triggerPressed(rawAxis(attackAxis)) : trigger < -.25f;
        boolean use = split ? triggerPressed(rawAxis(useAxis)) : trigger > .25f;
        if (autoTool && (attack || Mouse.isButtonDown(0))) selectBestTool(mc);
        if (autoTreetap && (use || Mouse.isButtonDown(1))) selectTreetap(mc);
        setKey(mc.gameSettings.keyBindAttack, attack);
        setKey(mc.gameSettings.keyBindUseItem, use);
        setKey(mc.gameSettings.keyBindInventory, button(3));
        setKey(mc.gameSettings.keyBindDrop, button(2));
        if (button(4) && !previousButtons[4]) player.inventory.currentItem = (player.inventory.currentItem + 8) % 9;
        if (button(5) && !previousButtons[5]) player.inventory.currentItem = (player.inventory.currentItem + 1) % 9;
        if (button(9) && !previousButtons[9]) openSettings(mc);
        else if (button(7) && !previousButtons[7]) mc.displayGuiScreen(new net.minecraft.client.gui.GuiIngameMenu());
        if (button(6) && !previousButtons[6]) toggleXray();
        updatePreviousButtons();
    }

    private void selectBestTool(Minecraft mc) {
        RayTraceResult hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        net.minecraft.block.state.IBlockState state = mc.world.getBlockState(pos);
        int current = mc.player.inventory.currentItem;
        int best = current;
        float bestStrength = toolScore(mc.player.inventory.getStackInSlot(current), state);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            float candidate = toolScore(stack, state);
            if (candidate > bestStrength) {
                best = slot;
                bestStrength = candidate;
            }
        }
        mc.player.inventory.currentItem = best;
    }

    private float toolScore(ItemStack stack, net.minecraft.block.state.IBlockState state) {
        if (stack.isEmpty()) return 1f;
        if (stack.getItem().getClass().getName().contains("ItemElectricTool")) {
            try {
                Class<?> electric = Class.forName("ic2.api.item.ElectricItem");
                Object manager = electric.getField("manager").get(null);
                Class<?> managerApi = Class.forName("ic2.api.item.IElectricItemManager");
                Method canUse = managerApi.getMethod("canUse", ItemStack.class, double.class);
                Field cost = stack.getItem().getClass().getField("operationEnergyCost");
                if (!Boolean.TRUE.equals(canUse.invoke(manager, stack, Double.valueOf(cost.getInt(stack.getItem()))))) return 1f;
            } catch (Exception ignored) { }
        }
        float speed = stack.getDestroySpeed(state);
        return stack.canHarvestBlock(state) ? speed + 1000f : speed;
    }

    private void selectTreetap(Minecraft mc) {
        RayTraceResult hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return;
        net.minecraft.block.state.IBlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (!"ic2.core.block.resources.BlockRubberWood".equals(state.getBlock().getClass().getName())) return;
        String resinFace = property(state, "resinFacing");
        if (!Boolean.parseBoolean(property(state, "resin")) || !Boolean.parseBoolean(property(state, "collectable"))) return;
        if (!hit.sideHit.getName().equalsIgnoreCase(resinFace)) return;
        int slot = treetapSlot(mc.player);
        if (slot >= 0) mc.player.inventory.currentItem = slot;
    }

    private String property(net.minecraft.block.state.IBlockState state, String wanted) {
        for (net.minecraft.block.properties.IProperty<?> property : state.getPropertyKeys()) {
            if (property.getName().equalsIgnoreCase(wanted) || property.getName().replace("_", "").equalsIgnoreCase(wanted)) {
                Comparable<?> value = state.getValue(property);
                return String.valueOf(value);
            }
        }
        return "";
    }

    private int treetapSlot(EntityPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            String itemClass = stack.getItem().getClass().getName();
            if (itemClass.equals("ic2.core.item.tool.ItemToolTreetap")) return slot;
            if (itemClass.equals("ic2.core.item.tool.electric.ItemElectricToolTreetap") && electricToolCharged(stack)) return slot;
        }
        return -1;
    }

    private boolean electricToolCharged(ItemStack stack) {
        try {
            Class<?> electric = Class.forName("ic2.api.item.ElectricItem");
            Object manager = electric.getField("manager").get(null);
            return Boolean.TRUE.equals(manager.getClass().getMethod("canUse", ItemStack.class, double.class).invoke(manager, stack, Double.valueOf(50d)));
        } catch (Exception ignored) { return false; }
    }

    private void toggleXray() {
        try {
            Class<?> xray = Class.forName("com.gabrielgollo.xray.XRayDeluxeMod");
            xray.getMethod("toggleFromController").invoke(null);
        } catch (Exception error) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("[Generic Assistant] X-Ray Deluxe is not installed."));
        }
    }

    private void openSettings(Minecraft mc) {
        releaseGameplayKeys();
        releaseMenuButtons();
        mc.displayGuiScreen(new AssistantSettingsScreen(this));
    }

    void toggleOption(int option) {
        if (option == 0) autoTool = !autoTool;
        if (option == 1) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.gameSettings.autoJump = !mc.gameSettings.autoJump;
            mc.gameSettings.saveOptions();
        }
        if (option == 2) autoTreetap = !autoTreetap;
        if (option == 3) refillHotbar = !refillHotbar;
        if (option == 4) replaceBrokenTool = !replaceBrokenTool;
        saveConfig();
    }

    boolean option(int option) {
        if (option == 0) return autoTool;
        if (option == 1) return Minecraft.getMinecraft().gameSettings.autoJump;
        if (option == 3) return refillHotbar;
        if (option == 4) return replaceBrokenTool;
        return autoTreetap;
    }

    String controllerStatus() {
        return controller == null ? "No controller detected" : controller.getName();
    }

    String axesStatus() {
        if (controller == null) return "";
        StringBuilder result = new StringBuilder();
        for (Component component : controller.getComponents()) {
            if (component.isAnalog()) {
                if (result.length() > 0) result.append(' ');
                result.append(component.getIdentifier().getName());
            }
        }
        return result.toString();
    }

    void reconnect() {
        releaseAll();
        controller = null;
        nextScan = 0;
    }

    private void setKey(KeyBinding binding, boolean down) {
        int code = binding.getKeyCode();
        if (down) {
            if (heldKeys.add(code)) KeyBinding.onTick(code);
            KeyBinding.setKeyBindState(code, true);
        } else if (heldKeys.remove(code)) {
            KeyBinding.setKeyBindState(code, physicalKey(code));
        }
    }

    private boolean physicalKey(int code) {
        return code < 0 ? Mouse.isButtonDown(code + 100) : Keyboard.isKeyDown(code);
    }

    private void releaseGameplayKeys() {
        for (Integer code : heldKeys) KeyBinding.setKeyBindState(code.intValue(), physicalKey(code.intValue()));
        heldKeys.clear();
    }

    private void releaseMenuButtons() {
        if (robot != null) {
            if (menuLeft) robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            if (menuRight) robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }
        menuLeft = false;
        menuRight = false;
    }

    private void releaseAll() {
        releaseGameplayKeys();
        releaseMenuButtons();
    }

    private void tapEscape() {
        try {
            if (robot == null) robot = new Robot();
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
        } catch (Exception ignored) { }
    }

    private void updatePreviousButtons() {
        for (int i = 0; i < previousButtons.length; i++) previousButtons[i] = button(i);
    }

    private boolean button(int index) {
        if (controller == null) return false;
        for (Component component : controller.getComponents()) {
            if (component.getIdentifier() instanceof Component.Identifier.Button
                    && component.getIdentifier().getName().equals(String.valueOf(buttonMap[index]))) return component.getPollData() > .5f;
        }
        return false;
    }

    private float rawAxis(String name) {
        if (controller != null) for (Component component : controller.getComponents()) {
            if (component.getIdentifier().getName().equals(name)) return component.getPollData();
        }
        return triggerRest;
    }

    private float axis(String name) {
        if (controller != null) for (Component component : controller.getComponents()) {
            if (component.getIdentifier().getName().equals(name)) return filter(component.getPollData(), deadzone);
        }
        return 0f;
    }

    private boolean triggerPressed(float value) {
        return (value - triggerRest) / (1f - triggerRest) > .35f;
    }

    private static float filter(float value, float zone) {
        return Math.abs(value) <= zone ? 0f : Math.signum(value) * (Math.abs(value) - zone) / (1f - zone);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float angleDelta(float value, float expected) {
        float delta = value - expected;
        while (delta > 180f) delta -= 360f;
        while (delta < -180f) delta += 360f;
        return delta;
    }
}
