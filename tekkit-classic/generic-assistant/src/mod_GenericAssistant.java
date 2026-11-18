import net.minecraft.client.Minecraft;
import java.util.Properties;
import java.io.*;

public final class mod_GenericAssistant extends BaseMod {
    static boolean autoTool=true, autoJump=false, autoTreetap=true;
    static boolean refillHotbar=true, replaceBrokenTool=true;
    private final HotbarAssistant hotbar=new HotbarAssistant();
    private boolean jumping;
    static mod_GenericAssistant instance;
    private Properties settings=new Properties();
    private boolean menuKey;
    private int previousSlot=-1;
    final AssistantPad pad=new AssistantPad();
    public String getVersion(){return "1.0.0-mc1.2.5";}
    public String getName(){return "gabrielgollo_Generic Assistant";}
    public void load(){
        instance=this;
        Properties p=settings;
        File file=new File("config/generic-assistant.properties");
        try {
            File legacy=new File("config/controller-assistant.properties");
            if(!file.isFile() && legacy.isFile()) java.nio.file.Files.copy(legacy.toPath(),file.toPath());
            if(file.isFile()){FileInputStream in=new FileInputStream(file);p.load(in);in.close();}
            pad.configure(p);pad.configureButtons(p);
            refillHotbar=Boolean.parseBoolean(p.getProperty("refillHotbar","true"));
            replaceBrokenTool=Boolean.parseBoolean(p.getProperty("replaceBrokenTool","true"));
            p.setProperty("refillHotbar",String.valueOf(refillHotbar));
            p.setProperty("replaceBrokenTool",String.valueOf(replaceBrokenTool));
            autoTool=Boolean.parseBoolean(p.getProperty("autoTool","true"));
            autoTreetap=Boolean.parseBoolean(p.getProperty("autoTreetap","true"));
            p.setProperty("autoTreetap",String.valueOf(autoTreetap));
            autoJump=Boolean.parseBoolean(p.getProperty("autoJump","false"));
            p.setProperty("autoTool",String.valueOf(autoTool));p.setProperty("autoJump",String.valueOf(autoJump));
            file.getParentFile().mkdirs();FileOutputStream out=new FileOutputStream(file);p.store(out,"Generic Assistant");out.close();
        }catch(Exception ex){ModLoader.getLogger().warning("Generic Assistant config: "+ex);}
        cpw.mods.fml.common.FMLCommonHandler.instance().registerTickHandler(new AssistantRenderTicker(this));
        ModLoader.setInGameHook(this,true,true);
        ModLoader.setInGUIHook(this,true,true);
    }
    public boolean onTickInGUI(float partial,Minecraft mc,vp gui){
        if(gui!=null){hotbar.reset();releaseJump(mc);}
        return true;
    }
    @SuppressWarnings("unchecked")
    void updateTitleScreen(Minecraft mc){
        if(mc==null || !(mc.s instanceof xt))return;
        for(Object button:mc.s.s)if(button instanceof AssistantTitleButton)return;
        mc.s.s.add(new AssistantTitleButton(this,mc.s.r-31));
    }
    void releaseJump(Minecraft mc){if(jumping && mc!=null && mc.A!=null){afu.a(mc.A.r.d,false);jumping=false;}}
    void saveSettings(){
        settings.setProperty("lookSpeed",String.valueOf(pad.lookSpeed));
        settings.setProperty("deadzone",String.valueOf(pad.deadzone));
        settings.setProperty("cursorSpeed",String.valueOf(pad.cursorSpeed));
        settings.setProperty("invertLookY",String.valueOf(pad.invertLookY));
        settings.setProperty("refillHotbar",String.valueOf(refillHotbar));
        settings.setProperty("replaceBrokenTool",String.valueOf(replaceBrokenTool));
        settings.setProperty("autoTool",String.valueOf(autoTool));settings.setProperty("autoJump",String.valueOf(autoJump));settings.setProperty("autoTreetap",String.valueOf(autoTreetap));
        try{FileOutputStream out=new FileOutputStream("config/generic-assistant.properties");settings.store(out,"Generic Assistant");out.close();}catch(IOException ex){ModLoader.getLogger().warning(ex.toString());}
    }
    static String text(Minecraft mc,String en,String pt){return mc!=null && mc.A!=null && "pt_BR".equalsIgnoreCase(mc.A.R)?pt:en;}
    void openSettings(Minecraft mc){releaseJump(mc);pad.release();mc.a(new AssistantGui());}

