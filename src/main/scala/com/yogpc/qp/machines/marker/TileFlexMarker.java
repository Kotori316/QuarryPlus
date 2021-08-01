package com.yogpc.qp.machines.marker;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.render.Box;
import com.yogpc.qp.render.RenderMarker;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TileFlexMarker extends BlockEntity implements QuarryMarker, CheckerLog, BlockEntityClientSerializable {

    private BlockPos min;
    private BlockPos max;
    @Nullable
    public Box[] boxes;
    @Nullable
    public Box directionBox;
    public Direction direction;

    public TileFlexMarker(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.FLEX_MARKER_TYPE, pos, state);
        this.min = pos;
        this.max = pos;
    }

    public void init(Direction facing) {
        this.direction = facing;
        this.min = getPos();
        this.max = getPos();
        move(Movable.LEFT, 5);
        move(Movable.RIGHT, 5);
        move(Movable.FORWARD, 10);
        if (world != null && world.isClient) setRender();
    }

    @SuppressWarnings("Duplicates")
    public void move(Movable movable, int amount) {
        Direction facing = movable.getActualFacing(direction);
        BlockPos offset = getPos();
        if (facing.getDirection() == Direction.AxisDirection.POSITIVE) {
            max = max.offset(facing, amount);
            int d = getDistance(max, offset, facing.getAxis());
            if (d > 64) {
                max = getLimited(max, offset, facing, 64);
            } else if (d < 1) {
                max = getLimited(max, offset, facing, 1);
            }
        } else {
            min = min.offset(facing, amount);
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
        if (world == null)
            return;
        var area = new Area(min, max, direction);
        boxes = RenderMarker.getRenderBox(area);
        net.minecraft.util.math.Box bb;
        final double a = 0.5d, c = 6d / 16d;
        if (direction == null) {
            // dummy
            bb = new net.minecraft.util.math.Box(getPos().getX() + a, getPos().getY() + a, getPos().getZ() + a,
                getPos().getX() + a, getPos().getY() + a, getPos().getZ() + a);
        } else if (direction.getAxis() == Direction.Axis.X) {
            bb = new net.minecraft.util.math.Box(getPos().getX() - c + a, getPos().getY() + a, getPos().getZ() + a,
                getPos().getX() + c + a, getPos().getY() + a, getPos().getZ() + a);
        } else {
            bb = new net.minecraft.util.math.Box(getPos().getX() + a, getPos().getY() + a, getPos().getZ() - c + a,
                getPos().getX() + a, getPos().getY() + a, getPos().getZ() + c + a);
        }
        directionBox = Box.apply(bb.offset(Vec3d.of(direction.getVector()).multiply(a)), 1d / 8d, 1d / 8d, 1d / 8d, true, true);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        compound.putLong("min", min.asLong());
        compound.putLong("max", max.asLong());
        compound.putString("direction", Optional.ofNullable(direction).map(Direction::toString).orElse(""));
        return super.writeNbt(compound);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        min = BlockPos.fromLong(compound.getLong("min"));
        max = BlockPos.fromLong(compound.getLong("max"));
        direction = Direction.byName(compound.getString("direction"));
        if (world != null && world.isClient) {
            setRender();
        }
    }

    @Override
    public List<? extends Text> getDebugLogs() {
        return List.of(
            "Direction: " + direction,
            "Min: " + min,
            "Max: " + max
        ).stream().map(LiteralText::new).toList();
    }

    @Override
    public Optional<Area> getArea() {
        return Optional.of(new Area(min, max, direction));
    }

    @Override
    public List<ItemStack> removeAndGetItems() {
        assert world != null;
        world.removeBlock(pos, false);
        return List.of(new ItemStack(QuarryPlus.ModObjects.BLOCK_FLEX_MARKER));
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    public enum Movable {
        UP(facing -> Direction.UP),
        LEFT(Direction::rotateYCounterclockwise),
        FORWARD(UnaryOperator.identity()),
        RIGHT(Direction::rotateYClockwise),
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
            case X -> new BlockPos(from.getX(), to.getY(), to.getZ()).offset(facing, limit);
            case Y -> new BlockPos(to.getX(), from.getY(), to.getZ()).offset(facing, limit);
            case Z -> new BlockPos(to.getX(), to.getY(), from.getZ()).offset(facing, limit);
        };
    }
}
