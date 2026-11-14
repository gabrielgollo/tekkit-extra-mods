import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/** Client-side X-Ray Deluxe for the Tekkit Classic 1.2.5 / FML 2.2 runtime. */
public final class mod_XRayDeluxe extends BaseMod implements forge.IRenderWorldLastHandler {
    static boolean enabled;
    static boolean fullbright;
    private static float previousGamma;
    static int radius = 12;
    static int hiddenOpacity;
    private static final String HIDDEN_ATLAS = "/xray-hidden.png";
    static int[] oreIds = new int[] {14, 15, 16, 21, 56, 73, 74, 135, 140, 247, 248, 249};
    private static int[] originalTextures;
    private static boolean[] originalDefaultAtlas;
    private boolean wasToggle, wasMenu;
    private static final int KEY_TOGGLE = 45, KEY_MENU = 46; // X / C

    public String getVersion() { return "1.0.0-mc1.2.5"; }
    public String getName() { return "X-Ray Deluxe"; }

    public void load() {
        readConfig();
        forge.MinecraftForgeClient.registerRenderContextHandler(HIDDEN_ATLAS, 0, new forge.IRenderContextHandler() {
            public void beforeRenderContext() {
                org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);
                org.lwjgl.opengl.GL11.glEnable(3042);
                org.lwjgl.opengl.GL14.glBlendColor(1,1,1,hiddenOpacity/100f);
                org.lwjgl.opengl.GL11.glBlendFunc(32771,32772);
            }
            public void afterRenderContext() { org.lwjgl.opengl.GL11.glPopAttrib(); }
        });
        forge.MinecraftForgeClient.registerRenderLastHandler(this);
        ModLoader.setInGameHook(this, true, false);
    }

    public boolean onTickInGame(float partialTick, Minecraft mc) {
        if (mc == null || mc.f == null || mc.h == null) return true;
        if (mc.s != null) { wasToggle = keyDown(KEY_TOGGLE); wasMenu = keyDown(KEY_MENU); return true; }
        boolean toggle = keyDown(KEY_TOGGLE);
        boolean menu = keyDown(KEY_MENU);
        if (toggle && !wasToggle) {
            enabled = !enabled;
            applyXray(mc, enabled);
            tell(mc, "X-Ray Deluxe " + (enabled ? tr(mc,"enabled","ativado") : tr(mc,"disabled","desativado")) + " (X)");
        }
        if (menu && !wasMenu) ModLoader.openGUI(mc.h, new XRayDeluxeGui());
        wasToggle = toggle; wasMenu = menu;
        return true;
    }

    private static long selectionRevision;
    private long renderedRevision = -1;
    private xd renderedWorld;
    private int[][] overlay = new int[0][4];
    private long nextScan;
    public void onRenderWorldLast(l renderer, float partial) {
        Minecraft mc = ModLoader.getMinecraftInstance();
        if (!enabled || mc == null || mc.h == null || mc.f == null) return;
        if (renderedWorld != mc.f || renderedRevision != selectionRevision) { renderedWorld = mc.f; renderedRevision = selectionRevision; nextScan = 0; }
        if (System.currentTimeMillis() >= nextScan) { overlay = findOres(mc); nextScan = System.currentTimeMillis()+700; }
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);
        org.lwjgl.opengl.GL11.glPushMatrix();
        try {
            org.lwjgl.opengl.GL11.glTranslated(-(mc.h.l+(mc.h.o-mc.h.l)*partial), -(mc.h.m+(mc.h.p-mc.h.m)*partial), -(mc.h.n+(mc.h.q-mc.h.n)*partial));
            // Reserve a foreground depth range for X-Ray: bypass terrain depth,
            // but still let nearer highlighted blocks occlude farther ones.
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
            org.lwjgl.opengl.GL11.glDepthFunc(org.lwjgl.opengl.GL11.GL_LEQUAL);
            org.lwjgl.opengl.GL11.glDepthRange(0.0, 0.001);
            org.lwjgl.opengl.GL11.glEnable(3553);
            org.lwjgl.opengl.GL11.glDisable(2896);
            org.lwjgl.opengl.GL11.glDisable(3042);
            org.lwjgl.opengl.GL11.glEnable(2884);
            org.lwjgl.opengl.GL11.glDepthMask(true);
            for (int[] ore : overlay) {
                if(mc.f.a(ore[1],ore[2],ore[3]) != ore[0]) continue;
                pb block=pb.m[ore[0]];
                if(block==null) continue;
                org.lwjgl.opengl.GL11.glBindTexture(3553,mc.p.b(block.getTextureFile()));
                int meta=mc.f.e(ore[1],ore[2],ore[3]);
                for(int face=0;face<6;face++) {
                    int tile=block.a(face,meta);
                    double u=(tile%16)/16.0, v=(tile/16)/16.0, span=15.99/256.0;
                    float shade=face==1 ? 1f : (face==0 ? .55f : (face<4 ? .8f : .68f));
                    org.lwjgl.opengl.GL11.glColor3f(shade,shade,shade);
                    org.lwjgl.opengl.GL11.glBegin(7);
                    for(int corner=0;corner<4;corner++) {
                        org.lwjgl.opengl.GL11.glTexCoord2d(u+((corner==1||corner==2)?span:0),v+(corner>=2?span:0));
                        int[] point=FACES[face][corner];
                        org.lwjgl.opengl.GL11.glVertex3d(ore[1]+point[0],ore[2]+point[1],ore[3]+point[2]);
                    }
                    org.lwjgl.opengl.GL11.glEnd();
                }
            }
        } finally { org.lwjgl.opengl.GL11.glPopMatrix(); org.lwjgl.opengl.GL11.glPopAttrib(); }
    }

    private static final int[][][] FACES = {
        {{0,0,1},{0,0,0},{1,0,0},{1,0,1}},
        {{0,1,0},{0,1,1},{1,1,1},{1,1,0}},
        {{1,1,0},{1,0,0},{0,0,0},{0,1,0}},
        {{0,1,1},{0,0,1},{1,0,1},{1,1,1}},
        {{0,1,0},{0,0,0},{0,0,1},{0,1,1}},
        {{1,1,1},{1,0,1},{1,0,0},{1,1,0}}
    };

    static boolean isOre(int id) {
        for (int i = 0; i < oreIds.length; i++) if (oreIds[i] == id) return true;
        return false;
    }

    static void drawOreIcon(Minecraft mc, int id, int x, int y) {
        pb block=pb.m[id];
        if(block==null) return;
        int tile=block.bN;
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);
        try {
            if(originalTextures!=null) block.bN=originalTextures[id];
            tf.c();
            new tw().a(mc.q,mc.p,new aan(id,1,0),x,y);
        } finally {
            block.bN=tile;
            org.lwjgl.opengl.GL11.glPopAttrib();
        }
    }

    static boolean shouldHighlight(int id) {
        return id == 10 || id == 11 || isOre(id);
    }

    static final int[] choices = {14,15,16,21,56,73,135,140,247,248,249};
    static final String[] names = {"Gold", "Iron", "Coal", "Lapis Lazuli", "Diamond", "Redstone", "Nether Ores", "RedPower Ores (all variants)", "IC2 Uranium", "IC2 Tin", "IC2 Copper"};
    static final String[] portugueseNames = {"Ouro", "Ferro", "Carvao", "Lapis-lazuli", "Diamante", "Redstone", "Nether Ores", "Minerios RedPower (todas variantes)", "Uranio IC2", "Estanho IC2", "Cobre IC2"};
    static String localizeLanguage(String language, String english, String portuguese) {
        return "pt_BR".equalsIgnoreCase(language) ? portuguese : english;
    }
    static String tr(Minecraft mc, String english, String portuguese) {
        return localizeLanguage(mc != null && mc.A != null ? mc.A.R : null, english, portuguese);
    }

    static void toggleOre(Minecraft mc, int id) {
        java.util.ArrayList<Integer> ids = new java.util.ArrayList<Integer>();
        boolean selected = isOre(id);
        for (int n : oreIds) if (n != id && !(id == 73 && n == 74)) ids.add(Integer.valueOf(n));
        if (!selected) { ids.add(Integer.valueOf(id)); if (id == 73) ids.add(Integer.valueOf(74)); }
        oreIds = new int[ids.size()];
        for (int i=0; i<oreIds.length; i++) oreIds[i] = ids.get(i).intValue();
        writeConfig();
        selectionRevision++;
        if (enabled) applyXray(mc, true);
    }

    static void toggleFullbright(Minecraft mc) {
        if(mc == null || mc.A == null) return;
        if(!fullbright) { previousGamma=mc.A.O; mc.A.O=16f; fullbright=true; }
        else { mc.A.O=previousGamma; fullbright=false; }
    }

    static void refreshSelection(Minecraft mc) {
        selectionRevision++;
        writeConfig();
        if (enabled) applyXray(mc,true);
    }

    static void setEnabled(Minecraft mc, boolean value) {
        enabled = value;
        selectionRevision++;
        applyXray(mc, value);
        tell(mc, "X-Ray Deluxe " + (enabled ? tr(mc,"enabled","ativado") : tr(mc,"disabled","desativado")));
    }

    /** Swap every non-whitelisted block's atlas tile to vanilla's empty alpha tile. */
    private static void applyXray(Minecraft mc, boolean value) {
        try {
            pb[] blocks = pb.m;
            if (originalTextures == null) {
                originalTextures = new int[blocks.length];
                originalDefaultAtlas = new boolean[blocks.length];
                for (int id=0; id<blocks.length; id++) if (blocks[id] != null) { originalTextures[id] = blocks[id].bN; originalDefaultAtlas[id] = blocks[id].isDefaultTexture; }
            }
            for (int id=0; id<blocks.length; id++) {
                if (blocks[id] == null) continue;
                if (id == 8 || id == 9 || id == 10 || id == 11) continue;
                // Custom atlases (including both BuildCraft oil blocks) must never be rewritten.
                if (!"/terrain.png".equals(blocks[id].getTextureFile()) && !HIDDEN_ATLAS.equals(blocks[id].getTextureFile())) continue;
                blocks[id].setTextureFile(value && !isOre(id) && hiddenOpacity > 0 ? HIDDEN_ATLAS : "/terrain.png");
                if (!value || isOre(id) || hiddenOpacity == 0) blocks[id].isDefaultTexture = originalDefaultAtlas[id];
                if (value && hiddenOpacity == 0 && !isOre(id) && id != 8 && id != 9 && id != 10 && id != 11) blocks[id].bN = 180;
                else blocks[id].bN = originalTextures[id];
            }
            if (mc != null && mc.g != null) mc.g.a();
        } catch (Throwable ex) {
            if (mc != null) tell(mc, tr(mc,"Failed to update blocks: ","Falha ao atualizar os blocos: ") + ex.getClass().getSimpleName());
        }
    }

    static int[][] findOres(Minecraft mc) {
        java.util.ArrayList<int[]> found = new java.util.ArrayList<int[]>();
        if (mc == null || mc.f == null || mc.h == null) return new int[0][4];
        try {
            double px = mc.h.o, py = mc.h.p, pz = mc.h.q;
            int r = radius, cx = (int)Math.floor(px), cy = (int)Math.floor(py), cz = (int)Math.floor(pz);
            for (int x = cx-r; x <= cx+r; x++)
                for (int y = Math.max(0, cy-r); y <= Math.min(255, cy+r); y++)
                    for (int z = cz-r; z <= cz+r; z++) {
                        int id = mc.f.a(x,y,z);
                        if (shouldHighlight(id)) { found.add(new int[] {id,x,y,z}); }
                    }
            int[][] result = found.toArray(new int[found.size()][]);
            return result;
        } catch (Throwable ignored) { return new int[0][4]; }
    }

    private static double number(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getField(fieldName);
        return f.getDouble(target);
    }

    private static boolean keyDown(int key) {
        try { return ((Boolean)Class.forName("org.lwjgl.input.Keyboard").getMethod("isKeyDown", new Class[] {Integer.TYPE}).invoke(null, new Object[] {Integer.valueOf(key)})).booleanValue(); }
        catch (Throwable ignored) { return false; }
    }

    private static void tell(Minecraft mc, String text) {
        try { if (mc != null && mc.h != null) mc.h.a("[X-Ray Deluxe] " + text); } catch (Throwable ignored) { }
    }

    static int[] defaultOreIds() {
        File file = new File("config/x-ray-deluxe-defaults.json");
        try {
            if(!file.isFile()) {
                file.getParentFile().mkdirs();
                java.io.InputStream bundled=mod_XRayDeluxe.class.getResourceAsStream("/x-ray-deluxe-defaults.json");
                if(bundled!=null) {
                    java.nio.file.Files.copy(bundled,file.toPath()); bundled.close();
                }
            }
            if(!file.isFile()) return new int[] {14,15,16,21,56,73,74,135,140,247,248,249};
            String json=new String(java.nio.file.Files.readAllBytes(file.toPath()),"UTF-8");
            javax.script.ScriptEngine engine=new javax.script.ScriptEngineManager().getEngineByName("nashorn");
            if(engine==null) throw new IllegalStateException("Java 8 JSON parser unavailable");
            engine.put("defaultsJson",json);
            Object parsed=engine.eval("Java.asJSONCompatible(JSON.parse(defaultsJson))");
            Object values=((java.util.Map)parsed).get("oreIds");
            if(!(values instanceof java.util.List)) throw new IllegalArgumentException("oreIds must be an array");
            java.util.LinkedHashSet<Integer> ids=new java.util.LinkedHashSet<Integer>();
            for(Object value:(java.util.List)values) {
                if(!(value instanceof Number)) throw new IllegalArgumentException("oreIds must contain integer IDs");
                int id=((Number)value).intValue();
                if(id<1 || id>4095 || ((Number)value).doubleValue()!=id) throw new IllegalArgumentException("Invalid block ID: "+value);
                ids.add(Integer.valueOf(id));
            }
            if(ids.contains(73) || ids.contains(74)) { ids.add(73); ids.add(74); }
            int[] result=new int[ids.size()]; int index=0;
            for(Integer id:ids) result[index++]=id.intValue();
            return result;
        } catch(Exception ex) {
            System.err.println("[X-Ray Deluxe] Invalid defaults JSON; using built-in defaults: "+ex.getMessage());
            return new int[] {14,15,16,21,56,73,74,135,140,247,248,249};
        }
    }

    private static File configFile() { return new File("config/x-ray-deluxe.cfg"); }
    private static void readConfig() {
        Properties p = new Properties();
        try {
            File f = configFile();
            if (f.isFile()) { FileInputStream in = new FileInputStream(f); p.load(in); in.close(); }
            hiddenOpacity = clamp(Integer.parseInt(p.getProperty("hiddenOpacity", "0")),0,100);
            radius = clamp(Integer.parseInt(p.getProperty("radius", "12")), 4, 32);
            int[] defaults = defaultOreIds();
            StringBuilder defaultText=new StringBuilder();
            for(int id:defaults) { if(defaultText.length()>0) defaultText.append(','); defaultText.append(id); }
            String ids = p.getProperty("oreIds", defaultText.toString());
            String[] parts = ids.trim().length() == 0 ? new String[0] : ids.split(","); oreIds = new int[parts.length];
            for (int i=0; i<parts.length; i++) oreIds[i] = Integer.parseInt(parts[i].trim());
        } catch (Exception ignored) { }
        writeConfig();
    }
    static void writeConfig() {
        try {
            File f = configFile(); File parent = f.getParentFile(); if (parent != null) parent.mkdirs();
            Properties p = new Properties(); p.setProperty("radius", String.valueOf(radius)); p.setProperty("hiddenOpacity", String.valueOf(hiddenOpacity));
            StringBuffer ids = new StringBuffer(); for (int i=0; i<oreIds.length; i++) { if (i>0) ids.append(','); ids.append(oreIds[i]); }
            p.setProperty("oreIds", ids.toString()); FileOutputStream out = new FileOutputStream(f); p.store(out, "X-Ray Deluxe for Tekkit Classic / Minecraft 1.2.5"); out.close();
        } catch (Exception ignored) { }
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
