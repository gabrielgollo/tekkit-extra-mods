package com.gabrielgollo.worldsync;
final class S3WorldStore implements AutoCloseable {
static final java.util.Map<String,RemoteWorld> worlds=new java.util.concurrent.ConcurrentHashMap<>();static final java.util.Map<String,byte[]> bytes=new java.util.concurrent.ConcurrentHashMap<>();static int puts;static boolean failNextList;
S3WorldStore(WorldSyncConfig c,String u){}public void close(){}
java.util.List<RemoteWorld> listWorlds(){if(failNextList){failNextList=false;throw new NoClassDefFoundError("Injected SDK loading failure");}return new java.util.ArrayList<>(worlds.values());}
long remoteLocalModified(RemoteWorld r){return r.modified;}
void upload(java.io.File f,String name,long modified)throws java.io.IOException{bytes.put(name,java.nio.file.Files.readAllBytes(f.toPath()));worlds.put(name,new RemoteWorld(name,name,modified));puts++;}
void download(RemoteWorld r,java.io.File f)throws java.io.IOException{java.nio.file.Files.write(f.toPath(),bytes.get(r.name));}
static class RemoteWorld{final String name,key;final long modified;RemoteWorld(String n,String k,long t){name=n;key=k;modified=t;}}
}