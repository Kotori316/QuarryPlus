package com.yogpc.qp.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuarryConfigLoaderTest {

    @Test
    void loadEmpty() {
        var loaded = assertDoesNotThrow(() -> QuarryConfigLoader.load(Config.inMemory()));
        assertNotNull(loaded);

        assertAll(
            () -> assertFalse(loaded.isDebug()),
            () -> assertNotNull(loaded.getPowerMap()),
            () -> assertEquals(1d / 16d, loaded.rebornEnergyConversionCoefficient())
        );
    }

    @Test
    void loadConfig() {
        var config = Config.inMemory();
        config.set("debug", true);
        config.set("rebornEnergyConversionCoefficient", 0.25d);
        var loaded = assertDoesNotThrow(() -> QuarryConfigLoader.load(config));
        assertNotNull(loaded);

        assertAll(
            () -> assertTrue(loaded.isDebug()),
            () -> assertNotNull(loaded.getPowerMap()),
            () -> assertEquals(4d / 16d, loaded.rebornEnergyConversionCoefficient())
        );
    }

    @Test
    void loadCommented() {
        var config = CommentedConfig.inMemory();
        var loaded = assertDoesNotThrow(() -> QuarryConfigLoader.load(config));
        assertNotNull(loaded);

        assertFalse(config.commentMap().isEmpty());
    }
}
