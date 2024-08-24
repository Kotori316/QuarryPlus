package com.yogpc.qp.fabric;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.fabric.integration.EnergyIntegration;
import com.yogpc.qp.fabric.packet.PacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class QuarryPlusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        QuarryPlus.LOGGER.info("Initialize Common");
        PacketHandler.Server.initCommon();
        PacketHandler.Server.initServer();
        PlatformAccessFabric.RegisterObjectsFabric.registerAll();
        EnergyIntegration.register();
        if (PlatformAccess.getAccess() instanceof ServerLifecycleEvents.ServerStopped f) {
            ServerLifecycleEvents.SERVER_STOPPED.register(f);
        }
        QuarryPlus.LOGGER.info("Initialize Common finished");
    }

}
