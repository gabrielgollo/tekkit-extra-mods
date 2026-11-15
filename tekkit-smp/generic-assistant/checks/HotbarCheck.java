package com.gabrielgollo.genericassistant.client;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
public final class HotbarCheck {
    static final Object BLOCK=new Object(), TOOL=new Object();
    static ItemStack stack(boolean tool,int damage){return new ItemStack(tool?TOOL:BLOCK,1,damage,tool);}
    static void require(boolean condition,String name){if(!condition)throw new AssertionError(name);}
    static Minecraft client(boolean tool){Minecraft mc=new Minecraft();mc.player.inventory.mainInventory.set(0,stack(tool,tool?99:2));mc.player.inventory.mainInventory.set(9,stack(tool,tool?30:2));return mc;}
    public static void main(String[] args){
        for(boolean tool:new boolean[]{false,true}){
            Minecraft mc=client(tool);HotbarAssistant helper=new HotbarAssistant();
            helper.update(mc,true,true,true,false);mc.player.inventory.mainInventory.set(0,ItemStack.EMPTY);
            helper.update(mc,true,true,false,false);
            require(mc.playerController.clicks==1,"replacement click");require(!mc.player.inventory.getStackInSlot(0).isEmpty(),"same slot filled");require(mc.player.inventory.getStackInSlot(9).isEmpty(),"source moved, not copied");
        }
        for(int scenario=0;scenario<8;scenario++){
            Minecraft mc=client(scenario==1);HotbarAssistant helper=new HotbarAssistant();helper.update(mc,true,true,scenario!=2,false);mc.player.inventory.mainInventory.set(0,ItemStack.EMPTY);
            if(scenario==3)mc.currentScreen=new Object();if(scenario==4)mc.player.inventory.currentItem=1;
            if(scenario==5)mc.world=new Object();if(scenario==6)mc.player.inventory.cursor=stack(false,2);
            if(scenario==7)mc.player.inventory.mainInventory.get(9).tag="different";
            helper.update(mc,scenario!=0,scenario!=1,true,scenario==2);
            require(mc.playerController.clicks==0,"suppression scenario "+scenario);
        }
        Minecraft mc=client(false);mc.player.inventory.mainInventory.get(9).damage=3;
        require(HotbarAssistant.replacement(mc.player.inventory.mainInventory,stack(false,2))==-1,"variant mismatch");
        mc.player.inventory.mainInventory.set(9,ItemStack.EMPTY);mc.player.inventory.mainInventory.set(1,stack(false,2));
        require(HotbarAssistant.replacement(mc.player.inventory.mainInventory,stack(false,2))==-1,"other hotbar slot untouched");
        mc=client(true);mc.player.inventory.mainInventory.get(9).damage=100;
        require(HotbarAssistant.replacement(mc.player.inventory.mainInventory,stack(true,99))==-1,"unusable tool");
        System.out.println("PASS: depletion, broken tools, inventory swaps, independent options, intentional drops, GUI/cursor/world/slot guards, variants and tags");
    }
}
