import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import sun.misc.Unsafe;
public final class HotbarCycleCheck {
    static Unsafe unsafe;
    static final class Client extends Minecraft {
        Client(){super(null,null,null,320,240,false);}
        public void a(ja error){}
    }
    static final class Container extends dd {public boolean b(yw player){return true;}}
    static final class Clicks extends ki {
        int count;
        Clicks(Minecraft mc){super(mc);}
        public void a(int x,int y,int z,int face){}
        public void c(int x,int y,int z,int face){}
        public void a(){}
        public float b(){return 0;}
        public boolean d(){return false;}
        public boolean a(yw player,xd world,aan stack,int x,int y,int z,int face){return false;}
        public aan a(int window,int slot,int button,boolean shift,yw player){
            if(window!=player.aq.f||button!=0||shift)throw new AssertionError("Inventory click protocol");
            int index=slot>=36?slot-36:slot;
            aan old=player.ap.a[index];player.ap.a[index]=player.ap.k();
            try{Field cursor=aak.class.getDeclaredField("f");cursor.setAccessible(true);cursor.set(player.ap,old);}catch(Exception error){throw new RuntimeException(error);}
            count++;return old;
        }
    }
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    static Client client(boolean tool)throws Exception{
        Client mc=(Client)unsafe.allocateInstance(Client.class);mc.h=(vq)unsafe.allocateInstance(vq.class);mc.f=(xd)unsafe.allocateInstance(xd.class);
        mc.h.ap=new aak(mc.h);mc.h.aq=new Container();mc.h.ar=mc.h.aq;mc.c=new Clicks(mc);
        mc.h.ap.a[0]=new aan(tool?257:35,1,tool?249:2);mc.h.ap.a[9]=new aan(tool?257:35,32,tool?30:2);
        return mc;
    }
    public static void main(String[] args)throws Exception{
        Class.forName("pb");
        Field field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);unsafe=(Unsafe)field.get(null);
        mod_GenericAssistant.refillHotbar=true;mod_GenericAssistant.replaceBrokenTool=true;
        for(boolean tool:new boolean[]{false,true}){
            Client mc=client(tool);HotbarAssistant helper=new HotbarAssistant();helper.update(mc,true,false);
            mc.h.ap.a[0]=null;helper.update(mc,false,false);
            require(((Clicks)mc.c).count==2,"Pickup and place expected");require(mc.h.ap.a[0]!=null&&mc.h.ap.a[9]==null&&mc.h.ap.k()==null,"Stack must move, not copy");
        }
        for(int scenario=0;scenario<7;scenario++){
            Client mc=client(false);HotbarAssistant helper=new HotbarAssistant();helper.update(mc,scenario!=0,false);mc.h.ap.a[0]=null;
            if(scenario==2)mc.s=new vp();if(scenario==3)mc.h.ap.c=1;if(scenario==4)mc.f=(xd)unsafe.allocateInstance(xd.class);
            if(scenario==5)mc.h.ap.a[9]=null;if(scenario==6)mod_GenericAssistant.refillHotbar=false;
            helper.update(mc,true,scenario==1);require(((Clicks)mc.c).count==0,"Guard scenario "+scenario);mod_GenericAssistant.refillHotbar=true;
        }
        System.out.println("PASS: Classic depletion, broken tool, inventory click protocol, no duplication, GUI/world/slot/drop and option guards");
    }
}
