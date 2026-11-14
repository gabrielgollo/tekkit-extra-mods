import net.minecraft.client.Minecraft;
import java.util.Properties;
import java.io.*;

/** Minecraft 1.2.5 client assistance; tool selection uses item effectiveness. */
public final class mod_ControllerAssistant extends BaseMod {
    static boolean autoTool=true, autoJump=false, autoTreetap=true;
    private boolean jumping;
    static mod_ControllerAssistant instance;
    private Properties settings=new Properties();
    private boolean menuKey;
    private boolean titleMouseDown;
    private int previousSlot=-1;
    final AssistantPad pad=new AssistantPad();
    public String getVersion(){return "0.1.0-mc1.2.5";}
    public String getName(){return "gabrielgollo_Controller Assistant";}
    public void load(){
        instance=this;
        Properties p=settings;
        File file=new File("config/controller-assistant.properties");
        try {
            if(file.isFile()){FileInputStream in=new FileInputStream(file);p.load(in);in.close();}
            pad.configure(p);pad.configureButtons(p);
            autoTool=Boolean.parseBoolean(p.getProperty("autoTool","true"));
            autoTreetap=Boolean.parseBoolean(p.getProperty("autoTreetap","true"));
            p.setProperty("autoTreetap",String.valueOf(autoTreetap));
            autoJump=Boolean.parseBoolean(p.getProperty("autoJump","false"));
            p.setProperty("autoTool",String.valueOf(autoTool));p.setProperty("autoJump",String.valueOf(autoJump));
            file.getParentFile().mkdirs();FileOutputStream out=new FileOutputStream(file);p.store(out,"Controller Assistant");out.close();
        }catch(Exception ex){ModLoader.getLogger().warning("Controller Assistant config: "+ex);}
        ModLoader.setInGameHook(this,true,true);
        ModLoader.setInGUIHook(this,true,true);
    }
    public boolean onTickInGUI(float partial,Minecraft mc,vp gui){
        releaseJump(mc);pad.update(mc);
        if(gui instanceof xt){
            drawTitleButton(gui,mc);
            boolean down=org.lwjgl.input.Mouse.isButtonDown(0);
            if(down&&!titleMouseDown){
                int x=org.lwjgl.input.Mouse.getX()*gui.q/mc.d;
                int y=gui.r-org.lwjgl.input.Mouse.getY()*gui.r/mc.e-1;
                if(x>=8&&x<=158&&y>=gui.r-31&&y<=gui.r-8)openSettings(mc);
            }
            titleMouseDown=down;
        }else titleMouseDown=false;
        return true;
    }
    private void drawTitleButton(vp gui,Minecraft mc){
        int x=8,y=gui.r-31;
        int mx=org.lwjgl.input.Mouse.getX()*gui.q/mc.d;
        int my=gui.r-org.lwjgl.input.Mouse.getY()*gui.r/mc.e-1;
        boolean hover=mx>=x&&mx<=x+150&&my>=y&&my<=y+23;
        oo.a(x,y,x+150,y+23,hover?0xE0455D73:0xE02C3948);
        gui.b(mc.q,text(mc,"Controller Assistant","Controller Assistant"),x+9,y+7,0xFFFFFF);
    }
    void releaseJump(Minecraft mc){if(jumping && mc!=null && mc.A!=null){afu.a(mc.A.r.d,false);jumping=false;}}
    void saveSettings(){
        settings.setProperty("autoTool",String.valueOf(autoTool));settings.setProperty("autoJump",String.valueOf(autoJump));settings.setProperty("autoTreetap",String.valueOf(autoTreetap));
        try{FileOutputStream out=new FileOutputStream("config/controller-assistant.properties");settings.store(out,"Controller Assistant");out.close();}catch(IOException ex){ModLoader.getLogger().warning(ex.toString());}
    }
    static String text(Minecraft mc,String en,String pt){return mc!=null && mc.A!=null && "pt_BR".equalsIgnoreCase(mc.A.R)?pt:en;}
    void openSettings(Minecraft mc){releaseJump(mc);pad.release();mc.a(new AssistantGui());}

    public boolean onTickInGame(float partial,Minecraft mc){
        boolean key=org.lwjgl.input.Keyboard.isKeyDown(66);
        if(key && !menuKey && mc!=null && mc.s==null) openSettings(mc);
        menuKey=key;
        boolean padAttack=pad.update(mc);
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
                }catch(Exception ex){ModLoader.getLogger().fine("Controller Assistant: electric tool energy unavailable");}
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
