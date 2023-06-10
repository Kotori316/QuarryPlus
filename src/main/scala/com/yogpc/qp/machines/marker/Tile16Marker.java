package com.yogpc.qp.machines.marker;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Tile16Marker extends BlockEntity implements QuarryMarker, CheckerLog {
    private BlockPos min = BlockPos.ZERO;
    private BlockPos max = BlockPos.ZERO;
    @Nullable
    public Box[] boxes;
    //private boolean bcLoaded = ModList.enabled().isLoaded(BC_TILE_ID);
    private Direction.AxisDirection xDirection = Direction.AxisDirection.NEGATIVE, zDirection = Direction.AxisDirection.POSITIVE;
    private int size = 16;

    public Tile16Marker(BlockPos pos, BlockState state) {
        super(Holder.MARKER_16_TYPE, pos, state);
    }

    public void init(Direction.AxisDirection xDirection, Direction.AxisDirection zDirection) {
        this.xDirection = Objects.requireNonNull(xDirection);
        this.zDirection = Objects.requireNonNull(zDirection);
        changeSize(this.size);
    }

    public void changeSize(int size) {
        int y = getBlockPos().getY();
        changeSize(size, y, y);
    }

    public void changeSize(int size, int yMax, int yMin) {
        this.size = size;
        BlockPos edge1 = getBlockPos().offset(xDirection.getStep() * (size + 1), 0, zDirection.getStep() * (size + 1));
        BlockPos edge2 = getBlockPos();
        min = new BlockPos(Math.min(edge1.getX(), edge2.getX()), yMin, Math.min(edge1.getZ(), edge2.getZ()));
        max = new BlockPos(Math.max(edge1.getX(), edge2.getX()), yMax, Math.max(edge1.getZ(), edge2.getZ()));
        if (level != null && level.isClientSide) setRender();
    }

    private void setRender() {
        boxes = RenderMarker.getRenderBox(new Area(min, max, Direction.fromAxisAndDirection(Direction.Axis.X, xDirection)));
    }

    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return size;
    }

    // BlockEntity overrides
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        min = BlockPos.of(compound.getLong("min"));
        max = BlockPos.of(compound.getLong("max"));
        xDirection = compound.getBoolean("x") ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        zDirection = compound.getBoolean("z") ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        size = compound.getInt("size");
        if (level != null && level.isClientSide) {
            setRender();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.putLong("min", min.asLong());
        compound.putLong("max", max.asLong());
        compound.putBoolean("x", xDirection == Direction.AxisDirection.POSITIVE);
        compound.putBoolean("z", zDirection == Direction.AxisDirection.POSITIVE);
        compound.putInt("size", size);
        super.saveAdditional(compound);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(min, max);
    }

    // Interface implementations

    public BlockPos min() {
        return min == BlockPos.ZERO ? getBlockPos() : min;
    }

    public BlockPos max() {
        return max == BlockPos.ZERO ? getBlockPos() : max;
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return Stream.of(
                "Size: " + size,
                "Min: " + min(),
                "Max: " + max()
        ).map(Component::literal).toList();
    }

    @Override
    public Optional<Area> getArea() {
        return Optional.of(new Area(min, max, Direction.fromAxisAndDirection(Direction.Axis.X, xDirection)));
    }

    @Override
    public List<ItemStack> removeAndGetItems() {
        assert level != null;
        level.removeBlock(getBlockPos(), false);
        return List.of(new ItemStack(getBlockState().getBlock()));
    }
}
