public class AssistanceCheck {
 public static void main(String[] args){
  if(AssistantPad.filter(.1f,.2f)!=0 || AssistantPad.filter(-.1f,.2f)!=0 || AssistantPad.filter(1,.2f)!=1)throw new AssertionError("deadzone");
  if(!mod_ControllerAssistant.wetResinFace(3,3)||mod_ControllerAssistant.wetResinFace(9,3)||mod_ControllerAssistant.wetResinFace(3,4))throw new AssertionError("resin safety");
  if(AssistantPad.triggerPressed(-1,-1)||!AssistantPad.triggerPressed(1,-1)||AssistantPad.triggerPressed(0,0)||!AssistantPad.triggerPressed(1,0))throw new AssertionError("split triggers");
  aan[] bar=new aan[9];bar[1]=new aan(256,1,0);bar[2]=new aan(257,1,0);bar[3]=new aan(258,1,0);
  if(mod_ControllerAssistant.bestTool(bar,0,pb.m[3])!=1)throw new AssertionError("dirt shovel");
  if(mod_ControllerAssistant.bestTool(bar,0,pb.m[1])!=2)throw new AssertionError("stone pickaxe");
  if(mod_ControllerAssistant.bestTool(bar,0,pb.m[17])!=3)throw new AssertionError("wood axe");
  AssistantGui gui=new AssistantGui();gui.q=320;gui.r=240;
  mod_ControllerAssistant.autoTool=true;mod_ControllerAssistant.autoJump=false;mod_ControllerAssistant.autoTreetap=true;
  gui.a(40,45,0);if(mod_ControllerAssistant.autoTool)throw new AssertionError("auto tool setting");
  gui.a(40,75,0);if(!mod_ControllerAssistant.autoJump)throw new AssertionError("auto jump setting");
  gui.a(40,105,0);if(mod_ControllerAssistant.autoTreetap)throw new AssertionError("treetap setting");
  System.out.println("PASS: settings button hit areas");
  System.out.println("PASS: stick deadzone, wet/dry resin face, shovel/pickaxe/axe hotbar selection");
 }
}
