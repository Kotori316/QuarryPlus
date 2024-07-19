package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.api.ModInitializer;

public final class QuarryPlusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        QuarryPlus.LOGGER.info("Initialize Common");
        PlatformAccessFabric.RegisterObjectsFabric.registerAll();
        QuarryPlus.LOGGER.info("Initialize Common finished");
    }

}
