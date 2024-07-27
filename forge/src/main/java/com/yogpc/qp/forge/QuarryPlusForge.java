package com.yogpc.qp.forge;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.machine.packet.PacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QuarryPlus.modID)
public final class QuarryPlusForge {
    public QuarryPlusForge() {
        QuarryPlus.LOGGER.info("Initialize Common");
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        PlatformAccessForge.RegisterObjectsForge.REGISTER_LIST.forEach(r -> r.register(modBus));
        PacketHandler.init();
        QuarryPlus.LOGGER.info("Initialize Common finished");
    }
}
