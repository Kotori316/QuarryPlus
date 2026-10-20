package com.yogpc.qp.forge;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.DefinedQuarryConfig;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.forge.config.ForgeConfigDefiner;
import com.yogpc.qp.forge.integration.EnergyIntegration;
import com.yogpc.qp.forge.integration.StorageIntegration;
import com.yogpc.qp.forge.packet.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

@Mod(QuarryPlus.modID)
public final class QuarryPlusForge {
    static QuarryConfig config;

    public QuarryPlusForge(BusGroup busGroup, ModContainer modContainer) {
        QuarryPlus.LOGGER.info("Initialize Common");
        PlatformAccessForge.RegisterObjectsForge.REGISTER_LIST.forEach(r -> r.register(busGroup));
        PacketHandler.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            callClient(busGroup);
        }
        AttachCapabilitiesEvent.BlockEntities.BUS.addListener(EnergyIntegration::attachCapabilities);
        AttachCapabilitiesEvent.BlockEntities.BUS.addListener(StorageIntegration::attachCapabilities);

        try {
            var clazz = Class.forName("com.yogpc.qp.forge.gametest.QuarryPlusGameTest", true, QuarryPlusForge.class.getClassLoader());
            var method = clazz.getMethod("register", RegisterEvent.class);
            QuarryPlus.LOGGER.info("Found GameTest class {}", clazz.getName());
            RegisterEvent.getBus(busGroup).addListener(event -> {
                try {
                    method.invoke(null, event);
                } catch (ReflectiveOperationException e) {
                    QuarryPlus.LOGGER.error("Failed to register GameTest", e);
                    throw new RuntimeException(e);
                }
            });
        } catch (ReflectiveOperationException e) {
            // no op
            QuarryPlus.LOGGER.debug("No GameTest are registered");
        }

        var builder = new ForgeConfigSpec.Builder();
        config = new DefinedQuarryConfig(new ForgeConfigDefiner(builder), PlatformAccess.getAccess()::isInDevelopmentEnvironment, PlatformAccess.getAccess().platformName());
        modContainer.addConfig(new ModConfig(ModConfig.Type.COMMON, builder.build(), modContainer));
        ModConfigEvent.Loading.getBus(busGroup).addListener(_ -> QuarryConfig.onConfigLoad(config));
        ModConfigEvent.Reloading.getBus(busGroup).addListener(_ -> QuarryConfig.onConfigLoad(config));

        QuarryPlus.LOGGER.info("Initialize Common finished");
    }

    private static void callClient(BusGroup modBus) {
        QuarryPlusClientForge.registerClientBus(modBus);
    }
}
