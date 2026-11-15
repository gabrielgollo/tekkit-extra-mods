package com.gabrielgollo.xray;

import com.gabrielgollo.xray.client.OreEntry;
import com.gabrielgollo.xray.client.XRaySettingsScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

@Mod(modid = XRayDeluxeMod.MOD_ID, name = "gabrielgollo_X-Ray Deluxe", version = "1.0.0-mc1.12.2", clientSideOnly = true, acceptedMinecraftVersions = "[1.12.2]")
public final class XRayDeluxeMod {
    public static final String MOD_ID = "gabrielgollo_xraydeluxe";
    private static XRayDeluxeMod instance;
    public static boolean enabled;
    public static boolean fullbright;
    public static int hiddenOpacity = 0;
    public static int radius = 12;
    private static float previousGamma;
    private static final Set<String> selected = new LinkedHashSet<String>();
    private static final List<OreEntry> categories = new ArrayList<OreEntry>();
    private static KeyBinding toggleKey;
    private static KeyBinding menuKey;
    private static File configFile;
    private static File defaultsFile;
    private static boolean selectedConfigured;
    private long nextScan;
    private static long selectionRevision;
    private long renderedRevision = -1;
    private net.minecraft.world.World renderedWorld;
    private int[][] overlay = new int[0][4];

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        File config = event.getModConfigurationDirectory();
        configFile = new File(config, "x-ray-deluxe-smp.properties");
        defaultsFile = new File(config, "x-ray-deluxe-smp-defaults.json");
        toggleKey = new KeyBinding("key.ggab_xray.toggle.v2", Keyboard.KEY_K, "key.categories.ggab_xray");
        menuKey = new KeyBinding("key.ggab_xray.menu.v2", Keyboard.KEY_J, "key.categories.ggab_xray");
        ClientRegistry.registerKeyBinding(toggleKey);
        ClientRegistry.registerKeyBinding(menuKey);
        loadConfig();
        MinecraftForge.EVENT_BUS.register(this);
        net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        discoverOreCategories();
        if (!selectedConfigured) selected.addAll(defaultSelection());
        saveConfig();
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try {
            if (configFile.isFile()) {
                FileInputStream input = new FileInputStream(configFile);
                properties.load(input);
                input.close();
            }
            hiddenOpacity = clamp(Integer.parseInt(properties.getProperty("hiddenOpacity", "0")), 0, 100);
            radius = clamp(Integer.parseInt(properties.getProperty("radius", "12")), 4, 32);
            selectedConfigured = properties.containsKey("selected");
            String raw = properties.getProperty("selected", "").trim();
            if (!raw.isEmpty()) for (String value : raw.split(",")) selected.add(value.trim());
        } catch (Exception error) {
            System.err.println("[X-Ray Deluxe] Could not read configuration: " + error.getMessage());
        }
    }

    private static Set<String> defaultSelection() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        try {
            if (!defaultsFile.isFile()) {
                defaultsFile.getParentFile().mkdirs();
                InputStream bundled = XRayDeluxeMod.class.getResourceAsStream("/x-ray-deluxe-defaults.json");
                if (bundled != null) {
                    Files.copy(bundled, defaultsFile.toPath());
                    bundled.close();
                }
            }
            if (defaultsFile.isFile()) {
                String json = new String(Files.readAllBytes(defaultsFile.toPath()), StandardCharsets.UTF_8);
                com.google.gson.JsonObject object = new com.google.gson.JsonParser().parse(json).getAsJsonObject();
                if (!object.has("selectAllDiscovered") || object.get("selectAllDiscovered").getAsBoolean()) {
                    for (OreEntry entry : categories) result.add(entry.getKey());
                }
                if (object.has("oreNames")) for (com.google.gson.JsonElement element : object.getAsJsonArray("oreNames")) result.add("dict:" + element.getAsString());
                if (object.has("blockRegistryNames")) for (com.google.gson.JsonElement element : object.getAsJsonArray("blockRegistryNames")) result.add("block:" + element.getAsString());
                return result;
            }
        } catch (Exception error) {
            System.err.println("[X-Ray Deluxe] Invalid defaults JSON; selecting discovered ores: " + error.getMessage());
        }
        for (OreEntry entry : categories) result.add(entry.getKey());
        return result;
    }

    private static void discoverOreCategories() {
        categories.clear();
        Set<String> seen = new LinkedHashSet<String>();
        for (String name : OreDictionary.getOreNames()) {
            if (!name.startsWith("ore") || name.length() <= 3) continue;
            List<ItemStack> stacks = OreDictionary.getOres(name);
            ItemStack icon = ItemStack.EMPTY;
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.ItemBlock) {
                    icon = stack.copy();
                    break;
                }
            }
            String key = "dict:" + name;
            if (seen.add(key)) categories.add(new OreEntry(key, formatName(name.substring(3)), icon));
        }
        for (Block block : Block.REGISTRY) {
            ResourceLocation id = block.getRegistryName();
            if (id == null || !id.getResourcePath().toLowerCase(Locale.ROOT).contains("ore")) continue;
            String key = "block:" + id.toString();
            if (seen.add(key)) categories.add(new OreEntry(key, formatName(id.getResourceDomain() + ":" + id.getResourcePath()), new ItemStack(block)));
        }
        Collections.sort(categories);
    }

    private static String formatName(String raw) {
        String value = raw.replace('_', ' ').replace(':', ' ');
        StringBuilder result = new StringBuilder();
        for (String part : value.split(" ")) {
            if (part.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private static void saveConfig() {
        try {
            configFile.getParentFile().mkdirs();
            Properties properties = new Properties();
            properties.setProperty("hiddenOpacity", String.valueOf(hiddenOpacity));
            properties.setProperty("radius", String.valueOf(radius));
            StringBuilder value = new StringBuilder();
            for (String entry : selected) {
                if (value.length() > 0) value.append(',');
                value.append(entry);
            }
            properties.setProperty("selected", value.toString());
            FileOutputStream output = new FileOutputStream(configFile);
            properties.store(output, "X-Ray Deluxe for Tekkit SMP 1.12.2");
            output.close();
        } catch (Exception error) {
            System.err.println("[X-Ray Deluxe] Could not save configuration: " + error.getMessage());
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;
        int key = Keyboard.getEventKey();
        if (key == toggleKey.getKeyCode()) setEnabled(mc, !enabled);
        else if (key == menuKey.getKeyCode() && mc.world != null) mc.displayGuiScreen(new XRaySettingsScreen());
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled || mc.world == null || mc.player == null) return;
        if (renderedWorld != mc.world || renderedRevision != selectionRevision) {
            renderedWorld = mc.world;
            renderedRevision = selectionRevision;
            nextScan = 0L;
        }
        if (System.currentTimeMillis() >= nextScan) {
            overlay = findHighlights(mc);
            nextScan = System.currentTimeMillis() + 600L;
        }
        drawHighlights(mc, event.getPartialTicks());
    }

    private int[][] findHighlights(Minecraft mc) {
        ArrayList<int[]> found = new ArrayList<int[]>();
        int centerX = (int)Math.floor(mc.player.posX);
        int centerY = (int)Math.floor(mc.player.posY);
        int centerZ = (int)Math.floor(mc.player.posZ);
        int worldHeight = mc.world.getHeight();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = Math.max(0, centerY - radius); y <= Math.min(worldHeight - 1, centerY + radius); y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = mc.world.getBlockState(pos);
                    if (isLava(state) || isSelectedOre(state)) found.add(new int[] {x, y, z});
                }
            }
        }
        return found.toArray(new int[found.size()][]);
    }

    private void drawHighlights(Minecraft mc, float partialTicks) {
        net.minecraft.entity.Entity view = mc.getRenderViewEntity();
        double cameraX = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        double cameraY = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        double cameraZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);
        org.lwjgl.opengl.GL11.glPushMatrix();
        try {
            org.lwjgl.opengl.GL11.glTranslated(-cameraX, -cameraY, -cameraZ);
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
            org.lwjgl.opengl.GL11.glDepthFunc(org.lwjgl.opengl.GL11.GL_LEQUAL);
            org.lwjgl.opengl.GL11.glDepthRange(0d, .001d);
            org.lwjgl.opengl.GL11.glDepthMask(true);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_LIGHTING);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);
            mc.getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
            for (int[] point : overlay) {
                BlockPos pos = new BlockPos(point[0], point[1], point[2]);
                IBlockState state = mc.world.getBlockState(pos);
                if (!isLava(state) && !isSelectedOre(state)) continue;
                net.minecraft.client.renderer.block.model.IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
                net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = model.getParticleTexture();
                if (sprite == null) continue;
                float brightness = isLava(state) ? 1f : .92f;
                org.lwjgl.opengl.GL11.glColor3f(brightness, brightness, brightness);
                drawCube(point[0], point[1], point[2], sprite);
            }
        } finally {
            org.lwjgl.opengl.GL11.glDepthRange(0d, 1d);
            org.lwjgl.opengl.GL11.glPopMatrix();
            org.lwjgl.opengl.GL11.glPopAttrib();
        }
    }

    private static void drawCube(int x, int y, int z, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
        double minU = sprite.getMinU(), maxU = sprite.getMaxU(), minV = sprite.getMinV(), maxV = sprite.getMaxV();
        int[][][] faces = {
            {{0,0,1},{0,0,0},{1,0,0},{1,0,1}}, {{0,1,0},{0,1,1},{1,1,1},{1,1,0}},
            {{1,1,0},{1,0,0},{0,0,0},{0,1,0}}, {{0,1,1},{0,0,1},{1,0,1},{1,1,1}},
            {{0,1,0},{0,0,0},{0,0,1},{0,1,1}}, {{1,1,1},{1,0,1},{1,0,0},{1,1,0}}
        };
        org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_QUADS);
        for (int face = 0; face < faces.length; face++) {
            float shade = face == 1 ? 1f : face == 0 ? .55f : face < 4 ? .78f : .66f;
            org.lwjgl.opengl.GL11.glColor3f(shade, shade, shade);
            for (int corner = 0; corner < 4; corner++) {
                boolean right = corner == 1 || corner == 2;
                boolean bottom = corner >= 2;
                org.lwjgl.opengl.GL11.glTexCoord2d(right ? maxU : minU, bottom ? maxV : minV);
                int[] vertex = faces[face][corner];
                org.lwjgl.opengl.GL11.glVertex3d(x + vertex[0], y + vertex[1], z + vertex[2]);
            }
        }
        org.lwjgl.opengl.GL11.glEnd();
    }

    public static boolean shouldHide(IBlockState state, BlockPos pos, net.minecraft.world.IBlockAccess access) {
        if (!enabled || state.getBlock().isAir(state, access, pos) || isLava(state) || isSelectedOre(state)) return false;
        return hiddenOpacity == 0;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isSelectedOre(IBlockState state) {
        ResourceLocation registryName = state.getBlock().getRegistryName();
        if (registryName != null && selected.contains("block:" + registryName.toString())) return true;
        ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        if (stack.isEmpty()) return false;
        try {
            for (int id : OreDictionary.getOreIDs(stack)) {
                String name = OreDictionary.getOreName(id);
                if (name.startsWith("ore") && selected.contains("dict:" + name)) return true;
            }
        } catch (IllegalArgumentException invalidStack) {
            return false;
        }
        return false;
    }

    public static boolean isLava(IBlockState state) {
        return state.getMaterial() == net.minecraft.block.material.Material.LAVA;
    }

    public static int getHiddenOpacity() {
        return hiddenOpacity;
    }

    public static void toggleFromController() {
        setEnabled(Minecraft.getMinecraft(), !enabled);
    }

    public static void setEnabled(Minecraft mc, boolean value) {
        if (enabled == value) return;
        enabled = value;
        refreshWorld(mc);
        tell(mc, "X-Ray Deluxe " + (enabled ? "enabled" : "disabled"));
    }

    public static void toggleFullbright(Minecraft mc) {
        if (mc == null || mc.gameSettings == null) return;
        if (!fullbright) {
            previousGamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 16f;
            fullbright = true;
        } else {
            mc.gameSettings.gammaSetting = previousGamma;
            fullbright = false;
        }
    }

    public static void changeOpacity(int delta) {
        hiddenOpacity = clamp(hiddenOpacity + delta, 0, 100);
        selectionChanged();
    }

    public static void changeRadius(int delta) {
        radius = clamp(radius + delta, 4, 32);
        selectionChanged();
    }

    public static List<OreEntry> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public static boolean isSelected(String key) {
        return selected.contains(key);
    }

    public static void toggleCategory(String key) {
        if (!selected.remove(key)) selected.add(key);
        selectionChanged();
    }

    public static void resetSelection() {
        selected.clear();
        selected.addAll(defaultSelection());
        selectionChanged();
    }

    private static void selectionChanged() {
        selectionRevision++;
        saveConfig();
        refreshWorld(Minecraft.getMinecraft());
    }

    private static void refreshWorld(Minecraft mc) {
        if (mc != null && mc.renderGlobal != null) mc.renderGlobal.loadRenderers();
        if (instance != null) instance.renderedRevision = -1;
    }

    private static void tell(Minecraft mc, String message) {
        if (mc.player != null) mc.player.sendMessage(new TextComponentString("[X-Ray Deluxe] " + message));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote && fullbright) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.gameSettings.gammaSetting = previousGamma;
            fullbright = false;
        }
    }
}
