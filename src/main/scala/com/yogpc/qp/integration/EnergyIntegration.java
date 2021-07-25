package com.yogpc.qp.integration;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import net.fabricmc.loader.api.FabricLoader;
import team.reborn.energy.Energy;

public class EnergyIntegration {
    private static boolean registered = false;

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            QuarryPlus.LOGGER.debug("Trying to register Reborn Energy Handler.");
            RebornEnergyRegister.register();
            registered = true;
        }
    }

    public static boolean hasAnyEnergyModule() {
        return registered;
    }
}

class RebornEnergyRegister {
    static void register() {
        try {
            Energy.registerHolder(t -> t instanceof PowerTile, t -> new RebornEnergyStorage((PowerTile) t));
        } catch (Throwable error) {
            QuarryPlus.LOGGER.error("QuarryPlus caught energy integration error.", error);
        }
    }
}
