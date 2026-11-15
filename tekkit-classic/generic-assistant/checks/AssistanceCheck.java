public class AssistanceCheck {
 public static void main(String[] args){
  if(AssistantPad.filter(.1f,.2f)!=0 || AssistantPad.filter(-.1f,.2f)!=0 || AssistantPad.filter(1,.2f)!=1)throw new AssertionError("deadzone");
  if(!mod_GenericAssistant.wetResinFace(3,3)||mod_GenericAssistant.wetResinFace(9,3)||mod_GenericAssistant.wetResinFace(3,4))throw new AssertionError("resin safety");
  if(AssistantPad.triggerPressed(-1,-1)||!AssistantPad.triggerPressed(1,-1)||AssistantPad.triggerPressed(0,0)||!AssistantPad.triggerPressed(1,0))throw new AssertionError("split triggers");
  aan[] bar=new aan[9];bar[1]=new aan(256,1,0);bar[2]=new aan(257,1,0);bar[3]=new aan(258,1,0);
  if(mod_GenericAssistant.bestTool(bar,0,pb.m[3])!=1)throw new AssertionError("dirt shovel");
  if(mod_GenericAssistant.bestTool(bar,0,pb.m[1])!=2)throw new AssertionError("stone pickaxe");
  if(mod_GenericAssistant.bestTool(bar,0,pb.m[17])!=3)throw new AssertionError("wood axe");
  AssistantGui gui=new AssistantGui();gui.q=320;gui.r=240;
  mod_GenericAssistant.autoTool=true;mod_GenericAssistant.autoJump=false;mod_GenericAssistant.autoTreetap=true;
  gui.a(40,45,0);if(mod_GenericAssistant.autoTool)throw new AssertionError("auto tool setting");
  gui.a(40,69,0);if(!mod_GenericAssistant.autoJump)throw new AssertionError("auto jump setting");
  gui.a(40,93,0);if(mod_GenericAssistant.autoTreetap)throw new AssertionError("treetap setting");
  mod_GenericAssistant.refillHotbar=true;mod_GenericAssistant.replaceBrokenTool=true;
  gui.a(40,117,0);if(mod_GenericAssistant.refillHotbar)throw new AssertionError("refill option");
  gui.a(40,141,0);if(mod_GenericAssistant.replaceBrokenTool)throw new AssertionError("replacement option");
  aan[] inventory=new aan[36];inventory[0]=new aan(35,64,2);inventory[9]=new aan(35,64,1);inventory[10]=new aan(35,32,2);
  if(HotbarAssistant.replacement(inventory,new aan(35,1,2))!=10)throw new AssertionError("variant-safe refill");
  inventory[10]=null;if(HotbarAssistant.replacement(inventory,new aan(35,1,2))!=-1)throw new AssertionError("do not search other hotbar slots");
  inventory[11]=new aan(257,1,20);if(HotbarAssistant.replacement(inventory,new aan(257,1,249))!=11)throw new AssertionError("worn replacement pickaxe");
  System.out.println("PASS: inventory replacement candidates and five settings button hit areas");
  System.out.println("PASS: stick deadzone, wet/dry resin face, shovel/pickaxe/axe hotbar selection");
 }
}
