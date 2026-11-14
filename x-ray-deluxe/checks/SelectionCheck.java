import java.lang.reflect.Method;
public class SelectionCheck {
 public static void main(String[] args) throws Exception {
  mod_XRayDeluxe.oreIds = new int[] {73,74,56};
  mod_XRayDeluxe.toggleOre(null,73);
  if(mod_XRayDeluxe.isOre(73)||mod_XRayDeluxe.isOre(74)||!mod_XRayDeluxe.isOre(56)) throw new AssertionError("redstone removal");
  mod_XRayDeluxe.toggleOre(null,73);
  if(!mod_XRayDeluxe.isOre(73)||!mod_XRayDeluxe.isOre(74)) throw new AssertionError("redstone selection");
  mod_XRayDeluxe.oreIds = new int[0];
  mod_XRayDeluxe.writeConfig();
  mod_XRayDeluxe.oreIds = new int[] {56};
  Method read=mod_XRayDeluxe.class.getDeclaredMethod("readConfig"); read.setAccessible(true); read.invoke(null);
  if(mod_XRayDeluxe.oreIds.length!=0) throw new AssertionError("empty persistence");
  if(!mod_XRayDeluxe.shouldHighlight(10) || !mod_XRayDeluxe.shouldHighlight(11)) throw new AssertionError("lava always visible with empty ore selection");
  if(mod_XRayDeluxe.shouldHighlight(56) || mod_XRayDeluxe.shouldHighlight(1)) throw new AssertionError("unselected blocks highlighted");
  System.out.println("PASS: flowing/still lava always highlighted independently of ore selection");
  System.out.println("PASS: redstone selection, deselection, empty selection persistence");
 }
}
