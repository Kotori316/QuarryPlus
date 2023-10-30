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
    private static final Marker MARKER;
    private static final Marker WARNING_MARKER;

    private static class DummyLoader extends SecureClassLoader {
    }

    static {
        enabled = QuarryPlus.config.common.logAllQuarryWork.get();
        CONTEXT = Configurator.initialize("quarryplus-config", new DummyLoader(),
            URI.create(Objects.requireNonNull(TraceQuarryWork.class.getResource("/quarry-log4j2.xml")).toString())
        );
        // temporal variable to set level, as org.apache.logging.log4j.Logger doesn't provide setters.
        var l = CONTEXT.getLogger("TQW");
        LOGGER = l;
        MARKER = MarkerManager.getMarker("QUARRY_TRACE");
        WARNING_MARKER = MarkerManager.getMarker("QUARRY_WARNING");
        if (!enabled) {
            l.setLevel(Level.WARN);
        }
    }

    public static void startWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        LOGGER.info(MARKER, "{} Started work with {} FE", header(tile, pos), energyInMachine);
    }

    public static void changeTarget(PowerTile tile, BlockPos pos, String state) {
        LOGGER.debug(MARKER, "{} Target changed in {}", header(tile, pos), state);
    }

    public static void progress(PowerTile tile, BlockPos pos, BlockPos targetPos, String reason) {
        LOGGER.debug(MARKER, "{} ({},{},{}) {}", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), reason);
    }

    public static void canBreakCheck(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, String detail) {
        LOGGER.info(MARKER, "{} ({},{},{}) {} State({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), detail, state);
    }

    public static void blockRemoveFailed(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, BreakResult breakResult) {
        LOGGER.info(MARKER, "{} ({},{},{}) {} {}", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), breakResult, state);
    }

    public static void blockRemoveSucceed(PowerTile tile, BlockPos pos, BlockPos targetPos, BlockState state, List<ItemStack> drops, int exp, long consumedEnergy) {
        LOGGER.debug(MARKER, "{} ({},{},{}) SUCCESS({} FE) {} EXP={} ({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), consumedEnergy / PowerTile.ONE_FE, state, exp,
            drops.stream().map(s -> "%dx %s".formatted(s.getCount(), ForgeRegistries.ITEMS.getKey(s.getItem()))).collect(Collectors.joining(",")));
    }

    public static void blockRemoveSucceed(PowerTile tile, BlockPos pos, BlockPos targetPos, List<BlockState> state, Map<ItemKey, Long> drops, int exp, long consumedEnergy) {
        var stateCount = state.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        LOGGER.debug(MARKER, "{} ({},{},{}) SUCCESS({} FE) {} EXP={} ({})", header(tile, pos), targetPos.getX(), targetPos.getY(), targetPos.getZ(), consumedEnergy / PowerTile.ONE_FE, stateCount, exp,
            drops.entrySet().stream().map(s -> "%dx %s".formatted(s.getValue(), s.getKey().getId())).collect(Collectors.joining(",")));
    }

    public static void unexpected(PowerTile tile, BlockPos pos, String reason) {
        LOGGER.warn(MARKER, "{} {}", header(tile, pos), reason);
    }

    public static void finishWork(PowerTile tile, BlockPos pos, int energyInMachine) {
        LOGGER.info(MARKER, "{} Finished work with {} FE", header(tile, pos), energyInMachine);
    }

    public static void convertItem(ItemKey before, ItemKey after) {
        LOGGER.debug(MARKER, "Convert {} to {}", before, after);
    }

    public static void transferItem(@Nullable BlockEntity from, @Nullable IItemHandler dest, ItemKey itemKey, int count) {
        String header = from != null ? header(from, from.getBlockPos()) : "ItemHandler Extraction";
        LOGGER.debug(MARKER, "{} Transfer {}x {} to {}", header, count, itemKey, dest);
    }

    public static void transferFluid(@Nullable BlockEntity from, @Nullable IFluidHandler dest, FluidKey fluidKey, int amount) {
        String header = from != null ? header(from, from.getBlockPos()) : "FluidHandler Extraction";
        LOGGER.debug(MARKER, "{} Transfer {}mB of {} to {}", header, amount, fluidKey, dest);
    }

    public static void noDrops(BlockState state, BlockPos pos, ItemStack tool) {
        LOGGER.debug(MARKER, "{} at ({},{},{}) has no drops with {}", state, pos.getX(), pos.getY(), pos.getZ(), tool);
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

    public static void initialLog(MinecraftServer server) {
        var gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        var config = Map.of("common", QuarryPlus.config.getAll(), "server", QuarryPlus.serverConfig.getAll());
        LOGGER.warn(MARKER, "Config {} {}", server.getMotd(), gson.toJson(config));
    }
}
