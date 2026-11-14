public class PanelCheck {
 public static void main(String[] args) {
  XRayDeluxeGui gui=new XRayDeluxeGui(); gui.q=320; gui.r=240;
  mod_XRayDeluxe.enabled=false;
  mod_XRayDeluxe.oreIds=new int[] {14};
  gui.a(35,55,0);
  if(mod_XRayDeluxe.isOre(14)) throw new AssertionError("ore checkbox");
  mod_XRayDeluxe.radius=12;
  gui.a(35,207,0);
  if(mod_XRayDeluxe.radius!=8) throw new AssertionError("radius minus");
  gui.a(95,207,0);
  if(mod_XRayDeluxe.radius!=12) throw new AssertionError("radius plus");
  gui.a(210,207,0);
  for(int id:mod_XRayDeluxe.choices) if(!mod_XRayDeluxe.isOre(id)) throw new AssertionError("defaults "+id);
  mod_XRayDeluxe.hiddenOpacity=0;
  gui.a(210,230,0);
  if(mod_XRayDeluxe.hiddenOpacity!=10) throw new AssertionError("opacity increase");
  gui.a(35,230,0);
  if(mod_XRayDeluxe.hiddenOpacity!=0) throw new AssertionError("opacity decrease");
  gui.a(35,230,0);
  if(mod_XRayDeluxe.hiddenOpacity!=0) throw new AssertionError("opacity minimum");
  mod_XRayDeluxe.hiddenOpacity=100;
  gui.a(210,230,0);
  if(mod_XRayDeluxe.hiddenOpacity!=100) throw new AssertionError("opacity maximum");
  System.out.println("PASS: opacity controls and bounds");
  System.out.println("PASS: panel checkbox, radius controls, default selection");
 }
}
