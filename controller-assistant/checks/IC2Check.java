public class IC2Check {
 public static void main(String[] args){
  pb stone=pb.m[1];
  aan[] bar=new aan[9];
  bar[5]=new aan(new ic2.common.ItemTreetap(29000,0));
  if(mod_ControllerAssistant.treetapSlot(bar)!=5)throw new AssertionError("normal treetap");
  bar[5]=null;bar[7]=new aan(new ic2.common.ItemTreetapElectric(29001,0));
  if(mod_ControllerAssistant.treetapSlot(bar)!=-1)throw new AssertionError("empty electric treetap skipped");
  ic2.common.ElectricItem.charge(bar[7],100,1,true,false);
  if(mod_ControllerAssistant.treetapSlot(bar)!=7)throw new AssertionError("electric treetap");
  bar[7]=null;
  if(mod_ControllerAssistant.treetapSlot(bar)!=-1)throw new AssertionError("missing treetap");
  ic2.common.ItemElectricToolDrill drill=new ic2.common.ItemElectricToolDrill(29002,0);drill.init();
  bar[1]=new aan(257,1,0);bar[2]=new aan(drill);
  if(mod_ControllerAssistant.bestTool(bar,0,pb.m[1])!=1)throw new AssertionError("discharged drill skipped");
  ic2.common.ElectricItem.charge(bar[2],1000,1,true,false);
  if(mod_ControllerAssistant.bestTool(bar,0,pb.m[1])!=2)throw new AssertionError("charged drill selected");
  ic2.common.Ic2Items.crop=new aan(pb.m[59]);
  ic2.common.ItemElectricToolChainsaw saw=new ic2.common.ItemElectricToolChainsaw(29003,0);saw.init();
  bar[3]=new aan(saw);bar[4]=new aan(258,1,0);
  if(mod_ControllerAssistant.bestTool(bar,0,pb.m[17])!=4)throw new AssertionError("discharged saw skipped");
  ic2.common.ElectricItem.charge(bar[3],1000,1,true,false);
  if(mod_ControllerAssistant.bestTool(bar,0,pb.m[17])!=3)throw new AssertionError("charged saw selected");
  System.out.println("PASS: real IC2 drill/chainsaw charge-aware selection");
  System.out.println("PASS: actual IC2 normal/electric treetap classes and absent tool");
 }
}
