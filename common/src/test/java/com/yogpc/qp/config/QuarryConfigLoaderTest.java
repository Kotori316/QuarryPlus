package com.yogpc.qp.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class QuarryConfigLoaderTest {

    @Test
    void loadEmpty() {
        var loaded = assertDoesNotThrow(() -> QuarryConfigLoader.load(Config.inMemory()));
        assertNotNull(loaded);

        assertAll(
            () -> assertFalse(loaded.debug()),
            () -> assertNotNull(loaded.powerMap()),
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
            () -> assertTrue(loaded.debug()),
            () -> assertNotNull(loaded.powerMap()),
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getDefault(boolean debug) {
        var loaded = QuarryConfig.defaultConfig(debug);
        assertNotNull(loaded);

        assertEquals(debug, loaded.debug());
    }

    @Test
    void realEmptyFile(@TempDir Path parent) {
        var configPath = parent.resolve("quarry-config.toml");
        var loaded = assertDoesNotThrow(() -> QuarryConfig.load(configPath, () -> false));
        assertNotNull(loaded);
    }
}
