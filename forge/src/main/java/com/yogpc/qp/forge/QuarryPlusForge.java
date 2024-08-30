package com.yogpc.qp.forge;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.integration.EnergyIntegration;
import com.yogpc.qp.forge.packet.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(QuarryPlus.modID)
public final class QuarryPlusForge {
    public QuarryPlusForge(IEventBus modBus) {
        QuarryPlus.LOGGER.info("Initialize Common");
        PlatformAccessForge.RegisterObjectsForge.REGISTER_LIST.forEach(r -> r.register(modBus));
        PacketHandler.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            callClient(modBus);
        }
        MinecraftForge.EVENT_BUS.register(EnergyIntegration.class);
        MinecraftForge.EVENT_BUS.register(PlatformAccess.getAccess());
        QuarryPlus.LOGGER.info("Initialize Common finished");
    }

    private static void callClient(IEventBus modBus) {
        QuarryPlusClientForge.registerClientBus(modBus);
    }
}
