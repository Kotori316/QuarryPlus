package com.yogpc.qp.integration;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
// import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings("SpellCheckingInspection")
public class EnergyIntegration {
    private static boolean registered = false;

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            QuarryPlus.LOGGER.debug("Trying to register Reborn Energy Handler.");
            try {
                /*if (RebornEnergyRegister.register()) {
                    registered = true;
                }*/
            } catch (Throwable error) {
                var rebornMods = FabricLoader.getInstance().getAllMods().stream()
                    .map(ModContainer::getMetadata).filter(s -> s.getId().contains("reborn"))
                    .map(m -> String.format("%s@%s", m.getId(), m.getVersion().getFriendlyString())).toList();
                QuarryPlus.LOGGER.error("QuarryPlus caught energy integration error. RebornMods: " + rebornMods, error);
            }
        }
    }

    public static boolean hasAnyEnergyModule() {
        return registered;
    }

    static BlockEntityType<?>[] getBlockEntityTypes() {
        return new BlockEntityType<?>[]{
            QuarryPlus.ModObjects.QUARRY_TYPE,
            QuarryPlus.ModObjects.ADV_QUARRY_TYPE,
            QuarryPlus.ModObjects.ADV_PUMP_TYPE
        };
    }
}

/*class RebornEnergyRegister {
    static boolean register() {
        EnergyStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
                if (blockEntity instanceof PowerTile powerTile) return new RebornEnergyStorage(powerTile);
                else return null;
            },
            EnergyIntegration.getBlockEntityTypes());
        EnergyStorage.SIDED.registerForBlockEntity((g, d) -> new RebornEnergyGenerator(g), QuarryPlus.ModObjects.CREATIVE_GENERATOR_TYPE);
        return true;
    }
}*/
