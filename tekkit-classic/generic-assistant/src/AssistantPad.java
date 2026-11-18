import net.java.games.input.*;
import java.util.*;
import net.minecraft.client.Minecraft;

final class AssistantPad {
    private Controller controller;
    String preferredDevice="";
    String status(){return controller==null ? "No controller detected" : controller.getName();}
    String axes(){
        if(controller==null)return "";StringBuilder out=new StringBuilder();
        for(Component c:controller.getComponents())if(c.isAnalog()){if(out.length()>0)out.append(" ");out.append(c.getIdentifier().getName());}
        return out.toString();
    }
    void reconnect(){release();controller=null;retry=0;}
    private long retry;
    private final Set<Integer> held=new HashSet<Integer>();
    private final boolean[] previous=new boolean[16];
    private final MenuScroll menuScroll=new MenuScroll();
    float deadzone=.2f;
    boolean using;
    String moveX="x",moveY="y",lookX="rx",lookY="ry",triggerAxis="z";
    float lookSpeed=5f, cursorSpeed=14f;
    boolean invertLookY;
    String triggerMode="combined",attackAxis="rz",useAxis="z";
    boolean menuArmed;
    private final FrameMotion frameMotion=new FrameMotion();
    private Object framePlayer,frameWorld;
    private float expectedYaw,expectedPitch;
    private boolean haveExpectedRotation;
    float triggerRest=-1f;
    private float rawAxis(String name){for(Component c:controller.getComponents())if(c.getIdentifier().getName().equals(name))return c.getPollData();return triggerRest;}
    static boolean triggerPressed(float value,float rest){return (value-rest)/(1f-rest)>.35f;}
    private boolean physical(int code){return code<0 ? org.lwjgl.input.Mouse.isButtonDown(code+100) : org.lwjgl.input.Keyboard.isKeyDown(code);}

