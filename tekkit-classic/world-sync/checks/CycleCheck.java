import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import com.sun.net.httpserver.*;
import net.minecraft.client.Minecraft;
import sun.misc.Unsafe;
public class CycleCheck {
 static Unsafe unsafe;
 static class Client extends Minecraft {
  Client() { super(null,null,null,320,240,false); }
  public void a(ja value) {}
  public void a(vp screen) { s=screen; }
 }
 static void set(Object target, Class<?> type, String name,Object value) throws Exception {
  Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value);
 }
 static void require(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=(Unsafe)uf.get(null);
  AtomicInteger uploads=new AtomicInteger();
  AtomicReference<byte[]> payload=new AtomicReference<byte[]>();
  HttpServer server=HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/",exchange->{
   if(exchange.getRequestMethod().equals("PUT")) {ByteArrayOutputStream body=new ByteArrayOutputStream(); byte[] buffer=new byte[8192]; int count; while((count=exchange.getRequestBody().read(buffer))!=-1)body.write(buffer,0,count); payload.set(body.toByteArray()); uploads.incrementAndGet();exchange.sendResponseHeaders(200,-1);}
   else {byte[] xml="<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\"><IsTruncated>false</IsTruncated></ListBucketResult>".getBytes("UTF-8");exchange.sendResponseHeaders(200,xml.length);exchange.getResponseBody().write(xml);}
   exchange.close();
  });server.start();
  try {
   Client mc=(Client)unsafe.allocateInstance(Client.class);
   set(cpw.mods.fml.client.FMLClientHandler.instance(),cpw.mods.fml.client.FMLClientHandler.class,"client",mc);
   mod_WorldSync mod=new mod_WorldSync();
   ClassicWorldSyncConfig config=ClassicWorldSyncConfig.load();
   config.update("http://127.0.0.1:"+server.getAddress().getPort(),"test","us-east-1","test","test","CycleCheck");
   set(mod,mod_WorldSync.class,"config",config);
   set(mod,mod_WorldSync.class,"startupScreenShown",true);
   ClassicWorldSyncTicker ticker=new ClassicWorldSyncTicker(mod);
   for(int cycle=1;cycle<=2;cycle++) {
    Files.write(Paths.get("saves/CycleCheck/level.dat"),("changed-save-"+cycle).getBytes("UTF-8"));
    eg handler=(eg)unsafe.allocateInstance(eg.class);set(handler,eg.class,"f","CycleCheck");
    xd world=(xd)unsafe.allocateInstance(xd.class);set(world,xd.class,"w",handler);
    mc.f=world;mc.s=null;
    ticker.tickEnd(EnumSet.of(cpw.mods.fml.common.TickType.RENDER));
    mc.f=null;mc.s=new xt();
    ticker.tickEnd(EnumSet.of(cpw.mods.fml.common.TickType.RENDER));
    require(mc.s instanceof ClassicWorldSyncMainMenu,"Exit upload interrupted title screen");
    long deadline=System.currentTimeMillis()+10000;
    while(mod.isBusy()&&System.currentTimeMillis()<deadline)Thread.sleep(20);
    require(!mod.isBusy()&&mod.status().startsWith("World uploaded:"),mod.status());
    require(uploads.get()==cycle,"Upload missing on cycle "+cycle);
    boolean found=false;
    try(java.util.zip.ZipInputStream zip=new java.util.zip.ZipInputStream(new ByteArrayInputStream(payload.get()))) {
     java.util.zip.ZipEntry entry;
     while((entry=zip.getNextEntry())!=null)if(entry.getName().equals("level.dat")) {
      ByteArrayOutputStream bytes=new ByteArrayOutputStream();int value;while((value=zip.read())!=-1)bytes.write(value);
      require(new String(bytes.toByteArray(),"UTF-8").equals("changed-save-"+cycle),"Uploaded stale save contents");found=true;
     }
    }
    require(found,"Uploaded ZIP lacks level.dat");
    mod.returnToTitle();ticker.tickEnd(EnumSet.of(cpw.mods.fml.common.TickType.RENDER));
    require(mc.s instanceof ClassicWorldSyncMainMenu,"Native title menu missing on cycle "+cycle);
    System.out.println("PASS cycle "+cycle+": world tracked, exit upload completed, native menu restored");
   }
  }finally{server.stop(0);}
 }
}
