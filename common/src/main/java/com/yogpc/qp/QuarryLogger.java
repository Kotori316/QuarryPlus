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

    // log4j2 Markers — for use with QuarryLogger.LOGGER
    public static final org.apache.logging.log4j.Marker LOG4J_QUARRY = org.apache.logging.log4j.MarkerManager.getMarker("quarry");
    public static final org.apache.logging.log4j.Marker LOG4J_ADV_QUARRY = org.apache.logging.log4j.MarkerManager.getMarker("advQuarry");
    public static final org.apache.logging.log4j.Marker LOG4J_ENERGY_TICK = org.apache.logging.log4j.MarkerManager.getMarker("TickLog");
    public static final org.apache.logging.log4j.Marker LOG4J_ENERGY_TOTAL = org.apache.logging.log4j.MarkerManager.getMarker("Total");
    public static final org.apache.logging.log4j.Marker LOG4J_CHUNK_LOADER = org.apache.logging.log4j.MarkerManager.getMarker("QuarryChunkLoader");

    // SLF4j Markers — for use with QuarryPlus.LOGGER
    public static final org.slf4j.Marker SLF4J_QUARRY = org.slf4j.MarkerFactory.getMarker("quarry");
    public static final org.slf4j.Marker SLF4J_ADV_QUARRY = org.slf4j.MarkerFactory.getMarker("advQuarry");

    private static final class DummyClassLoader extends SecureClassLoader {
    }

    static {
        try {
            Logger logger;
            final var configUri = QuarryLogger.class.getResource("/quarryplus-log4j2.xml").toURI();
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
