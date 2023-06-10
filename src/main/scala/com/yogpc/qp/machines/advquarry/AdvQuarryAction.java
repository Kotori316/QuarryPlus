package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.*;
import com.yogpc.qp.machines.filler.FillerAction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yogpc.qp.machines.advquarry.AdvQuarry.ACTION;
import static com.yogpc.qp.machines.advquarry.AdvQuarry.LOGGER;

public abstract class AdvQuarryAction implements BlockEntityTicker<TileAdvQuarry> {
    static final Map<String, Serializer> SERIALIZER_MAP;

    static {
        SERIALIZER_MAP = Stream.of(
                new WaitingSerializer(),
                new MakeFrameSerializer(),
                new BreakBlockSerializer(),
                new CheckFluidSerializer(),
                new FillerWorkSerializer(),
                new FinishedSerializer()
        ).collect(Collectors.toMap(Serializer::key, Function.identity()));
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
                        LOGGER.error(ACTION, "Unknown type '{}' found in tag: {}", key, tag);
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

    TargetIterator createTargetIterator(Area area, boolean chunkByChunk) {
        return TargetIterator.of(area, chunkByChunk);
    }

    abstract static class Serializer {
        abstract String key();

        abstract AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry);
    }

    public static final class Waiting extends AdvQuarryAction {
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
            if (quarry.getEnergy() > quarry.getMaxEnergy() / 4 && quarry.canStartWork()) {
                startQuarry(quarry);
            }
        }
    }

    static void startQuarry(TileAdvQuarry quarry) {
        if (quarry.workConfig.placeAreaFrame()) {
            quarry.setAction(new MakeFrame(quarry.getArea()));
        } else {
            quarry.setAction(new BreakBlock(quarry));
        }
        TraceQuarryWork.startWork(quarry, quarry.getBlockPos(), quarry.getEnergyStored());
    }

    private static final class WaitingSerializer extends Serializer {

        @Override
        String key() {
            return "Waiting";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            return Waiting.WAITING;
        }
    }

    public static final class MakeFrame extends AdvQuarryAction {
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
                    var targetState = quarry.getTargetWorld().getBlockState(current);
                    BreakResult result;
                    if (targetState.isAir()) {
                        result = BreakResult.SUCCESS;
                    } else {
                        result = quarry.breakOneBlock(current, true);
                    }
                    if (result.isSuccess()) {
                        if (result == BreakResult.FAIL_EVENT || result == BreakResult.SKIPPED) {
                            // Not breakable. Go next.
                            current = skipIterator(this.posIterator, MakeFrame.skipFramePlace(quarry));
                        } else if (quarry.useEnergy(PowerManager.getMakeFrameEnergy(quarry), PowerTile.Reason.MAKE_FRAME, false)) {
                            quarry.getTargetWorld().setBlockAndUpdate(current, Holder.BLOCK_FRAME.defaultBlockState());
                            current = skipIterator(this.posIterator, MakeFrame.skipFramePlace(quarry));
                        }
                    }
                } else {
                    // Go to the next work.
                    quarry.setAction(new BreakBlock(quarry));
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

    private static final class MakeFrameSerializer extends Serializer {

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

    public static final class BreakBlock extends AdvQuarryAction {
        private final TargetIterator iterator;
        private boolean searchEnergyConsumed;

        public BreakBlock(TileAdvQuarry quarry) {
            assert quarry.getArea() != null;
            this.iterator = this.createTargetIterator(quarry.getArea(), quarry.workConfig.chunkByChunk());
        }

        BreakBlock(TileAdvQuarry quarry, int x, int z, boolean searchEnergyConsumed) {
            assert quarry.getArea() != null;
            this.iterator = this.createTargetIterator(quarry.getArea(), quarry.workConfig.chunkByChunk());
            this.iterator.setCurrent(new TargetIterator.XZPair(x, z));
            this.searchEnergyConsumed = searchEnergyConsumed;
        }

        @Override
        CompoundTag writeDetail(CompoundTag tag) {
            var xzPair = iterator.peek();
            tag.putInt("currentX", xzPair.x());
            tag.putInt("currentZ", xzPair.z());
            tag.putBoolean("searchEnergyConsumed", searchEnergyConsumed);
            return tag;
        }

        @Override
        String key() {
            return "BreakBlock";
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
            BreakResult result = null;
            while (result == null || result == BreakResult.SKIPPED) {
                var target = iterator.peek();
                if (!searchEnergyConsumed) {
                    var energy = PowerManager.getAdvSearchEnergy(pos.getY() - quarry.digMinY, quarry);
                    searchEnergyConsumed = quarry.useEnergy(energy, PowerTile.Reason.ADV_SEARCH, false);
                }
                if (!searchEnergyConsumed) break; // Not enough energy.
                result = quarry.breakBlocks(target.x(), target.z());
                if (result.isSuccess()) {
                    iterator.next();
                    searchEnergyConsumed = false;
                    if (!iterator.hasNext()) {
                        // Go to the next work.
                        quarry.setAction(new CheckFluid(quarry));
                        break;
                    }
                }
            }
        }
    }

    private static final class BreakBlockSerializer extends Serializer {

        @Override
        String key() {
            return "BreakBlock";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            int x = tag.getInt("currentX");
            int z = tag.getInt("currentZ");
            boolean searchEnergyConsumed = tag.getBoolean("searchEnergyConsumed");
            return new BreakBlock(quarry, x, z, searchEnergyConsumed);
        }
    }

    public static final class CheckFluid extends AdvQuarryAction {
        private final TargetIterator iterator;

        public CheckFluid(TileAdvQuarry quarry) {
            assert quarry.getArea() != null;
            this.iterator = this.createTargetIterator(quarry.getArea(), quarry.workConfig.chunkByChunk());
        }

        CheckFluid(TileAdvQuarry quarry, int x, int z) {
            assert quarry.getArea() != null;
            this.iterator = this.createTargetIterator(quarry.getArea(), quarry.workConfig.chunkByChunk());
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
            return "CheckFluid";
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
            int count = 0;
            var targetWorld = quarry.getTargetWorld();
            while (count < 32 && iterator.hasNext()) {
                boolean flagRemoved = false;
                var target = iterator.peek();
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(target.x(), 0, target.z());
                for (int y = quarry.digMinY + 1; y < pos.getY() - 1; y++) {
                    mutableBlockPos.setY(y);
                    var blockState = targetWorld.getBlockState(mutableBlockPos);
                    var blockCondition = blockState.is(Holder.BLOCK_DUMMY) || blockState.is(Blocks.STONE) || blockState.is(Blocks.COBBLESTONE);
                    var blockIsReplaced = quarry.getReplacementState() == blockState;
                    if (blockCondition && !blockIsReplaced) {
                        targetWorld.setBlock(mutableBlockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        flagRemoved = true;
                    }
                }
                iterator.next();
                if (flagRemoved) count += 1;
            }
            if (!iterator.hasNext()) {
                if (quarry.hasFillerModule())
                    quarry.setAction(new FillerWork(quarry));
                else
                    quarry.setAction(Finished.FINISHED);
            }
        }
    }

    private static final class CheckFluidSerializer extends Serializer {
        @Override
        String key() {
            return "CheckFluid";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            int x = tag.getInt("currentX");
            int z = tag.getInt("currentZ");
            return new CheckFluid(quarry, x, z);
        }
    }

    public static final class Finished extends AdvQuarryAction {
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

    private static final class FinishedSerializer extends Serializer {

        @Override
        String key() {
            return "Finished";
        }

        @Override
        AdvQuarryAction fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            return Finished.FINISHED;
        }
    }

    public static final class FillerWork extends AdvQuarryAction {
        private final TargetIterator iterator;

        public FillerWork(TileAdvQuarry quarry) {
            assert quarry.getArea() != null;
            this.iterator = this.createTargetIterator(quarry.getArea(), quarry.workConfig.chunkByChunk());
        }

        FillerWork(TileAdvQuarry quarry, int x, int z) {
            assert quarry.getArea() != null;
            this.iterator = this.createTargetIterator(quarry.getArea(), quarry.workConfig.chunkByChunk());
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
            return "FillerWork";
        }

        @Override
        public void tick(Level level, BlockPos pos, BlockState state, TileAdvQuarry quarry) {
            var target = this.iterator.peek();
            var mutablePos = new BlockPos.MutableBlockPos(target.x(), 0, target.z());
            for (int y = quarry.digMinY + 1; y < pos.getY(); y++) {
                mutablePos.setY(y);
                var targetState = quarry.getTargetWorld().getBlockState(mutablePos);
                if (targetState.canBeReplaced()) {
                    var energy = PowerManager.getFillerEnergy(quarry) * 10;
                    if (quarry.useEnergy(energy, PowerTile.Reason.FILLER, false)) {
                        var toReplace = FillerAction.getToReplace(quarry.getTargetWorld().dimension(), mutablePos);
                        quarry.getTargetWorld().setBlockAndUpdate(mutablePos, toReplace);
                    } else {
                        return; // Insufficient energy.
                    }
                }
            }
            this.iterator.next();
            if (!iterator.hasNext()) {
                quarry.setAction(Finished.FINISHED);
            }
        }
    }

    private static final class FillerWorkSerializer extends Serializer {

        @Override
        String key() {
            return "FillerWork";
        }

        @Override
        FillerWork fromTag(CompoundTag tag, TileAdvQuarry quarry) {
            int x = tag.getInt("currentX");
            int z = tag.getInt("currentZ");
            return new FillerWork(quarry, x, z);
        }
    }
}
