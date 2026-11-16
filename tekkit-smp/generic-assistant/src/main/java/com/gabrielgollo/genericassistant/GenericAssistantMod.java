package com.gabrielgollo.genericassistant;

import com.gabrielgollo.genericassistant.client.ClientProxy;
import com.gabrielgollo.genericassistant.client.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GenericAssistantMod.MOD_ID, name = "gabrielgollo_Generic Assistant", version = "1.0.0-mc1.12.2", clientSideOnly = true, acceptedMinecraftVersions = "[1.12.2]", guiFactory = "com.gabrielgollo.genericassistant.client.AssistantGuiFactory")
public final class GenericAssistantMod {
    public static final String MOD_ID = "gabrielgollo_genericassistant";

    @SidedProxy(clientSide = "com.gabrielgollo.genericassistant.client.ClientProxy", serverSide = "com.gabrielgollo.genericassistant.client.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }
}
