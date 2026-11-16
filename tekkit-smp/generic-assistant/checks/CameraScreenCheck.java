package com.gabrielgollo.genericassistant.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
public class CameraScreenCheck {
    static void require(boolean b,String message){if(!b)throw new AssertionError(message);}
    static GuiButton button(AssistantSettingsScreen screen,int id){for(GuiButton b:screen.buttonList)if(b.id==id)return b;throw new AssertionError("Missing button "+id);}
    static void click(AssistantSettingsScreen screen,int id)throws Exception{
        GuiButton target=button(screen,id);int x=target.x+target.width/2,y=target.y+target.height/2;
        for(int i=0;i<screen.buttonList.size();i++){
            GuiButton candidate=screen.buttonList.get(i);
            if(x>=candidate.x&&x<candidate.x+candidate.width&&y>=candidate.y&&y<candidate.y+candidate.height)screen.actionPerformed(candidate);
        }
        screen.updateScreen();
    }
    public static void main(String[] args)throws Exception{
        GuiScreen parent=new GuiScreen();AssistantGuiFactory factory=new AssistantGuiFactory();require(factory.hasConfigGui(),"Forge Config disabled");
        AssistantSettingsScreen screen=(AssistantSettingsScreen)factory.createConfigGui(parent);screen.initGui();click(screen,7);
        ClientProxy proxy=ClientProxy.active;
        for(int setting=0;setting<3;setting++)for(int direction:new int[]{-1,1}){
            screen.actionPerformed(button(screen,10+setting*2+(direction==1?1:0)));
            require(proxy.changed==setting&&proxy.direction==direction,"Wrong adjustment routing");
        }
        click(screen,16);require(proxy.inverted,"Inversion toggle missing");
        click(screen,5);require(proxy.scans==1,"Reconnect action missing");
        for(GuiButton b:screen.buttonList)require(b.x>=0&&b.y>=0&&b.x+b.width<=screen.width&&b.y+b.height<=screen.height,"Button outside 320x240 screen");
        screen.drawScreen(0,0,0);screen.actionPerformed(button(screen,6));require(Minecraft.getMinecraft().currentScreen==parent,"Done lost parent");
        screen.keyTyped(' ',1);require(Minecraft.getMinecraft().currentScreen==parent,"Escape lost parent");
        click(screen,7);require(button(screen,0)!=null,"Assistance page missing");
        System.out.println("PASS: Forge Config factory, three adjustment pairs, inversion, rescan, assistance page, parent navigation and 320x240 layout");
    }
}
