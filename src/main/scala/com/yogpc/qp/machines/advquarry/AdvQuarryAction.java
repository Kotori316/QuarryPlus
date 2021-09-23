package com.yogpc.qp.machines.advquarry;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.BreakResult;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import javax.annotation.Nullable;
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
                new WaitingSerializer(),
                new MakeFrameSerializer(),
                new BreakBlockSerializer(),
                new FinishedSerializer()
            ).map(s -> Map.entry(s.key(), s))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    final CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.putString("type", key());
        return writeDetail(tag);
    }

    static AdvQuarryAction fromNbt(CompoundTag tag, TileAdvQuarry quarry) {
        var key = tag.getString("type");
        return Optional.ofNullable(SERIALIZER_MAP.get(key))
            .map(s -> s.fromTag(tag, quarry))
            .orElseGet(() -> {
                if (!tag.isEmpty())
                    LOGGER.error("Unknown type '{}' found in tag: {}", key, tag);
                return Waiting.WAITING;
            });
    }

    abstract CompoundTag writeDetail(CompoundTag tag);

    abstract String key();

    @Override
    public String toString() {
        return key();
    }

    @Override
    public abstract void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry);

    @Nullable
    static <T> T skipIterator(Iterator<T> iterator, Predicate<T> skipCondition) {
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (!skipCondition.test(next)) {
                return next;
            }
        }
        return null;
    }

    abstract static class Serializer {
        abstract String key();

        abstract AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry);
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
            if (quarry.getEnergy() > quarry.getMaxEnergy() / 4) {
                quarry.action = new MakeFrame(quarry.getArea());
            }
        }
    }

    private static class WaitingSerializer extends Serializer {

        @Override
        String key() {
            return "Waiting";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            return Waiting.WAITING;
        }
    }

    public static class MakeFrame extends AdvQuarryAction {
        private Iterator<BlockPos> posIterator;
        @Nullable
        private BlockPos current;

        public MakeFrame(Area area) {
            this.posIterator = Area.getFramePosStream(area).iterator();
            this.current = this.posIterator.next();
        }

        MakeFrame(Area area, @Nullable BlockPos current) {
            this.posIterator = Area.getFramePosStream(area).dropWhile(Predicate.isEqual(current).negate()).iterator();
            if (!this.posIterator.hasNext()) {
                this.posIterator = Area.getFramePosStream(area).iterator();
                this.current = this.posIterator.next();
            } else {
                this.current = current;
            }
        }

        @Override
        CompoundTag writeDetail(CompoundTag tag) {
            if (current != null)
                tag.putLong("current", current.asLong());
            return tag;
        }

        @Override
        String key() {
            return "MakeFrame";
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
            for (int i = 0; i < 4; i++) {
                if (current != null) {
                    var result = quarry.breakOneBlock(current, true);
                    if (result.isSuccess()) {
                        if (result == BreakResult.FAIL_EVENT) {
                            // Not breakable. Go next.
                            current = skipIterator(this.posIterator, MakeFrame.skipFramePlace(quarry));
                        } else if (quarry.useEnergy(PowerManager.getMakeFrameEnergy(quarry), PowerTile.Reason.MAKE_FRAME, false)) {
                            quarry.getTargetWorld().setBlockAndUpdate(current, Holder.BLOCK_FRAME.defaultBlockState());
                            current = skipIterator(this.posIterator, MakeFrame.skipFramePlace(quarry));
                        }
                    }
                } else {
                    // Go to the next work.
                    quarry.action = new BreakBlock(quarry);
                    break;
                }
            }
        }

        static Predicate<BlockPos> skipFramePlace(TileAdvQuarry quarry) {
            var world = quarry.getTargetWorld();
            assert world != null; // This must be called in tick update.
            return pos -> {
                var state = world.getBlockState(pos);
                return state.is(Holder.BLOCK_FRAME) // Frame
                    || !quarry.canBreak(world, pos, state) // Unbreakable
                    || pos.equals(quarry.getBlockPos()) // This machine
                    ;
            };
        }
    }

    private static class MakeFrameSerializer extends Serializer {

        @Override
        String key() {
            return "MakeFrame";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            var pos = tag.contains("current") ? BlockPos.of(tag.getLong("current")) : null;
            return new MakeFrame(quarry.getArea(), pos);
        }
    }

    public static class BreakBlock extends AdvQuarryAction {
        private final TargetIterator iterator;

        public BreakBlock(TileAdvQuarry quarry) {
            assert quarry.getArea() != null;
            this.iterator = TargetIterator.of(quarry.getArea());
        }

        BreakBlock(TileAdvQuarry quarry, int x, int z) {
            assert quarry.getArea() != null;
            this.iterator = TargetIterator.of(quarry.getArea());
            this.iterator.setCurrent(new TargetIterator.XZPair(x, z));
        }

        @Override
        CompoundTag writeDetail(CompoundTag tag) {
            var xzPair = iterator.peek();
            tag.putInt("currentX", xzPair.x());
            tag.putInt("currentZ", xzPair.z());
            return tag;
        }

        @Override
        String key() {
            return "BreakBlock";
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
            var target = iterator.peek();
            var result = quarry.breakBlocks(target.x(), target.z());
            if (result.isSuccess()) {
                iterator.next();
                if (!iterator.hasNext()) {
                    // Go to the next work.
                    quarry.action = Finished.FINISHED;
                }
            }
        }
    }

    private static class BreakBlockSerializer extends Serializer {

        @Override
        String key() {
            return "BreakBlock";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            int x = tag.getInt("currentX");
            int z = tag.getInt("currentZ");
            return new BreakBlock(quarry, x, z);
        }
    }

    public static class Finished extends AdvQuarryAction {
        public static final Finished FINISHED = new Finished();

        private Finished() {
        }

        @Override
        CompoundTag writeDetail(CompoundTag tag) {
            return tag;
        }

        @Override
        String key() {
            return "Finished";
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
        }
    }

    private static class FinishedSerializer extends Serializer {

        @Override
        String key() {
            return "Finished";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            return Finished.FINISHED;
        }
    }
}
