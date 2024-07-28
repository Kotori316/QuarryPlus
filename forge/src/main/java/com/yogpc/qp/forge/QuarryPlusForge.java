package com.yogpc.qp.forge;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.integration.EnergyIntegration;
import com.yogpc.qp.forge.packet.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QuarryPlus.modID)
public final class QuarryPlusForge {
    public QuarryPlusForge() {
        QuarryPlus.LOGGER.info("Initialize Common");
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        PlatformAccessForge.RegisterObjectsForge.REGISTER_LIST.forEach(r -> r.register(modBus));
        PacketHandler.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> QuarryPlusClientForge::registerClientBus);
        MinecraftForge.EVENT_BUS.register(EnergyIntegration.class);
        QuarryPlus.LOGGER.info("Initialize Common finished");
    }
}
