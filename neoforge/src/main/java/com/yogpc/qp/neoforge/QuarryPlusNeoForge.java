package com.yogpc.qp.neoforge;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.DefinedQuarryConfig;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.neoforge.config.NeoForgeConfigDefiner;
import com.yogpc.qp.neoforge.integration.EnergyIntegration;
import com.yogpc.qp.neoforge.integration.StorageIntegration;
import com.yogpc.qp.neoforge.packet.PacketHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(QuarryPlus.modID)
public final class QuarryPlusNeoForge {
    static QuarryConfig config;

    public QuarryPlusNeoForge(IEventBus modBus, ModContainer container) {
        QuarryPlus.LOGGER.info("Initialize {} with {}", container.getModId(), container.getClass().getName());
        PlatformAccessNeoForge.RegisterObjectsNeoForge.REGISTER_LIST.forEach(r -> r.register(modBus));
        modBus.register(this);
        modBus.register(EnergyIntegration.class);
        modBus.register(StorageIntegration.class);
        var builder = new ModConfigSpec.Builder();
        config = new DefinedQuarryConfig(new NeoForgeConfigDefiner(builder), PlatformAccess.getAccess()::isInDevelopmentEnvironment, PlatformAccess.getAccess().platformName());
        container.registerConfig(ModConfig.Type.SYNCED, builder.build());
        QuarryPlus.LOGGER.info("Initialize finished {}", container.getModId());
    }

    @SubscribeEvent
    public void setupPacket(RegisterPayloadHandlersEvent event) {
        PacketHandler.init(event);
    }

    @SubscribeEvent
    public void onConfigLoad(ModConfigEvent.Loading event) {
        QuarryConfig.onConfigLoad(config);
    }

    @SubscribeEvent
    public void onConfigReload(ModConfigEvent.Reloading event) {
        QuarryConfig.onConfigLoad(config);
    }
}
