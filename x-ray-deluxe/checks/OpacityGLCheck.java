import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
public class OpacityGLCheck {
 public static void main(String[] args) throws Exception {
  Pbuffer target=new Pbuffer(16,16,new PixelFormat(),null,null); target.makeCurrent();
  try {
   java.lang.reflect.Constructor<?> ctor=Class.forName("mod_XRayDeluxe$1").getDeclaredConstructor(mod_XRayDeluxe.class); ctor.setAccessible(true);
   forge.IRenderContextHandler handler=(forge.IRenderContextHandler)ctor.newInstance(new mod_XRayDeluxe());
   GL11.glViewport(0,0,16,16); GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_DEPTH_TEST);
   for(int percent:new int[]{0,10,50,100}) {
    GL11.glClearColor(0,0,0,1); GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    mod_XRayDeluxe.hiddenOpacity=percent; handler.beforeRenderContext();
    GL11.glColor3f(1,1,1); GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex2f(-1,-1); GL11.glVertex2f(1,-1); GL11.glVertex2f(1,1); GL11.glVertex2f(-1,1); GL11.glEnd();
    handler.afterRenderContext();
    ByteBuffer pixel=org.lwjgl.BufferUtils.createByteBuffer(4); GL11.glReadPixels(8,8,1,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixel);
    int actual=pixel.get(0)&255, expected=Math.round(percent*2.55f);
    if(Math.abs(actual-expected)>8) throw new AssertionError(percent+"% expected "+expected+" got "+actual);
    if(GL11.glGetError()!=GL11.GL_NO_ERROR) throw new AssertionError("GL error");
    System.out.println("PASS: opacity "+percent+"% pixel="+actual);
   }
  } finally { target.destroy(); }
 }
}
