package com.yogpc.qp.machines;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
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
            LOGGER = CONTEXT.getLogger("TraceQuarryWork");
            MARKER = MarkerManager.getMarker("QUARRY_TRACE");
        } else {
            CONTEXT = null;
            LOGGER = null;
            MARKER = null;
        }
    }

    public static void startWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        if (enabled)
            LOGGER.info(MARKER, "{} Started work with {} FE", header(tile, pos), energyInMachine);
    }

    public static void changeTarget(PowerTile tile, BlockPos pos, String state) {
        if (enabled)
            LOGGER.debug(MARKER, "{} Target changed in {}", header(tile, pos), state);
    }

    public static void progress(PowerTile tile, BlockPos pos, BlockPos targetPos, String reason) {
        if (enabled)
            LOGGER.debug(MARKER, "{} ({},{},{}) {}", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), reason);
    }

    public static void canBreakCheck(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, String detail) {
        if (enabled) {
            LOGGER.info(MARKER, "{} ({},{},{}) {} State({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), detail, state);
        }
    }

    public static void blockRemoveFailed(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, BreakResult breakResult) {
        if (enabled)
            LOGGER.info(MARKER, "{} ({},{},{}) {} {}", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), breakResult, state);
    }

    public static void blockRemoveSucceed(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, List<ItemStack> drops, int exp) {
        if (enabled) {
            LOGGER.debug(MARKER, "{} ({},{},{}) SUCCESS {} EXP={} ({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), state, exp,
                drops.stream().map(s -> "%dx %s".formatted(s.getCount(), ForgeRegistries.ITEMS.getKey(s.getItem()))).collect(Collectors.joining(",")));
        }
    }

    public static void unexpected(PowerTile tile, BlockPos pos, String reason) {
        if (enabled)
            LOGGER.warn(MARKER, "{} {}", header(tile, pos), reason);
    }

    public static void finishWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        if (enabled)
            LOGGER.info(MARKER, "{} Finished work with {} FE", header(tile, pos), energyInMachine);
    }

    private static String header(PowerTile tile, BlockPos pos) {
        return "[%s(%d,%d,%d)]".formatted(tile.getClass().getSimpleName(), pos.getX(), pos.getY(), pos.getZ());
    }

}
