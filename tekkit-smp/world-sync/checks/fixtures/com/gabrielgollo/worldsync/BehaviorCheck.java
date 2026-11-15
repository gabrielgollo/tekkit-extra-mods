package com.gabrielgollo.worldsync;
import java.io.*;import java.nio.file.*;import java.util.zip.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class BehaviorCheck {
 static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void waitFor(WorldSyncMod m)throws Exception{long until=System.currentTimeMillis()+5000;while(m.isBusy()&&System.currentTimeMillis()<until)Thread.sleep(10);require(!m.isBusy(),"Timed out");require(!m.status().startsWith("Sync failed"),m.status());}
 public static void main(String[] args)throws Exception{
  Minecraft mc=Minecraft.INSTANCE;mc.mcDataDir=Files.createTempDirectory("smp-sync-check-").toFile();mc.currentScreen=new GuiMainMenu();
  WorldSyncConfig config=WorldSyncConfig.load(new File(mc.mcDataDir,"config"));require(new File(mc.mcDataDir,"config/gabrielgollo-world-sync.properties").isFile(),"First config not created");
  require(config.endpoint.isEmpty()&&config.bucket.isEmpty()&&config.userId.isEmpty()&&!config.isConfigured(),"Personal defaults must be absent");
  config.update("http://localhost","test","us-east-1","test","test","Check");config.save();
  Path level=mc.mcDataDir.toPath().resolve("saves/Local/level.dat");Files.createDirectories(level.getParent());Files.write(level,"first".getBytes("UTF-8"));
  Path backup=mc.mcDataDir.toPath().resolve("saves/Local.world-sync-backup-1/level.dat");Files.createDirectories(backup.getParent());Files.write(backup,new byte[]{1});
  WorldSyncMod mod=new WorldSyncMod();mod.preInit(new FMLPreInitializationEvent());
  mod.onClientTick(new TickEvent.ClientTickEvent());waitFor(mod);
  require(S3WorldStore.puts==1,"Startup must upload only missing world, not backups");require(mc.screenChanges==0,"Startup opened config");
  mod.onClientTick(new TickEvent.ClientTickEvent());require(!mod.isBusy(),"Startup repeated");
  for(int i=0;i<2;i++){
   String expected="latest-"+i;Files.write(level,expected.getBytes("UTF-8"));
   mod.onServerStopping(new FMLServerStoppingEvent());mod.onServerStopped(new FMLServerStoppedEvent());waitFor(mod);
   require(S3WorldStore.puts==i+2,"Exit upload missing");require(mc.screenChanges==0,"Exit opened config");
   try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(S3WorldStore.bytes.get("Local")))){ZipEntry e;boolean matched=false;while((e=zip.getNextEntry())!=null)if(e.getName().equals("level.dat")){ByteArrayOutputStream out=new ByteArrayOutputStream();int b;while((b=zip.read())!=-1)out.write(b);matched=expected.equals(new String(out.toByteArray(),"UTF-8"));}require(matched,"Stale exit save");}
  }
  mod.createSyncScreen(mc.currentScreen);require(!mod.isBusy(),"Opening settings starts a sync");
  S3WorldStore.failNextList=true;
  mod.retrySync();long failureDeadline=System.currentTimeMillis()+5000;
  while(mod.isBusy()&&System.currentTimeMillis()<failureDeadline)Thread.sleep(10);
  require(!mod.isBusy()&&mod.status().contains("NoClassDefFoundError"),"SDK failure left checking status stuck");
  mod.retrySync();waitFor(mod);
  S3WorldStore.worlds.put("Local",new S3WorldStore.RemoteWorld("Local","Local",System.currentTimeMillis()+60000));
  mod.onServerStopping(new FMLServerStoppingEvent());mod.onServerStopped(new FMLServerStoppedEvent());waitFor(mod);
  require(mod.conflict()!=null,"Newer cloud copy did not produce a conflict");
  require(mc.screenChanges==0,"Server thread opened a screen");
  mod.onClientTick(new TickEvent.ClientTickEvent());
  require(mc.currentScreen instanceof WorldSyncScreen,"Conflict prompt missing");
  require(mc.screenChanges==1,"Conflict prompt repeated");
  System.out.println("PASS: first-run configuration, automatic startup upload, backup exclusion, startup once, two updated exit uploads, no forced screens");
 }
}
