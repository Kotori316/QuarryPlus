package com.yogpc.qp.fabric.config;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class QuarryConfigFabricTest extends BeforeMC {
    @TempDir
    Path dir;

    @Test
    void loadEmptyFile() throws IOException {
        var path = dir.resolve("quarryplus.toml");
        var config = assertDoesNotThrow(() -> QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FABRIC, false));

        assertAll(
            () -> assertFalse(config.debug()),
            () -> assertEquals(1d / 16d, config.rebornEnergyConversionCoefficient()),
            () -> assertEquals(256, config.markerPlusRange()),
            () -> assertNotNull(config.powerMap())
        );
        var written = Files.readString(path);
        assertTrue(written.contains("rebornEnergyConversionCoefficient"));
        assertTrue(written.contains("#"), "Comments must be written: " + written);
    }

    @Test
    void loadModifiedFile() throws IOException {
        var path = dir.resolve("quarryplus.toml");
        Files.writeString(path, """
            debug = true
            rebornEnergyConversionCoefficient = 0.25
            markerPlusRange = 128
            [powerMap.quarry]
            maxEnergy = 123.0
            """);
        var config = QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FABRIC, false);

        assertAll(
            () -> assertTrue(config.debug()),
            () -> assertEquals(0.25d, config.rebornEnergyConversionCoefficient()),
            () -> assertEquals(128, config.markerPlusRange()),
            () -> assertEquals(123d, config.powerMap().quarry().maxEnergy())
        );
    }

    @Test
    void outOfRangeIsCorrected() throws IOException {
        var path = dir.resolve("quarryplus.toml");
        Files.writeString(path, "markerPlusRange = 1\n");
        var config = QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FABRIC, false);

        // Invalid values are reset to the default one.
        assertEquals(256, config.markerPlusRange());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void loadDigFromMaxY(boolean value) throws IOException {
        var path = dir.resolve("quarryplus.toml");
        Files.writeString(path, "digFromMaxY = %s\n".formatted(value));
        var config = QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FABRIC, false);

        assertEquals(value, config.digFromMaxY());
    }

    @Test
    void notFabricIgnoresReborn() throws IOException {
        var path = dir.resolve("quarryplus.toml");
        Files.writeString(path, "rebornEnergyConversionCoefficient = 0.25\n");
        var config = QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FORGE, false);

        assertEquals(1d / 16d, config.rebornEnergyConversionCoefficient());
    }
}
