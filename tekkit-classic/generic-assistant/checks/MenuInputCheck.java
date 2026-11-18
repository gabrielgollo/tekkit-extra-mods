import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import sun.misc.Unsafe;
public final class MenuInputCheck {
    static class Client extends Minecraft {
        Client(){super(null,null,null,320,240,false);}
        public void a(ja error){}
        public void a(vp screen){s=screen;}
    }
    static class Screen extends vp {
        int left,right,releases,back,lastX,lastY;
        protected void a(int x,int y,int button){if(button==0)left++;else right++;lastX=x;lastY=y;}
        protected void b(int x,int y,int button){releases++;}
        protected void a(char value,int key){if(key==1)back++;}
    }
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Field field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);Unsafe unsafe=(Unsafe)field.get(null);
        Client mc=(Client)unsafe.allocateInstance(Client.class);Screen first=new Screen();mc.s=first;MenuInput input=new MenuInput();
        input.update(mc,first,10,20,true,false,false);require(first.left==0,"Held A must not enter a new screen");
        input.update(mc,first,10,20,false,false,false);
        input.update(mc,first,10,20,true,false,false);
        for(int frame=0;frame<100;frame++)input.update(mc,first,10,20,true,false,false);
        require(first.left==1&&first.lastX==10&&first.lastY==20,"A must click once at cursor");
        input.update(mc,first,10,20,false,false,false);require(first.releases==1,"A release missing");
        input.update(mc,first,10,20,false,true,false);require(first.right==1,"X right click missing");
        input.update(mc,first,10,20,false,false,true);input.update(mc,first,10,20,false,false,true);require(first.back==1,"B must close once");
        Screen next=new Screen();mc.s=next;input.update(mc,next,5,6,true,false,false);require(next.left==0,"A leaked into next screen");
        input.update(mc,next,5,6,false,false,false);input.update(mc,next,5,6,true,false,false);require(next.left==1,"New screen not rearmed");
        input.reset();require(next.releases==1,"Reset did not release click");
        mod_GenericAssistant mod=new mod_GenericAssistant();xt title=new xt();title.q=320;title.r=240;mc.s=title;
        title.s.add(new abp(18342,164,209,150,20,"World Sync"));
        mod.updateTitleScreen(mc);mod.updateTitleScreen(mc);
        require(title.s.size()==2,"Duplicate assistant button or World Sync lost");
        AssistantTitleButton button=null;for(Object value:title.s)if(value instanceof AssistantTitleButton)button=(AssistantTitleButton)value;
        require(button!=null&&button.c(mc,12,215),"Native configuration button not clickable");
        require(mc.s instanceof AssistantGui,"Native button did not open configuration");
        xt reopened=new xt();reopened.q=320;reopened.r=240;mc.s=reopened;mod.updateTitleScreen(mc);
        require(reopened.s.size()==1&&reopened.s.get(0) instanceof AssistantTitleButton,"Button not restored after menu recreation");
        System.out.println("PASS: direct A/X/B events, click/release edges, page rearming, native settings button, World Sync coexistence and menu recreation");
    }
}
