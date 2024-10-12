package com.yogpc.qp.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerMap;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;

public interface QuarryConfig {
    boolean debug();

    boolean noEnergy();

    PowerMap powerMap();

    EnableMap enableMap();

    double rebornEnergyConversionCoefficient();

    boolean removeBedrockOnNetherTop();

    boolean enableChunkLoader();

    boolean convertDeepslateOres();

    static QuarryConfig load(Path path, BooleanSupplier inDevelop) {
        try (var config = CommentedFileConfig.builder(path)
            .preserveInsertionOrder()
            .sync()
            .autosave()
            .onLoad(() -> QuarryPlus.LOGGER.info("Config loaded from {}", path))
            .onAutoSave(() -> QuarryPlus.LOGGER.info("Config saved to {}", path))
            .build()
        ) {
            config.load();
            return QuarryConfigLoader.load(config, inDevelop);
        }
    }

    @VisibleForTesting
    static QuarryConfig defaultConfig(boolean debug) {
        return fromConfig(Config.inMemory(), debug);
    }

    @VisibleForTesting
    static QuarryConfig fromConfig(Config config, boolean debug) {
        return QuarryConfigLoader.load(config, () -> debug);
    }
}