    public boolean onTickInGame(float partial,Minecraft mc){
        boolean key=org.lwjgl.input.Keyboard.isKeyDown(66);
        if(key && !menuKey && mc!=null && mc.s==null) openSettings(mc);
        menuKey=key;
        boolean padAttack=pad.update(mc);
        hotbar.update(mc,padAttack || pad.using || org.lwjgl.input.Mouse.isButtonDown(0) || org.lwjgl.input.Mouse.isButtonDown(1),mc!=null && mc.A!=null && (mc.A.t.e || org.lwjgl.input.Keyboard.isKeyDown(mc.A.t.d)));
        if(mc==null||mc.h==null||mc.f==null||mc.s!=null){releaseJump(mc);return true;}
        if(autoTool && (padAttack || org.lwjgl.input.Mouse.isButtonDown(0)) && mc.z!=null && mc.z.g==null){
            int id=mc.f.a(mc.z.b,mc.z.c,mc.z.d);
            if(id>0 && pb.m[id]!=null){
                pb block=pb.m[id];
                int slot=bestTool(mc.h.ap.a,mc.h.ap.c,block,mc.f.e(mc.z.b,mc.z.c,mc.z.d));
                if(slot!=mc.h.ap.c){previousSlot=mc.h.ap.c;mc.h.ap.c=slot;}
            }
        }
        if(autoJump && mc.h.z && mc.h.A && mc.A.n.e && !mc.A.v.e) {
            afu.a(mc.A.r.d,true); jumping=true;
        } else if(jumping) {
            afu.a(mc.A.r.d,org.lwjgl.input.Keyboard.isKeyDown(mc.A.r.d));jumping=false;
        }
        if(autoTreetap && (pad.using || org.lwjgl.input.Mouse.isButtonDown(1)) && mc.z!=null && mc.z.g==null) {
            int id=mc.f.a(mc.z.b,mc.z.c,mc.z.d);
            pb block=id>0?pb.m[id]:null;
            int metadata=mc.f.e(mc.z.b,mc.z.c,mc.z.d);
            if(block!=null && block.getClass().getName().equals("ic2.common.BlockRubWood") && wetResinFace(metadata,mc.z.e)) {
                int slot=treetapSlot(mc.h.ap.a);
                if(slot>=0)mc.h.ap.c=slot;
            }
        }
        return true;
    }
    static boolean wetResinFace(int metadata,int face){return metadata>=2 && metadata<6 && metadata==face;}
    static int treetapSlot(aan[] inventory){
        for(int slot=0;slot<9 && slot<inventory.length;slot++) {
            if(inventory[slot]==null)continue;
            String type=inventory[slot].a().getClass().getName();
            if(type.equals("ic2.common.ItemTreetap"))return slot;
            if(type.equals("ic2.common.ItemTreetapElectric")) {
                try {
                    Class<?> electric=Class.forName("ic2.common.ElectricItem");
                    if(Boolean.TRUE.equals(electric.getMethod("canUse",aan.class,int.class).invoke(null,inventory[slot],50)))return slot;
                }catch(Exception ex){ModLoader.getLogger().fine("Generic Assistant: electric tool energy unavailable");}
            }
        }
        return -1;
    }
    static float toolStrength(aan stack,pb block,int metadata){
        yr item=stack.a();
        if(item.getClass().getName().startsWith("ic2."))try {
            Class<?> tool=Class.forName("ic2.common.ItemElectricTool");
            if(tool.isInstance(item)) {
                int cost=tool.getField("operationEnergyCost").getInt(item);
                boolean charged=Boolean.TRUE.equals(Class.forName("ic2.common.ElectricItem").getMethod("canUse",aan.class,int.class).invoke(null,stack,cost));
                if(!charged)return 1f;
                java.util.Set blocks=(java.util.Set)tool.getField("mineableBlocks").get(item);
                return blocks.contains(block)||item.a(block) ? item.getClass().getField("a").getFloat(item) : 1f;
            }
        }catch(Exception ignored){}
        return item.getStrVsBlock(stack,block,metadata);
    }
    static int bestTool(aan[] inventory,int current,pb block){return bestTool(inventory,current,block,0);}
    static int bestTool(aan[] inventory,int current,pb block,int metadata){
        int best=current;float strength=inventory[current]==null?1f:toolStrength(inventory[current],block,metadata);
        for(int slot=0;slot<9 && slot<inventory.length;slot++){
            aan item=inventory[slot];if(item==null)continue;
            float candidate=toolStrength(item,block,metadata);
            if(candidate>strength){best=slot;strength=candidate;}
        }
        return best;
    }
}
