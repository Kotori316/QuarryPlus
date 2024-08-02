package com.yogpc.qp.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerMap;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;

public interface QuarryConfig {
    boolean debug();

    PowerMap powerMap();

    double rebornEnergyConversionCoefficient();

    static QuarryConfig load(Path path, BooleanSupplier inDevelop) {
        try (var config = CommentedFileConfig.builder(path)
            .preserveInsertionOrder()
            .autosave()
            .onLoad(() -> QuarryPlus.LOGGER.info("Config loaded from {}", path))
            .onAutoSave(() -> QuarryPlus.LOGGER.info("Config saved to {}", path))
            .build()
        ) {
            config.load();
            return QuarryConfigLoader.load(config, inDevelop);
        }
    }
}
