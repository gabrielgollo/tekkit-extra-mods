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
    float deadzone=.2f;
    boolean using;
    String moveX="x",moveY="y",lookX="rx",lookY="ry",triggerAxis="z";
    float lookSpeed=5f;
    String triggerMode="combined",attackAxis="rz",useAxis="z";
    boolean menuWasOpen;
    boolean menuArmed;
    private float expectedYaw,expectedPitch;
    private boolean haveExpectedRotation;
    float triggerRest=-1f;
    private float rawAxis(String name){for(Component c:controller.getComponents())if(c.getIdentifier().getName().equals(name))return c.getPollData();return triggerRest;}
    static boolean triggerPressed(float value,float rest){return (value-rest)/(1f-rest)>.35f;}
    private boolean physical(int code){return code<0 ? org.lwjgl.input.Mouse.isButtonDown(code+100) : org.lwjgl.input.Keyboard.isKeyDown(code);}

    java.awt.Robot robot;
    boolean menuClick,menuRight;
    void configure(Properties p){
        preferredDevice=setting(p,"deviceName","");
        moveX=setting(p,"axis.moveX","x");moveY=setting(p,"axis.moveY","y");
        lookX=setting(p,"axis.lookX","rx");lookY=setting(p,"axis.lookY","ry");
        triggerAxis=setting(p,"axis.triggers","z");
        triggerMode=setting(p,"triggerMode","combined");
        attackAxis=setting(p,"axis.attack","rz");useAxis=setting(p,"axis.use","z");
        try{triggerRest=Float.parseFloat(setting(p,"triggerRest","-1"));if(triggerRest!=-1f && triggerRest!=0f)triggerRest=-1f;}catch(NumberFormatException ignored){}

        try{deadzone=Math.max(.05f,Math.min(.8f,Float.parseFloat(setting(p,"deadzone","0.20"))));lookSpeed=Math.max(.1f,Math.min(20f,Float.parseFloat(setting(p,"lookSpeed","5"))));}catch(NumberFormatException ignored){}
    }
    private String setting(Properties p,String key,String fallback){String v=p.getProperty(key,fallback);p.setProperty(key,v);return v;}
    private int mappedButton(int logical){return buttonMap[logical];}
    private int[] buttonMap={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    void configureButtons(Properties p){for(int i=0;i<10;i++){try{buttonMap[i]=Integer.parseInt(setting(p,"button."+i,String.valueOf(i)));}catch(NumberFormatException ignored){}}}
    private void releaseMenu(){
        if(robot!=null){if(menuClick)robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);if(menuRight)robot.mouseRelease(java.awt.event.InputEvent.BUTTON3_DOWN_MASK);}
        menuClick=false;menuRight=false;
    }
    private void menu(Minecraft mc){
        try{
            if(robot==null)robot=new java.awt.Robot();
            java.awt.Point point=java.awt.MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(point.x+Math.round(axis(moveX)*14),point.y+Math.round(axis(moveY)*14));
            boolean left=button(0),right=button(2);
            if(left!=menuClick){if(left)robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);else robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);menuClick=left;}
            if(right!=menuRight){if(right)robot.mousePress(java.awt.event.InputEvent.BUTTON3_DOWN_MASK);else robot.mouseRelease(java.awt.event.InputEvent.BUTTON3_DOWN_MASK);menuRight=right;}
            if(button(1)&&!previous[1]){robot.keyPress(java.awt.event.KeyEvent.VK_ESCAPE);robot.keyRelease(java.awt.event.KeyEvent.VK_ESCAPE);}
            if(button(4)&&!previous[4])robot.mouseWheel(-1);
            if(button(5)&&!previous[5])robot.mouseWheel(1);
        }catch(Exception ex){ModLoader.getLogger().warning("Controller Assistant menu: "+ex.getMessage());}
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
    boolean update(Minecraft mc){
        if(controller==null && System.currentTimeMillis()>retry){
            retry=System.currentTimeMillis()+3000;
            try {for(Controller candidate:ControllerEnvironment.getDefaultEnvironment().getControllers())
                if((candidate.getType()==Controller.Type.GAMEPAD || candidate.getType()==Controller.Type.STICK) && (preferredDevice.length()==0 || candidate.getName().toLowerCase(java.util.Locale.ROOT).contains(preferredDevice.toLowerCase(java.util.Locale.ROOT)))){controller=candidate;ModLoader.getLogger().info("Controller Assistant: "+candidate.getName());break;}}
            catch(Throwable ex){ModLoader.getLogger().warning("Controller Assistant: "+ex);}
        }
        if(controller==null)return false;
        if(!controller.poll()){release();controller=null;return false;}
        if(mc==null){release();return false;}
        if(mc.s!=null){
            for(Integer code:held)afu.a(code,physical(code));held.clear();using=false;
            if(!menuWasOpen){menuWasOpen=true;menuArmed=false;}
            if(!button(0)&&!button(1)&&!button(2))menuArmed=true;
            if(menuArmed)menu(mc);
            for(int i=0;i<previous.length;i++)previous[i]=button(i);return false;
        }
        // Don't inject gameplay keys while the game is unfocused, but keep
        // menu navigation alive so the controller can focus/start the game.
        if(!org.lwjgl.opengl.Display.isActive()){release();return false;}
        releaseMenu();menuWasOpen=false;
        if(mc.h==null){release();return false;}
        // Vanilla mouse look runs outside this mod. Detect its rotation so a
        // centered/drifting stick never competes with the mouse camera.
        boolean mouseLook=false;
        if(haveExpectedRotation){
            mouseLook=Math.abs(angleDelta(mc.h.u,expectedYaw))>.01f || Math.abs(mc.h.v-expectedPitch)>.01f;
        }
        float x=axis(moveX),y=axis(moveY);
        key(mc.A.n,y<-.25);key(mc.A.p,y>.25);key(mc.A.o,x<-.25);key(mc.A.q,x>.25);
        key(mc.A.r,button(0) || (mod_ControllerAssistant.autoJump && mc.h.z && mc.h.A && y<-.25f));key(mc.A.v,button(1));
        // JInput commonly exposes Xbox right stick as rx/ry and combined triggers as z.
        float lookHorizontal=axis(lookX),lookVertical=axis(lookY);
        if(mouseLook)controllerLook=false;
        if(Math.abs(lookHorizontal)>.03f || Math.abs(lookVertical)>.03f)controllerLook=true;
        if(controllerLook && !mouseLook){
            mc.h.u+=lookHorizontal*lookSpeed;
            mc.h.v=Math.max(-90,Math.min(90,mc.h.v+lookVertical*lookSpeed));
        }
        expectedYaw=mc.h.u;expectedPitch=mc.h.v;haveExpectedRotation=true;
        float trigger=axis(triggerAxis);boolean split="split".equalsIgnoreCase(triggerMode);
        boolean attack=split ? triggerPressed(rawAxis(attackAxis),triggerRest) : trigger<-.25f;
        using=split ? triggerPressed(rawAxis(useAxis),triggerRest) : trigger>.25f;
        key(mc.A.w,attack);key(mc.A.x,using);
        key(mc.A.s,button(3));key(mc.A.t,button(2));
        if(button(4)&&!previous[4])mc.h.ap.c=(mc.h.ap.c+8)%9;
        if(button(5)&&!previous[5])mc.h.ap.c=(mc.h.ap.c+1)%9;
        if(button(9)&&!previous[9])mod_ControllerAssistant.instance.openSettings(mc);
        else if(button(7)&&!previous[7])mc.i();
        if(button(6)&&!previous[6])toggleXray(mc);
        for(int i=0;i<previous.length;i++)previous[i]=button(i);
        return attack;
    }
    private void toggleXray(Minecraft mc){
        try {Class<?> type=Class.forName("mod_XRayDeluxe");java.lang.reflect.Field active=type.getDeclaredField("enabled");active.setAccessible(true);java.lang.reflect.Method toggle=type.getDeclaredMethod("setEnabled",Minecraft.class,boolean.class);toggle.setAccessible(true);toggle.invoke(null,mc,!active.getBoolean(null));}
        catch(Exception ex){ModLoader.getLogger().warning("Controller Assistant X-Ray integration: "+ex.getClass().getSimpleName());}
    }
    private boolean controllerLook;
    private static float angleDelta(float value,float expected){
        float delta=value-expected;
        while(delta>180f)delta-=360f;
        while(delta< -180f)delta+=360f;
        return delta;
    }
}