    java.awt.Robot robot;
    private final MenuInput menuInput=new MenuInput();
    void configure(Properties p){
        preferredDevice=setting(p,"deviceName","");
        moveX=setting(p,"axis.moveX","x");moveY=setting(p,"axis.moveY","y");
        lookX=setting(p,"axis.lookX","rx");lookY=setting(p,"axis.lookY","ry");
        triggerAxis=setting(p,"axis.triggers","z");
        triggerMode=setting(p,"triggerMode","combined");
        attackAxis=setting(p,"axis.attack","rz");useAxis=setting(p,"axis.use","z");
        try{triggerRest=Float.parseFloat(setting(p,"triggerRest","-1"));if(triggerRest!=-1f && triggerRest!=0f)triggerRest=-1f;}catch(NumberFormatException ignored){}

        try{deadzone=Math.max(.05f,Math.min(.8f,Float.parseFloat(setting(p,"deadzone","0.20"))));lookSpeed=Math.max(.1f,Math.min(30f,Float.parseFloat(setting(p,"lookSpeed","5"))));}catch(NumberFormatException ignored){}
        try{cursorSpeed=Math.max(1f,Math.min(40f,Float.parseFloat(setting(p,"cursorSpeed","14"))));}catch(NumberFormatException ignored){}
        invertLookY=Boolean.parseBoolean(setting(p,"invertLookY","false"));
    }
    private String setting(Properties p,String key,String fallback){String v=p.getProperty(key,fallback);p.setProperty(key,v);return v;}
    private int mappedButton(int logical){return buttonMap[logical];}
    private int[] buttonMap={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    void configureButtons(Properties p){for(int i=0;i<10;i++){try{buttonMap[i]=Integer.parseInt(setting(p,"button."+i,String.valueOf(i)));}catch(NumberFormatException ignored){}}}
    private void releaseMenu(){
        menuScroll.reset();
        menuInput.reset();
        menuArmed=false;
    }
    private void menu(Minecraft mc){
        vp current=mc.s;
        int x=org.lwjgl.input.Mouse.getX()*current.q/mc.d;
        int y=current.r-org.lwjgl.input.Mouse.getY()*current.r/mc.e-1;
        menuInput.update(mc,current,x,y,button(0),button(2),button(1));
        menuArmed=menuInput.armed();
        if(!menuArmed || mc.s!=current)return;
        int scroll=menuScroll.poll(axis(lookY),System.nanoTime()/1000000L);
        if(button(4)&&!previous[4])scroll--;
        if(button(5)&&!previous[5])scroll++;
        if(scroll!=0)try{
            if(robot==null)robot=new java.awt.Robot();
            robot.mouseWheel(scroll);
        }catch(Exception error){ModLoader.getLogger().warning("Generic Assistant scroll: "+error);}
        for(int i=0;i<previous.length;i++)previous[i]=button(i);
    }
    void renderFrame(Minecraft mc){
        if(mc==null || !pollController()){
            frameMotion.reset();haveExpectedRotation=false;return;
        }
        if(framePlayer!=mc.h || frameWorld!=mc.f){
            framePlayer=mc.h;frameWorld=mc.f;frameMotion.reset();haveExpectedRotation=false;
        }
        float scale=frameMotion.step(System.nanoTime());
        if(mc.s!=null){
            haveExpectedRotation=false;controllerLook=false;
            for(Integer code:held)afu.a(code,physical(code));held.clear();using=false;
            menu(mc);
            if(mc.s==null || !menuArmed)return;
            try{
                if(robot==null)robot=new java.awt.Robot();
                java.awt.Point point=java.awt.MouseInfo.getPointerInfo().getLocation();
                int dx=frameMotion.pixelsX(axis(moveX)*cursorSpeed*scale);
                int dy=frameMotion.pixelsY(axis(moveY)*cursorSpeed*scale);
                if(dx!=0 || dy!=0)robot.mouseMove(point.x+dx,point.y+dy);
            }catch(Exception error){ModLoader.getLogger().warning("Generic Assistant cursor: "+error);}
            return;
        }
        releaseMenu();
        if(!org.lwjgl.opengl.Display.isActive() || mc.h==null || mc.f==null){
            frameMotion.reset();haveExpectedRotation=false;return;
        }
        boolean mouseLook=haveExpectedRotation && (Math.abs(angleDelta(mc.h.u,expectedYaw))>.01f || Math.abs(mc.h.v-expectedPitch)>.01f);
        float horizontal=axis(lookX),vertical=axis(lookY);
        if(mouseLook)controllerLook=false;
        if(Math.abs(horizontal)>.03f || Math.abs(vertical)>.03f)controllerLook=true;
        if(controllerLook && !mouseLook){
            float yaw=horizontal*lookSpeed*scale;
            float pitch=Math.max(-90f,Math.min(90f,mc.h.v+vertical*lookSpeed*scale*(invertLookY?-1f:1f)))-mc.h.v;
            mc.h.u+=yaw;mc.h.w+=yaw;
            mc.h.v+=pitch;mc.h.x+=pitch;
        }
        expectedYaw=mc.h.u;expectedPitch=mc.h.v;haveExpectedRotation=true;
    }
    private float axis(String name){
        for(Component c:controller.getComponents()) if(c.getIdentifier().getName().equals(name)) return filter(c.getPollData(),deadzone);
        return 0;
    }
    static float filter(float value,float zone){return Math.abs(value)<=zone?0:Math.signum(value)*(Math.abs(value)-zone)/(1-zone);}
    private boolean button(int index){
        for(Component c:controller.getComponents()) if(c.getIdentifier() instanceof Component.Identifier.Button && c.getIdentifier().getName().equals(String.valueOf(mappedButton(index)))) return c.getPollData()>.5f;
        return false;
    }
    private void key(afu binding,boolean down){
        int code=binding.d;
        if(down){if(held.add(code)) afu.a(code);afu.a(code,true);}
        else if(held.remove(code)) afu.a(code,physical(code));
    }
    void release(){releaseMenu();using=false;for(Integer code:held) afu.a(code,physical(code));held.clear();Arrays.fill(previous,false);}
    private boolean pollController(){
        if(controller==null && System.currentTimeMillis()>retry){
            retry=System.currentTimeMillis()+3000;
            try {for(Controller candidate:ControllerEnvironment.getDefaultEnvironment().getControllers())
                if((candidate.getType()==Controller.Type.GAMEPAD || candidate.getType()==Controller.Type.STICK) && (preferredDevice.length()==0 || candidate.getName().toLowerCase(java.util.Locale.ROOT).contains(preferredDevice.toLowerCase(java.util.Locale.ROOT)))){controller=candidate;ModLoader.getLogger().info("Generic Assistant: "+candidate.getName());break;}}
            catch(Throwable ex){ModLoader.getLogger().warning("Generic Assistant: "+ex);}
        }
        if(controller==null)return false;
        if(!controller.poll()){release();controller=null;return false;}
        return true;
    }
    boolean update(Minecraft mc){
        if(!pollController())return false;
        if(mc==null){release();return false;}
        if(mc.s!=null)return false;
        // Don't inject gameplay keys while the game is unfocused, but keep
        // menu navigation alive so the controller can focus/start the game.
        if(!org.lwjgl.opengl.Display.isActive()){release();return false;}
        releaseMenu();
        if(mc.h==null){release();return false;}
        float x=axis(moveX),y=axis(moveY);
        key(mc.A.n,y<-.25);key(mc.A.p,y>.25);key(mc.A.o,x<-.25);key(mc.A.q,x>.25);
        key(mc.A.r,button(0) || (mod_GenericAssistant.autoJump && mc.h.z && mc.h.A && y<-.25f));key(mc.A.v,button(1));
        float trigger=axis(triggerAxis);boolean split="split".equalsIgnoreCase(triggerMode);
        boolean attack=split ? triggerPressed(rawAxis(attackAxis),triggerRest) : trigger<-.25f;
        using=split ? triggerPressed(rawAxis(useAxis),triggerRest) : trigger>.25f;
        key(mc.A.w,attack);key(mc.A.x,using);
        key(mc.A.s,button(3));key(mc.A.t,button(2));
        if(button(4)&&!previous[4])mc.h.ap.c=(mc.h.ap.c+8)%9;
        if(button(5)&&!previous[5])mc.h.ap.c=(mc.h.ap.c+1)%9;
        if(button(9)&&!previous[9])mod_GenericAssistant.instance.openSettings(mc);
        else if(button(7)&&!previous[7])mc.i();
        if(button(6)&&!previous[6])toggleXray(mc);
        for(int i=0;i<previous.length;i++)previous[i]=button(i);
        return attack;
    }
    private void toggleXray(Minecraft mc){
        try {Class<?> type=Class.forName("mod_XRayDeluxe");java.lang.reflect.Field active=type.getDeclaredField("enabled");active.setAccessible(true);java.lang.reflect.Method toggle=type.getDeclaredMethod("setEnabled",Minecraft.class,boolean.class);toggle.setAccessible(true);toggle.invoke(null,mc,!active.getBoolean(null));}
        catch(Exception ex){ModLoader.getLogger().warning("Generic Assistant X-Ray integration: "+ex.getClass().getSimpleName());}
    }
    private boolean controllerLook;
    private static float angleDelta(float value,float expected){
        float delta=value-expected;
        while(delta>180f)delta-=360f;
        while(delta< -180f)delta+=360f;
        return delta;
    }
}
