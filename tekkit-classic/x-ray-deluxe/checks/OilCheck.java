import java.lang.reflect.Method;
public class OilCheck {
 public static void main(String[] args) throws Exception {
  pb ignored=pb.m[1];
  BuildCraftCore.customBuildCraftTexture="/net/minecraft/src/buildcraft/core/gui/block_textures.png";
  pb[] oils={new buildcraft.energy.BlockOilStill(3000,pb.m[8].cd),new buildcraft.energy.BlockOilFlowing(3001,pb.m[8].cd)};
  int[] tiles={oils[0].bN,oils[1].bN};
  Method apply=mod_XRayDeluxe.class.getDeclaredMethod("applyXray",net.minecraft.client.Minecraft.class,boolean.class); apply.setAccessible(true);
  for(int opacity:new int[]{0,10,50,100}) {
   mod_XRayDeluxe.hiddenOpacity=opacity;
   for(boolean active:new boolean[]{true,false}) {
    apply.invoke(null,null,active);
    for(int i=0;i<2;i++) if(oils[i].bN!=tiles[i] || !oils[i].getTextureFile().equals(BuildCraftCore.customBuildCraftTexture)) throw new AssertionError("oil changed");
   }
  }
  System.out.println("PASS: real BuildCraft still/flowing oil tiles and atlas preserved at 0/10/50/100 percent and on/off");
 }
}
