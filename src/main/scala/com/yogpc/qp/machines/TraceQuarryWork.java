package com.yogpc.qp.machines;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.GsonBuilder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.security.SecureClassLoader;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class TraceQuarryWork {
    public static final boolean enabled;
    private static final LoggerContext CONTEXT;
    private static final Logger LOGGER;
    private static final Marker WARNING_MARKER;

    private static class DummyLoader extends SecureClassLoader {
    }

    static {
        enabled = QuarryPlus.config.common.logAllQuarryWork.get();
        CONTEXT = Configurator.initialize("quarryplus-config", new DummyLoader(),
            URI.create(Objects.requireNonNull(TraceQuarryWork.class.getResource("/quarry-log4j2.xml")).toString())
        );
        Logger t;
        if (CONTEXT != null) {
            // temporal variable to set level, as org.apache.logging.log4j.Logger doesn't provide setters.
            var l = CONTEXT.getLogger("TQW");
            if (!enabled) {
                l.setLevel(Level.WARN);
            }
            t = l;
        } else {
            // I don't know what is happening, but I should care this case
            t = QuarryPlus.LOGGER;
        }
        LOGGER = t;
        WARNING_MARKER = MarkerManager.getMarker("QUARRY_WARNING");
    }

    private static final Marker MARKER_START_WORK = MarkerManager.getMarker("startWork");

    public static void startWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        LOGGER.info(MARKER_START_WORK, "{} Started work with {} FE", header(tile, pos), energyInMachine);
    }

    private static final Marker MARKER_CHANGE_TARGET = MarkerManager.getMarker("changeTarget");

    public static void changeTarget(PowerTile tile, BlockPos pos, String state, String target) {
        LOGGER.debug(MARKER_CHANGE_TARGET, "{} In {} to {}", header(tile, pos), state, target);
    }

    private static final Marker MARKER_CHANGE_STATE = MarkerManager.getMarker("changeState");

    public static void changeState(PowerTile tile, BlockPos pos, String oldState, String newState) {
        LOGGER.debug(MARKER_CHANGE_STATE, "{} From {} to {}", header(tile, pos), oldState, newState);
    }

    private static final Marker MARKER_PROGRESS = MarkerManager.getMarker("progress");

    public static void progress(PowerTile tile, BlockPos pos, BlockPos targetPos, String reason) {
        LOGGER.debug(MARKER_PROGRESS, "{} ({},{},{}) {}", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), reason);
    }

    private static final Marker MARKER_CAN_BREAK_CHECK = MarkerManager.getMarker("canBreakCheck");

    public static void canBreakCheck(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, String detail) {
        LOGGER.info(MARKER_CAN_BREAK_CHECK, "{} ({},{},{}) {} State({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), detail, state);
    }

    private static final Marker MARKER_BLOCK_REMOVE_FAILED = MarkerManager.getMarker("blockRemoveFailed");

    public static void blockRemoveFailed(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, BreakResult breakResult, Object... additional) {
        LOGGER.info(MARKER_BLOCK_REMOVE_FAILED, "{} ({},{},{}) {} {} {}", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), breakResult, state, additional);
    }

    private static final Marker MARKER_BLOCK_REMOVE_SUCCEED = MarkerManager.getMarker("removeSuccess");

    public static void blockRemoveSucceed(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, List<ItemStack> drops, int exp, long consumedEnergy) {
        LOGGER.debug(MARKER_BLOCK_REMOVE_SUCCEED, "{} ({},{},{}) {} FE {} EXP={} ({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), consumedEnergy / PowerTile.ONE_FE, state, exp,
            drops.stream().map(s -> "%dx %s".formatted(s.getCount(), ForgeRegistries.ITEMS.getKey(s.getItem()))).collect(Collectors.joining(",")));
    }

    public static void blockRemoveSucceed(PowerTile tile, BlockPos pos, BlockPos targetPos, List<BlockState> state, Map<ItemKey, Long> drops, int exp, long consumedEnergy) {
        var stateCount = state.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        LOGGER.debug(MARKER_BLOCK_REMOVE_SUCCEED, "{} ({},{},{}) {} FE {} EXP={} ({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), consumedEnergy / PowerTile.ONE_FE, stateCount, exp,
            drops.entrySet().stream().map(s -> "%dx %s".formatted(s.getValue(), s.getKey().getId())).collect(Collectors.joining(",")));
    }

    private static final Marker MARKER_UNEXPECTED = MarkerManager.getMarker("unexpected");

    public static void unexpected(PowerTile tile, BlockPos pos, String reason) {
        LOGGER.warn(MARKER_UNEXPECTED, "{} {}", header(tile, pos), reason);
    }

    private static final Marker MARKER_FINISH_WORK = MarkerManager.getMarker("finishWork");

    public static void finishWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        LOGGER.info(MARKER_FINISH_WORK, "{} Finished work with {} FE", header(tile, pos), energyInMachine);
    }

    private static final Marker MARKER_CONVERT_ITEM = MarkerManager.getMarker("convertItem");

    public static void convertItem(ItemKey before, ItemKey after) {
        LOGGER.debug(MARKER_CONVERT_ITEM, "Convert {} to {}", before, after);
    }

    private static final Marker MARKER_TRANSFER_ITEM = MarkerManager.getMarker("transferItem");

    public static void transferItem(@Nullable BlockEntity from, @Nullable IItemHandler dest, ItemKey itemKey, int count) {
        String header = from != null ? header(from, from.getBlockPos()) : "ItemHandler Extraction";
        LOGGER.debug(MARKER_TRANSFER_ITEM, "{} Transfer {}x {} to {}", header, count, itemKey, dest);
    }

    private static final Marker MARKER_TRANSFER_FLUID = MarkerManager.getMarker("transferFluid");

    public static void transferFluid(@Nullable BlockEntity from, @Nullable IFluidHandler dest, FluidKey fluidKey, int amount) {
        String header = from != null ? header(from, from.getBlockPos()) : "FluidHandler Extraction";
        LOGGER.debug(MARKER_TRANSFER_FLUID, "{} Transfer {}mB of {} to {}", header, amount, fluidKey, dest);
    }

    private static final Marker MARKER_NO_DROPS = MarkerManager.getMarker("noDrops");

    public static void noDrops(BlockState state, BlockPos pos, ItemStack tool) {
        LOGGER.debug(MARKER_NO_DROPS, "{} at ({},{},{}) has no drops with {}", state, pos.getX(), pos.getY(), pos.getZ(), tool);
    }

    private static String header(BlockEntity tile, BlockPos pos) {
        return "[%s(%d,%d,%d)]".formatted(tile.getClass().getSimpleName(), pos.getX(), pos.getY(), pos.getZ());
    }

    // No meaning for value, just for existence check
    private static final Cache<String, String> knownKeys = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    /**
     * Log given message once in a minute.
     */
    public static void logOnceIn10Minutes(String key, Supplier<String> message, @Nullable Supplier<? extends Throwable> error) {
        if (knownKeys.getIfPresent(key) == null) {
            knownKeys.put(key, key);
            var msg = "[" + key + "] " + message.get();
            if (error == null) {
                QuarryPlus.LOGGER.warn(msg);
                LOGGER.warn(WARNING_MARKER, msg);
            } else {
                QuarryPlus.LOGGER.error(msg, error.get());
                LOGGER.error(WARNING_MARKER, msg, error.get());
            }
        }
    }

    private static final Marker MARKER_INITIAL_LOG = MarkerManager.getMarker("initialLog");

    public static void initialLog(MinecraftServer server) {
        var gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        var config = Map.of("common", QuarryPlus.config.getAll(), "server", QuarryPlus.serverConfig.getAll());
        LOGGER.warn(MARKER_INITIAL_LOG, "Config in '{}'{}{}", server.getMotd(), System.lineSeparator(), gson.toJson(config));
    }
}
