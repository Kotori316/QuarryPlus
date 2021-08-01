package com.yogpc.qp.machines.marker;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.render.Box;
import com.yogpc.qp.render.RenderMarker;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Tile16Marker extends BlockEntity implements QuarryMarker, CheckerLog, BlockEntityClientSerializable {
    private BlockPos min = BlockPos.ORIGIN;
    private BlockPos max = BlockPos.ORIGIN;
    @Nullable
    public Box[] boxes;
    //private boolean bcLoaded = ModList.get().isLoaded(BC_TILE_ID);
    private Direction.AxisDirection xDirection = Direction.AxisDirection.NEGATIVE, zDirection = Direction.AxisDirection.POSITIVE;
    private int size = 16;

    public Tile16Marker(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.MARKER_16_TYPE, pos, state);
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
        BlockPos edge1 = getPos().add(xDirection.offset() * (size + 1), 0, zDirection.offset() * (size + 1));
        BlockPos edge2 = getPos();
        min = new BlockPos(Math.min(edge1.getX(), edge2.getX()), yMin, Math.min(edge1.getZ(), edge2.getZ()));
        max = new BlockPos(Math.max(edge1.getX(), edge2.getX()), yMax, Math.max(edge1.getZ(), edge2.getZ()));
        if (world != null && world.isClient) setRender();
    }

    private void setRender() {
        assert world != null;
        boxes = RenderMarker.getRenderBox(new Area(min, max, Direction.from(Direction.Axis.X, xDirection)));
    }

    @Environment(EnvType.CLIENT)
    public int getSize() {
        return size;
    }

    // TileEntity overrides
    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        min = BlockPos.fromLong(compound.getLong("min"));
        max = BlockPos.fromLong(compound.getLong("max"));
        xDirection = compound.getBoolean("x") ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        zDirection = compound.getBoolean("z") ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        size = compound.getInt("size");
        if (world != null && world.isClient) {
            setRender();
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        compound.putLong("min", min.asLong());
        compound.putLong("max", max.asLong());
        compound.putBoolean("x", xDirection == Direction.AxisDirection.POSITIVE);
        compound.putBoolean("z", zDirection == Direction.AxisDirection.POSITIVE);
        compound.putInt("size", size);
        return super.writeNbt(compound);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    // Interface implementations

    public BlockPos min() {
        return min == BlockPos.ZERO ? getPos() : min;
    }

    public BlockPos max() {
        return max == BlockPos.ZERO ? getPos() : max;
    }

    @Override
    public List<? extends Text> getDebugLogs() {
        return List.of(
            "Size: " + size,
            "Min: " + min(),
            "Max: " + max()
        ).stream().map(LiteralText::new).toList();
    }

    @Override
    public Optional<Area> getArea() {
        return Optional.of(new Area(min, max, Direction.from(Direction.Axis.X, xDirection)));
    }

    @Override
    public List<ItemStack> removeAndGetItems() {
        assert world != null;
        world.removeBlock(pos, false);
        return List.of(new ItemStack(QuarryPlus.ModObjects.BLOCK_16_MARKER));
    }
}
