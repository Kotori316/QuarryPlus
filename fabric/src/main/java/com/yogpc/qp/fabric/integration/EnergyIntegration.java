package com.yogpc.qp.fabric.integration;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

public final class EnergyIntegration {
    private static boolean registered = false;

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            QuarryPlus.LOGGER.debug("Trying to register Reborn Energy Handler");
            try {
                if (RebornEnergyRegister.register()) {
                    registered = true;
                    QuarryPlus.LOGGER.info("Registered Reborn Energy Handler");
                }
            } catch (Throwable error) {
                var rebornMods = FabricLoader.getInstance().getAllMods().stream()
                    .map(ModContainer::getMetadata).filter(s -> s.getId().contains("reborn"))
                    .map(m -> String.format("%s@%s", m.getId(), m.getVersion().getFriendlyString())).toList();
                QuarryPlus.LOGGER.error("QuarryPlus caught energy integration error. RebornMods: %s".formatted(rebornMods), error);
            }
        }
    }

    public static boolean hasAnyEnergyModule() {
        return registered;
    }
}

class RebornEnergyRegister {
    static boolean register() {
        for (BlockEntityType<?> blockEntityType : PlatformAccess.getAccess().registerObjects().getBlockEntityTypes()) {
            EnergyStorage.SIDED.registerForBlockEntities(RebornEnergyStorage::provider, blockEntityType);
        }
        return true;
    }
}
