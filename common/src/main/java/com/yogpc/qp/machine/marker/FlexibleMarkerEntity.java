package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class FlexibleMarkerEntity extends QpEntity implements QuarryMarker, ClientSync {
    @NotNull
    BlockPos min;
    @NotNull
    BlockPos max;
    Direction direction = Direction.NORTH;

    public FlexibleMarkerEntity(@NotNull BlockPos pos, BlockState blockState) {
        super(pos, blockState);
        min = pos;
        max = pos;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        toClientTag(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fromClientTag(tag, registries);
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        min = BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("min")).getOrThrow();
        max = BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("max")).getOrThrow();
        direction = Direction.CODEC.parse(NbtOps.INSTANCE, tag.get("direction")).getOrThrow();
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("min", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, min).getOrThrow());
        tag.put("max", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, max).getOrThrow());
        tag.put("direction", Direction.CODEC.encodeStart(NbtOps.INSTANCE, direction).getOrThrow());
        return tag;
    }

    public void init(Direction facing) {
        this.direction = facing;
        this.min = getBlockPos();
        this.max = getBlockPos();
        move(Movable.LEFT, 5);
        move(Movable.RIGHT, 5);
        move(Movable.FORWARD, 10);
        syncToClient();
    }

    @SuppressWarnings("Duplicates")
    void move(Movable movable, int amount) {
        assert level != null;
        Direction facing = movable.getActualFacing(direction);
        BlockPos offset = getBlockPos();
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            max = max.relative(facing, amount);
            int d = getDistance(max, offset, facing.getAxis());
            if (d > getMaxRange()) {
                max = getLimited(max, offset, facing, getMaxRange());
            } else if (d < 0) {
                max = getLimited(max, offset, facing, 0);
            }
            if (facing == Direction.UP && max.getY() >= level.getMaxBuildHeight()) {
                max = new BlockPos(max.getX(), level.getMaxBuildHeight() - 1, max.getZ());
            }
        } else {
            min = min.relative(facing, amount);
            int d = getDistance(offset, min, facing.getAxis());
            if (d > getMaxRange()) {
                min = getLimited(min, offset, facing, getMaxRange());
            } else if (d < 0) {
                min = getLimited(min, offset, facing, 0);
            }
            if (facing == Direction.DOWN && min.getY() < level.getMinBuildHeight()) {
                min = new BlockPos(min.getX(), level.getMinBuildHeight(), min.getZ());
            }
        }
    }

    private int getMaxRange() {
        return NormalMarkerEntity.MAX_SEARCH;
    }

    public enum Movable {
        UP(facing -> Direction.UP),
        LEFT(Direction::getCounterClockWise),
        FORWARD(UnaryOperator.identity()),
        RIGHT(Direction::getClockWise),
        DOWN(facing -> Direction.DOWN);

        private final UnaryOperator<Direction> operator;
        public final String transName;

        Movable(UnaryOperator<Direction> operator) {
            this.operator = operator;
            this.transName = "gui." + name().toLowerCase(Locale.US);
        }

        public Direction getActualFacing(Direction facing) {
            return this.operator.apply(facing);
        }

        public static Movable valueOf(int i) {
            return values()[i];
        }

        public int distanceFromOrigin(BlockPos origin, BlockPos areaMin, BlockPos areaMax, Direction facing) {
            Direction actualFacing = getActualFacing(facing);
            BlockPos relative = actualFacing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? areaMax : areaMin;
            return Math.abs(getDistance(origin, relative, actualFacing.getAxis()));
        }
    }

    public static int getDistance(BlockPos to, BlockPos from, Direction.Axis axis) {
        return to.get(axis) - from.get(axis);
    }

    public static BlockPos getLimited(BlockPos to, BlockPos from, Direction facing, int limit) {
        return switch (facing.getAxis()) {
            case X -> new BlockPos(from.getX(), to.getY(), to.getZ()).relative(facing, limit);
            case Y -> new BlockPos(to.getX(), from.getY(), to.getZ()).relative(facing, limit);
            case Z -> new BlockPos(to.getX(), to.getY(), from.getZ()).relative(facing, limit);
        };
    }

    @Override
    public Optional<QuarryMarker.Link> getLink() {
        return Optional.of(createLink());
    }

    FlexibleMarkerLink createLink() {
        var stack = new ItemStack(getBlockState().getBlock());
        return new FlexibleMarkerLink(getBlockPos(), min, max, direction, stack);
    }

    public Direction getDirection() {
        return direction;
    }

    record FlexibleMarkerLink(BlockPos markerPos, BlockPos min, BlockPos max,
                              Direction direction, ItemStack stack) implements QuarryMarker.Link {
        @Override
        public Area area() {
            return new Area(min, max, direction);
        }

        @Override
        public void remove(Level level) {
            level.removeBlock(markerPos, false);
        }

        @Override
        public List<ItemStack> drops() {
            return List.of(stack);
        }
    }

    public AABB getRenderAabb() {
        var link = createLink();
        var area = link.area();
        return new AABB(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ());
    }
}
