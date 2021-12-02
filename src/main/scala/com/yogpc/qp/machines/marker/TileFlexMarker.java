package com.yogpc.qp.machines.marker;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.render.Box;
import com.yogpc.qp.render.RenderMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TileFlexMarker extends BlockEntity implements QuarryMarker, CheckerLog {

    private BlockPos min;
    private BlockPos max;
    @Nullable
    public Box[] boxes;
    @Nullable
    public Box directionBox;
    public Direction direction;

    public TileFlexMarker(BlockPos pos, BlockState state) {
        super(Holder.FLEX_MARKER_TYPE, pos, state);
        this.min = pos;
        this.max = pos;
    }

    public void init(Direction facing) {
        this.direction = facing;
        this.min = getBlockPos();
        this.max = getBlockPos();
        move(Movable.LEFT, 5);
        move(Movable.RIGHT, 5);
        move(Movable.FORWARD, 10);
        if (level != null && level.isClientSide) setRender();
    }

    @SuppressWarnings("Duplicates")
    public void move(Movable movable, int amount) {
        Direction facing = movable.getActualFacing(direction);
        BlockPos offset = getBlockPos();
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            max = max.relative(facing, amount);
            int d = getDistance(max, offset, facing.getAxis());
            if (d > 64) {
                max = getLimited(max, offset, facing, 64);
            } else if (d < 1) {
                max = getLimited(max, offset, facing, 1);
            }
        } else {
            min = min.relative(facing, amount);
            int d = getDistance(offset, min, facing.getAxis());
            if (d > 64) {
                min = getLimited(min, offset, facing, 64);
            } else if (d < 1) {
                min = getLimited(min, offset, facing, 1);
            }
            if (facing == Direction.DOWN && min.getY() < 0) {
                min = new BlockPos(min.getX(), 0, min.getZ());
            }
        }
    }

    private void setRender() {
        if (level == null)
            return;
        var area = new Area(min, max, direction);
        boxes = RenderMarker.getRenderBox(area);
        AABB bb;
        final double a = 0.5d, c = 6d / 16d;
        if (direction == null) {
            // dummy
            bb = new AABB(getBlockPos().getX() + a, getBlockPos().getY() + a, getBlockPos().getZ() + a,
                getBlockPos().getX() + a, getBlockPos().getY() + a, getBlockPos().getZ() + a);
        } else if (direction.getAxis() == Direction.Axis.X) {
            bb = new AABB(getBlockPos().getX() - c + a, getBlockPos().getY() + a, getBlockPos().getZ() + a,
                getBlockPos().getX() + c + a, getBlockPos().getY() + a, getBlockPos().getZ() + a);
        } else {
            bb = new AABB(getBlockPos().getX() + a, getBlockPos().getY() + a, getBlockPos().getZ() - c + a,
                getBlockPos().getX() + a, getBlockPos().getY() + a, getBlockPos().getZ() + c + a);
        }
        directionBox = Box.apply(bb.move(Vec3.atLowerCornerOf(direction.getNormal()).scale(a)), 1d / 8d, 1d / 8d, 1d / 8d, true, true);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        saveAdditional(compound);
        return super.save(compound);
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.putLong("min", min.asLong());
        compound.putLong("max", max.asLong());
        compound.putString("direction", Optional.ofNullable(direction).map(Direction::toString).orElse(""));
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        min = BlockPos.of(compound.getLong("min"));
        max = BlockPos.of(compound.getLong("max"));
        direction = Direction.byName(compound.getString("direction"));
        if (level != null && level.isClientSide) {
            setRender();
        }
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
            "Direction: " + direction,
            "Min: " + min,
            "Max: " + max
        ).map(TextComponent::new).toList();
    }

    @Override
    public Optional<Area> getArea() {
        return Optional.of(new Area(min, max, direction));
    }

    @Override
    public List<ItemStack> removeAndGetItems() {
        assert level != null;
        level.removeBlock(getBlockPos(), false);
        return List.of(new ItemStack(Holder.BLOCK_FLEX_MARKER));
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

        private static final Movable[] v;

        public static Movable valueOf(int i) {
            return v[i];
        }

        static {
            v = values();
        }
    }

    public static int getDistance(BlockPos to, BlockPos from, Direction.Axis axis) {
        return switch (axis) {
            case X -> to.getX() - from.getX();
            case Y -> to.getY() - from.getY();
            case Z -> to.getZ() - from.getZ();
        };
    }

    public static BlockPos getLimited(BlockPos to, BlockPos from, Direction facing, int limit) {
        return switch (facing.getAxis()) {
            case X -> new BlockPos(from.getX(), to.getY(), to.getZ()).relative(facing, limit);
            case Y -> new BlockPos(to.getX(), from.getY(), to.getZ()).relative(facing, limit);
            case Z -> new BlockPos(to.getX(), to.getY(), from.getZ()).relative(facing, limit);
        };
    }
}
