package com.gabrielgollo.controllerassistant;

import com.gabrielgollo.controllerassistant.client.ClientProxy;
import com.gabrielgollo.controllerassistant.client.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ControllerAssistantMod.MOD_ID, name = "gabrielgollo_Controller Assistant", version = "1.0.0-mc1.12.2", clientSideOnly = true, acceptedMinecraftVersions = "[1.12.2]")
public final class ControllerAssistantMod {
    public static final String MOD_ID = "gabrielgollo_controllerassistant";

    @SidedProxy(clientSide = "com.gabrielgollo.controllerassistant.client.ClientProxy", serverSide = "com.gabrielgollo.controllerassistant.client.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }
}
