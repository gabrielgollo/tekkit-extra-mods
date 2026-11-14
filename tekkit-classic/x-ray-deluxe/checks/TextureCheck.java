import java.lang.reflect.Method;
public class TextureCheck {
 public static void main(String[] args) throws Exception {
  pb stone=pb.m[1], gold=pb.m[14], water=pb.m[8];
  boolean defaultAtlas=stone.isDefaultTexture;
  int stoneTile=stone.bN, goldTile=gold.bN, waterTile=water.bN;
  String waterAtlas=water.getTextureFile();
  Method apply=mod_XRayDeluxe.class.getDeclaredMethod("applyXray",net.minecraft.client.Minecraft.class,boolean.class); apply.setAccessible(true);
  mod_XRayDeluxe.oreIds=new int[] {14};
  mod_XRayDeluxe.hiddenOpacity=0;
  apply.invoke(null,null,true);
  if(stone.bN!=180 || gold.bN!=goldTile || water.bN!=waterTile || !waterAtlas.equals(water.getTextureFile())) throw new AssertionError("invisible selection/liquid preservation");
  mod_XRayDeluxe.hiddenOpacity=30;
  apply.invoke(null,null,true);
  if(!stone.getTextureFile().equals("/xray-hidden.png") || stone.bN!=stoneTile) throw new AssertionError("translucent atlas");
  apply.invoke(null,null,false);
  if(stone.isDefaultTexture!=defaultAtlas || stone.bN!=stoneTile || !stone.getTextureFile().equals("/terrain.png") || gold.bN!=goldTile) throw new AssertionError("restore");
  System.out.println("PASS: invisible terrain, ore preservation, liquid preservation, opacity atlas, restore");
 }
}
