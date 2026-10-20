package com.yogpc.qp.fabric.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.DefinedQuarryConfig;
import com.yogpc.qp.config.QuarryConfig;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public final class QuarryConfigFabric {
    /// Load the config file. If the file doesn't exist or has invalid values, they are corrected and written to the file.
    /// The file is saved in the background and reloaded automatically when it's modified.
    public static QuarryConfig load(Path path, BooleanSupplier inDevelop, PlatformAccess.ModdingPlatform platform) {
        return load(path, inDevelop, platform, true);
    }

    /// @param background `false` to save the file synchronously and not to watch the file. It's for tests.
    static QuarryConfig load(Path path, BooleanSupplier inDevelop, PlatformAccess.ModdingPlatform platform, boolean background) {
        // The config doesn't exist yet when the listener is created, so the action is set later.
        var onLoad = new AtomicReference<Runnable>(() -> {
        });
        var builder = CommentedFileConfig.builder(path)
            .preserveInsertionOrder()
            .autosave()
            .onLoad(() -> {
                QuarryPlus.LOGGER.info("Config loaded from {}", path);
                onLoad.get().run();
            })
            .onAutoSave(() -> QuarryPlus.LOGGER.info("Config saved to {}", path));
        if (background) {
            builder.async().autoreload();
        } else {
            builder.sync();
        }
        var config = builder.build();

        var definer = new NightConfigDefiner(config);
        var quarryConfig = new DefinedQuarryConfig(definer, inDevelop, platform);
        // Called on the first load and on every reloading.
        onLoad.set(() -> {
            // Correcting the config saves the file, which triggers reloading again. Do it only if it's required.
            if (!definer.isCorrect(config)) {
                // Use the version returning a value. The Consumer version doesn't trigger the autosave.
                config.bulkCommentedUpdate(c -> {
                    definer.correct(c);
                    return null;
                });
            }
            QuarryConfig.onConfigLoad(quarryConfig);
        });
        config.load();
        return quarryConfig;
    }
}
