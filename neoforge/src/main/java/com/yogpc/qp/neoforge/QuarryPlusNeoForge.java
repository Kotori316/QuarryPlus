package com.yogpc.qp.neoforge;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.neoforge.integration.EnergyIntegration;
import com.yogpc.qp.neoforge.packet.PacketHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(QuarryPlus.modID)
public final class QuarryPlusNeoForge {
    public QuarryPlusNeoForge(IEventBus modBus, ModContainer container) {
        QuarryPlus.LOGGER.info("Initialize {} with {}", container.getModId(), container.getClass().getName());
        PlatformAccessNeoForge.RegisterObjectsNeoForge.REGISTER_LIST.forEach(r -> r.register(modBus));
        modBus.register(this);
        if (FMLEnvironment.dist.isClient()) {
            modBus.register(QuarryPlusClientNeoForge.class);
        }
        modBus.register(EnergyIntegration.class);
        NeoForge.EVENT_BUS.register(PlatformAccess.getAccess());
        QuarryPlus.LOGGER.info("Initialize finished {}", container.getModId());
    }

    @SubscribeEvent
    public void setupPacket(RegisterPayloadHandlersEvent event) {
        PacketHandler.init(event);
    }
}
