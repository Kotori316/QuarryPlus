package com.yogpc.qp.machines;

import java.net.URI;
import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

public final class TraceQuarryWork {
    private static final boolean enabled;
    private static final LoggerContext CONTEXT;
    private static final Logger LOGGER;
    private static final Marker MARKER;

    static {
        enabled = QuarryPlus.config.common.logAllQuarryWork.get();
        if (enabled) {
            CONTEXT = Configurator.initialize("quarryplus-config", null,
                URI.create(Objects.requireNonNull(TraceQuarryWork.class.getResource("/quarry-log4j2.xml")).toString())
            );
            LOGGER = CONTEXT.getLogger("Test1");
            MARKER = MarkerManager.getMarker("QUARRY_TRACE");
        } else {
            CONTEXT = null;
            LOGGER = null;
            MARKER = null;
        }
    }

    public static void startWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        if (enabled)
            LOGGER.debug(MARKER, "{} Started work with {} FE", header(tile, pos), energyInMachine);
    }

    public static void finishWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        if (enabled)
            LOGGER.debug(MARKER, "{} Finished work with {} FE", header(tile, pos), energyInMachine);
    }

    private static String header(PowerTile tile, BlockPos pos) {
        return "[%s(%d,%d,%d)]".formatted(tile.getClass().getSimpleName(), pos.getX(), pos.getY(), pos.getZ());
    }

}
