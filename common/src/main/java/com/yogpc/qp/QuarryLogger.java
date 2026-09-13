package com.yogpc.qp;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.URISyntaxException;
import java.security.SecureClassLoader;

public final class QuarryLogger {

    private static final LoggerContext LOGGER_CONTEXT;
    public static final Logger LOGGER;

    private static final class DummyClassLoader extends SecureClassLoader {
    }

    static {
        try {
            Logger logger;
            final var configUri = QuarryLogger.class.getResource("quarryplus-log4j2.xml").toURI();
            LOGGER_CONTEXT = Configurator.initialize(QuarryPlus.modID + "-config", new DummyClassLoader(), configUri);
            final var logName = QuarryPlus.MOD_NAME + "Debug";
            if (LOGGER_CONTEXT != null) {
                logger = LOGGER_CONTEXT.getLogger(logName);
            } else {
                logger = org.apache.logging.log4j.LogManager.getLogger(logName);
            }
            LOGGER = logger;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void applyLogLevel(boolean debugMode) {
        if (LOGGER_CONTEXT == null) return;

        var level = debugMode ? Level.DEBUG : Level.INFO;
        LOGGER_CONTEXT.getConfiguration()
            .getLoggerConfig(LOGGER.getName())
            .setLevel(level);

        LOGGER_CONTEXT.updateLoggers();
    }
}
