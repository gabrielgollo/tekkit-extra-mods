import java.lang.reflect.*;
import java.util.*;
import net.java.games.input.*;

public class PadProfileCheck {
 static float stick=.6f, trigger=-1f;
 static boolean pressed;
 static Component component(final Component.Identifier id,final int value){
  return (Component)Proxy.newProxyInstance(Component.class.getClassLoader(),new Class[]{Component.class},new InvocationHandler(){
   public Object invoke(Object self,Method method,Object[] args){
    String name=method.getName();
    if(name.equals("getIdentifier"))return id;
    if(name.equals("getName"))return id.getName();
    if(name.equals("getPollData"))return value==0?stick:value==1?trigger:pressed?1f:0f;
    if(name.equals("isAnalog"))return value<2;
    if(name.equals("isRelative"))return false;
    if(name.equals("getDeadZone"))return 0f;
    return null;
   }
  });
 }
 public static void main(String[] args)throws Exception {
  final Component[] parts={component(Component.Identifier.Axis.X,0),component(Component.Identifier.Axis.RZ,1),component(Component.Identifier.Button._5,2)};
  Controller fake=(Controller)Proxy.newProxyInstance(Controller.class.getClassLoader(),new Class[]{Controller.class},new InvocationHandler(){
   public Object invoke(Object self,Method method,Object[] args){
    if(method.getName().equals("getComponents"))return parts;
    if(method.getName().equals("getName"))return "Simulated Z1E profile";
    if(method.getName().equals("poll"))return true;
    if(method.getName().equals("getType"))return Controller.Type.GAMEPAD;
    return null;
   }
  });
  AssistantPad pad=new AssistantPad();
  Field controller=AssistantPad.class.getDeclaredField("controller");controller.setAccessible(true);controller.set(pad,fake);
  Properties config=new Properties();config.setProperty("button.0","5");config.setProperty("axis.lookX","x");config.setProperty("triggerMode","split");
  pad.configure(config);pad.configureButtons(config);
  Method axis=AssistantPad.class.getDeclaredMethod("axis",String.class);axis.setAccessible(true);
  Method button=AssistantPad.class.getDeclaredMethod("button",int.class);button.setAccessible(true);
  Method raw=AssistantPad.class.getDeclaredMethod("rawAxis",String.class);raw.setAccessible(true);
  float movement=(Float)axis.invoke(pad,"x");
  if(Math.abs(movement-.5f)>.001)throw new AssertionError("axis scaling");
  stick=.1f;if((Float)axis.invoke(pad,"x")!=0)throw new AssertionError("stick drift");
  pressed=true;if(!((Boolean)button.invoke(pad,0)))throw new AssertionError("button remap");
  pressed=false;if((Boolean)button.invoke(pad,0))throw new AssertionError("button release");
  if(AssistantPad.triggerPressed((Float)raw.invoke(pad,"rz"),-1))throw new AssertionError("neutral trigger");
  trigger=1;if(!AssistantPad.triggerPressed((Float)raw.invoke(pad,"rz"),-1))throw new AssertionError("trigger press");
  if(!pad.status().contains("Simulated")||!pad.axes().contains("rz"))throw new AssertionError("diagnostics");
  System.out.println("PASS: simulated JInput axes, drift, remapped button press/release, split trigger and diagnostics");
 }
}
