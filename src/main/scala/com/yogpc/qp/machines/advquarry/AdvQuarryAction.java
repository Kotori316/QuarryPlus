package com.yogpc.qp.machines.advquarry;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AdvQuarryAction implements BlockEntityTicker<TileAdvQuarry> {
    private static final Logger LOGGER = LogManager.getLogger(AdvQuarryAction.class);
    static final Map<String, Serializer> SERIALIZER_MAP;

    static {
        SERIALIZER_MAP = Stream.of(
                new WaitingSerializer()
            ).map(s -> Map.entry(s.key(), s))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    final CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.putString("type", key());
        return writeDetail(tag);
    }

    static AdvQuarryAction fromNbt(CompoundTag tag) {
        var key = tag.getString("type");
        return Optional.ofNullable(SERIALIZER_MAP.get(key))
            .map(s -> s.fromTag(tag))
            .orElseGet(() -> {
                if (!tag.isEmpty())
                    LOGGER.error("Unknown type '{}' found in tag: {}", key, tag);
                return Waiting.WAITING;
            });
    }

    abstract CompoundTag writeDetail(CompoundTag tag);

    abstract String key();

    @Override
    public abstract void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry);

    abstract static class Serializer {
        abstract String key();

        abstract AdvQuarryAction fromTag(CompoundTag tag);
    }

    public static class Waiting extends AdvQuarryAction {
        public static final Waiting WAITING = new Waiting();

        private Waiting() {
        }

        @Override
        String key() {
            return "Waiting";
        }

        @Override
        CompoundTag writeDetail(CompoundTag tag) {
            return new CompoundTag();
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
        }
    }

    private static class WaitingSerializer extends Serializer {

        @Override
        String key() {
            return "Waiting";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag) {
            return Waiting.WAITING;
        }
    }

    public static class MakeFrame extends AdvQuarryAction {

        @Override
        CompoundTag writeDetail(CompoundTag tag) {
            return null;
        }

        @Override
        String key() {
            return "MakeFrame";
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {

        }
    }
}
