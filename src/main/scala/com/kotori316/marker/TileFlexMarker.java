package com.kotori316.marker;

import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;

import com.kotori316.marker.render.Box;

public class TileFlexMarker extends TileEntity implements IAreaConfigurable {

    public static final String BC_CORE_ID = "buildcraftlib"; // BuildCraftAPI|core - buildcraftapi_core
    public static final String BC_TILE_ID = "buildcraftlib"; // BuildCraftAPI|tiles - buildcraftapi_tiles

    private BlockPos min = BlockPos.ZERO;
    private BlockPos max = BlockPos.ZERO;
    @Nullable
    public Box[] boxes;
    @Nullable
    public Box directionBox;
    public Direction direction;
    private boolean bcLoaded = ModList.get().isLoaded(BC_TILE_ID); // ModAPIManager.INSTANCE.hasAPI("buildcraftapi_tiles");

    public TileFlexMarker() {
        super(Marker.Entries.TYPE);
    }

    public void init(Direction facing) {
        this.direction = facing;
        this.min = getPos();
        this.max = getPos();
        move(Movable.LEFT, 5);
        move(Movable.RIGHT, 5);
        move(Movable.FORWARD, 10);
        setRender();
    }

    @SuppressWarnings("Duplicates")
    public void move(Movable movable, int amount) {
        Direction facing = movable.getActualFacing(direction);
        BlockPos offset = getPos();
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public Runnable setMinMax(BlockPos min, BlockPos max) {
        return () -> {
            this.min = min;
            this.max = max;
            setRender();
        };
    }

    private void setRender() {
        assert world != null; // called in real world.
        if (!world.isRemote)
            return;
        boxes = IAreaConfigurable.getRenderBox(this.min, this.max);
        AxisAlignedBB bb;
        if (direction.getAxis() == Direction.Axis.X) {
            bb = new AxisAlignedBB(getPos().getX() - c + a, getPos().getY() + a, getPos().getZ() + a,
                getPos().getX() + c + a, getPos().getY() + a, getPos().getZ() + a);
        } else {
            bb = new AxisAlignedBB(getPos().getX() + a, getPos().getY() + a, getPos().getZ() - c + a,
                getPos().getX() + a, getPos().getY() + a, getPos().getZ() + c + a);
        }
        directionBox = Box.apply(bb.offset(Vector3d.func_237491_b_(direction.getDirectionVec()).scale(a)), 1d / 8d, 1d / 8d, 1d / 8d, true, true);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLong("min", min.toLong());
        compound.putLong("max", max.toLong());
        compound.putString("direction", Optional.ofNullable(direction).map(Direction::toString).orElse(""));
        return super.write(compound);
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT compound) {
        super.func_230337_a_(state, compound);
        min = BlockPos.fromLong(compound.getLong("min"));
        max = BlockPos.fromLong(compound.getLong("max"));
        direction = Direction.byName(compound.getString("direction"));
        if (hasWorld()) {
            setRender();
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return super.serializeNBT();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 128 * 128 * 2;
    }

    public BlockPos min() {
        return min == BlockPos.ZERO ? getPos() : min;
    }

    public BlockPos max() {
        return max == BlockPos.ZERO ? getPos() : max;
    }

    /*@Override
    @net.minecraftforge.fml.common.Optional.Method(modid = TileFlexMarker.BC_CORE_ID)
    public void removeFromWorld() {
        if (!getWorld().isRemote) {
            getWorld().destroyBlock(getPos(), true);
        }
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = TileFlexMarker.BC_CORE_ID)
    public boolean isValidFromLocation(BlockPos pos) {
        return false;
    }*/

    /*@Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        String[] strings = {
            "Pos: x=" + pos.getX() + " y=" + pos.getY() + " z=" + pos.getZ(),
            "Facing: " + (direction == null ? "Unknown" : direction.toString()),
            "Min: x=" + min.getX() + " y=" + min.getY() + " z=" + min.getZ(),
            "Max: x=" + max.getX() + " y=" + max.getY() + " z=" + max.getZ(),
        };
        left.addAll(Arrays.asList(strings));
    }*/

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        /*if (bcLoaded) {
            if (cap == TilesAPI.CAP_TILE_AREA_PROVIDER) {
                return TilesAPI.CAP_TILE_AREA_PROVIDER.cast(this);
            }
        }*/
        return super.getCapability(cap, side);
    }

    public enum Movable {
        UP(facing -> Direction.UP),
        LEFT(Direction::rotateYCCW),
        FORWARD(UnaryOperator.identity()),
        RIGHT(Direction::rotateY),
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
        switch (axis) {
            case X:
                return to.getX() - from.getX();
            case Y:
                return to.getY() - from.getY();
            case Z:
                return to.getZ() - from.getZ();
        }
        throw new IllegalStateException(String.format("Other axis? Axis=%s, from=%s, to=%s", axis, from, to));
    }

    public static BlockPos getLimited(BlockPos to, BlockPos from, Direction facing, int limit) {
        switch (facing.getAxis()) {
            case X:
                return new BlockPos(from.getX(), to.getY(), to.getZ()).offset(facing, limit);
            case Y:
                return new BlockPos(to.getX(), from.getY(), to.getZ()).offset(facing, limit);
            case Z:
                return new BlockPos(to.getX(), to.getY(), from.getZ()).offset(facing, limit);
        }
        throw new IllegalStateException(String.format("Other axis? Facing=%s, from=%s, to=%s, limit=%d", facing, from, to, limit));
    }
}
