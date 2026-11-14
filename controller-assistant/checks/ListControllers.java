import net.java.games.input.*;
public class ListControllers {
 public static void main(String[] args){
  Controller[] devices=ControllerEnvironment.getDefaultEnvironment().getControllers();
  System.out.println("Devices: "+devices.length);
  for(Controller device:devices){System.out.println(device.getType()+": "+device.getName());device.poll();for(Component c:device.getComponents())System.out.println("  "+c.getIdentifier().getName()+" "+c.getName()+" neutral="+c.getPollData());}
 }
}
