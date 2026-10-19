package com.yogpc.qp.fabric.config;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        var config = assertDoesNotThrow(() -> QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FABRIC));

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
        var config = QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FABRIC);

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
        var config = QuarryConfigFabric.load(path, () -> false, PlatformAccess.ModdingPlatform.FABRIC);

        // Invalid values are reset to the default one.
        assertEquals(256, config.markerPlusRange());
    }
}
