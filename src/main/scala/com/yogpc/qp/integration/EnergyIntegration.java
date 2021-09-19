package com.yogpc.qp.integration;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import net.fabricmc.loader.api.FabricLoader;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings("SpellCheckingInspection")
public class EnergyIntegration {
    private static boolean registered = false;

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            QuarryPlus.LOGGER.debug("Trying to register Reborn Energy Handler.");
            if (RebornEnergyRegister.register()) {
                registered = true;
            }
        }
        if (FabricLoader.getInstance().isModLoaded("fasttransferlib")) {
            QuarryPlus.LOGGER.debug("Trying to register FastTransferLib Handler.");
            if (FastTransferLibRegister.register()) {
                registered = true;
            }
        }
    }

    public static boolean hasAnyEnergyModule() {
        return registered;
    }
}

class RebornEnergyRegister {
    static boolean register() {
        try {
            EnergyStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
                    if (blockEntity instanceof PowerTile powerTile) return new RebornEnergyStorage(powerTile);
                    else return null;
                },
                QuarryPlus.ModObjects.QUARRY_TYPE);
            return true;
        } catch (Throwable error) {
            QuarryPlus.LOGGER.error("QuarryPlus caught energy integration error.", error);
            return false;
        }
    }
}

class FastTransferLibRegister {
    static boolean register() {
        try {
            EnergyApi.SIDED.registerForBlockEntities((blockEntity, context) -> {
                    if (blockEntity instanceof PowerTile powerTile) return new FastTransferLibEnergyIO(powerTile, context);
                    else return null;
                },
                QuarryPlus.ModObjects.QUARRY_TYPE);
            return true;
        } catch (Throwable error) {
            QuarryPlus.LOGGER.error("QuarryPlus caught energy integration error.", error);
            return false;
        }
    }
}
