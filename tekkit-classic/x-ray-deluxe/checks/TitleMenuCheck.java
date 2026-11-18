import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import sun.misc.Unsafe;
public final class TitleMenuCheck {
    static class Client extends Minecraft {
        Client(){super(null,null,null,320,240,false);}
        public void a(ja error){}
        public void a(vp screen){s=screen;}
    }
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Field field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);Unsafe unsafe=(Unsafe)field.get(null);
        Client mc=(Client)unsafe.allocateInstance(Client.class);
        XRayMenuTicker.addButton(null);XRayMenuTicker.addButton(mc);
        xt title=new xt();title.q=320;title.r=240;mc.s=title;
        title.s.add(new abp(28417,8,209,150,20,"Generic Assistant"));
        title.s.add(new abp(18342,164,209,150,20,"World Sync"));
        XRayMenuTicker.addButton(mc);XRayMenuTicker.addButton(mc);require(title.s.size()==3,"Duplicated or lost title buttons");
        XRayTitleButton button=(XRayTitleButton)title.s.get(2);
        require(!button.c(mc,0,0)&&mc.s==title,"Click outside button");
        require(button.c(mc,220,12)&&mc.s instanceof XRayDeluxeGui,"Cannot open X-Ray config before entering a world");
        XRayDeluxeGui gui=(XRayDeluxeGui)mc.s;gui.p=mc;gui.q=320;gui.r=240;
        mod_XRayDeluxe.enabled=false;mod_XRayDeluxe.radius=12;gui.a(35,207,0);
        require(mod_XRayDeluxe.radius==8,"Pre-world configuration unavailable");
        gui.a((char)0,1);require(mc.s==title,"Escape did not return to title");
        xt resized=new xt();resized.q=640;resized.r=480;mc.s=resized;XRayMenuTicker.addButton(mc);
        require(resized.s.size()==1&&((abp)resized.s.get(0)).c==532,"Button not restored or aligned after menu recreation");
        mc.s=new vp();XRayMenuTicker.addButton(mc);require(mc.s.s.isEmpty(),"Button injected outside title");
        require(new XRayMenuTicker().ticks().contains(cpw.mods.fml.common.TickType.RENDER),"Missing render callback");
        System.out.println("PASS: native X-Ray title button, no duplicates, other mod buttons preserved, pre-world configuration, Escape and resized menus");
    }
}
