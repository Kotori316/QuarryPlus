package com.kotori316.marker;

import java.util.Objects;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import com.kotori316.marker.render.Box;

import static com.kotori316.marker.TileFlexMarker.BC_TILE_ID;

public class Tile16Marker extends TileEntity implements /*ITileAreaProvider, IDebuggable, IMarker,*/ IAreaConfigurable {
    private BlockPos min = BlockPos.ZERO;
    private BlockPos max = BlockPos.ZERO;
    @Nullable
    public Box[] boxes;
    private boolean bcLoaded = ModList.get().isLoaded(BC_TILE_ID);
    private Direction.AxisDirection xDirection = Direction.AxisDirection.NEGATIVE, zDirection = Direction.AxisDirection.POSITIVE;
    private int size = 16;

    public Tile16Marker() {
        super(Marker.Entries.TYPE16);
    }

    public void init(Direction.AxisDirection xDirection, Direction.AxisDirection zDirection) {
        this.xDirection = Objects.requireNonNull(xDirection);
        this.zDirection = Objects.requireNonNull(zDirection);
        changeSize(this.size);
    }

    public void changeSize(int size) {
        int y = getPos().getY();
        changeSize(size, y, y);
    }

    public void changeSize(int size, int yMax, int yMin) {
        this.size = size;
        BlockPos edge1 = getPos().add(xDirection.getOffset() * (size + 1), 0, zDirection.getOffset() * (size + 1));
        BlockPos edge2 = getPos();
        min = new BlockPos(Math.min(edge1.getX(), edge2.getX()), yMin, Math.min(edge1.getZ(), edge2.getZ()));
        max = new BlockPos(Math.max(edge1.getX(), edge2.getX()), yMax, Math.max(edge1.getZ(), edge2.getZ()));
        setRender();
    }

    private void setRender() {
        assert world != null;
        if (!world.isRemote)
            return;
        boxes = IAreaConfigurable.getRenderBox(this.min, this.max);
    }

    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return size;
    }

    // TileEntity overrides
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

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT compound) {
        super.func_230337_a_(state, compound);
        min = BlockPos.fromLong(compound.getLong("min"));
        max = BlockPos.fromLong(compound.getLong("max"));
        xDirection = compound.getBoolean("x") ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        zDirection = compound.getBoolean("z") ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        size = compound.getInt("size");
        if (hasWorld()) {
            setRender();
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLong("min", min.toLong());
        compound.putLong("max", max.toLong());
        compound.putBoolean("x", xDirection == Direction.AxisDirection.POSITIVE);
        compound.putBoolean("z", zDirection == Direction.AxisDirection.POSITIVE);
        compound.putInt("size", size);
        return super.write(compound);
    }

    // Interface implementations

    public BlockPos min() {
        return min == BlockPos.ZERO ? getPos() : min;
    }

    public BlockPos max() {
        return max == BlockPos.ZERO ? getPos() : max;
    }

    /*
        @Override
        @net.minecraftforge.fml.common.Optional.Method(modid = TileFlexMarker.BC_CORE_ID)
        public boolean isValidFromLocation(BlockPos pos) {
            return false;
        }

        @Override
        @net.minecraftforge.fml.common.Optional.Method(modid = TileFlexMarker.BC_CORE_ID)
        public void removeFromWorld() {
            if (!getWorld().isRemote) {
                getWorld().destroyBlock(getPos(), true);
            }
        }

        @Override
        public void getDebugInfo(List<String> left, List<String> right, Direction side) {
            String[] strings = {
                "Pos: x=" + pos.getX() + " y=" + pos.getY() + " z=" + pos.getZ(),
                "Facing: " + (xDirection == null ? "Unknown" : Direction.getFacingFromAxis(xDirection, Direction.Axis.X)) +
                    ", " + (zDirection == null ? "Unknown" : Direction.getFacingFromAxis(zDirection, Direction.Axis.Z)),
                "Min: x=" + min.getX() + " y=" + min.getY() + " z=" + min.getZ(),
                "Max: x=" + max.getX() + " y=" + max.getY() + " z=" + max.getZ(),
            };
            left.addAll(Arrays.asList(strings));
        }
    */
    @Override
    @OnlyIn(Dist.CLIENT)
    public Runnable setMinMax(BlockPos min, BlockPos max) {
        return () -> {
            this.max = max;
            this.min = min;
            setRender();
            size = (max.getX() - min.getX() - 1);
        };
    }
}
