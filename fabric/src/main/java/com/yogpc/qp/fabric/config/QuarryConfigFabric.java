package com.yogpc.qp.fabric.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryLogger;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.DefinedQuarryConfig;
import com.yogpc.qp.config.QuarryConfig;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;

public final class QuarryConfigFabric {
    /// Load the config file. If the file doesn't exist or has invalid values, they are corrected and written to the file.
    public static QuarryConfig load(Path path, BooleanSupplier inDevelop, PlatformAccess.ModdingPlatform platform) {
        var config = CommentedFileConfig.builder(path)
            .preserveInsertionOrder()
            .async()
            .autosave()
            .autoreload()
            .onLoad(() -> QuarryPlus.LOGGER.info("Config loaded from {}", path))
            .onAutoSave(() -> QuarryPlus.LOGGER.info("Config saved to {}", path))
            .build();
        config.load();

        var definer = new NightConfigDefiner(config);
        var quarryConfig = new DefinedQuarryConfig(definer, inDevelop, platform);
        // Save the file only once after all values are corrected.
        config.bulkCommentedUpdate(definer::correct);

        QuarryLogger.applyLogLevel(quarryConfig.debug());
        return quarryConfig;
    }
}
