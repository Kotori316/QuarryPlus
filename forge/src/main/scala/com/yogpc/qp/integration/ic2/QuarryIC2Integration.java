package com.yogpc.qp.integration.ic2;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public final class QuarryIC2Integration {
    private static final Logger LOGGER = QuarryPlus.getLogger(QuarryIC2Integration.class);
    private static final AtomicInteger isIC2Loaded = new AtomicInteger(-1);

    public static boolean isIC2Loaded() {
        int loaded = isIC2Loaded.get();
        if (loaded == -1) {
            boolean ic2 = ModList.get().isLoaded("ic2");
            LOGGER.info("Set QuarryPlus ic2 integration to {}", ic2);
            isIC2Loaded.set(ic2 ? 1 : 0);
            return ic2;
        }
        return loaded == 1;
    }

    public static void registerIc2Tile(PowerTile tile) {
        if (isIC2Loaded())
            Internal.register(tile);
    }

    public static void unloadIc2Tile(PowerTile tile) {
        if (isIC2Loaded())
            Internal.unload(tile);
    }

    private static class Internal {

        private static void register(PowerTile tile) {
            IEnergySink sink = new PowerTileEnergySink(tile);
            EnergyNet.INSTANCE.addTile(sink);
            LOGGER.trace("Registered {} as IC2 tile at {} in {}",
                tile.getClass().getSimpleName(), sink.getPosition(), sink.getWorldObj().dimension().location());
        }

        private static void unload(PowerTile tile) {
            IEnergyTile energyTile = EnergyNet.INSTANCE.getTile(tile.getLevel(), tile.getBlockPos());
            // Check the instance
            if (energyTile instanceof PowerTileEnergySink sink && sink.tile() == tile) {
                EnergyNet.INSTANCE.removeTile(sink);
                LOGGER.trace("Unregistered {} as IC2 tile at {} in {}",
                    tile.getClass().getSimpleName(), sink.getPosition(), sink.getWorldObj().dimension().location());
            }
            // energyTile is often null, and it may be expected.
        }
    }
}
