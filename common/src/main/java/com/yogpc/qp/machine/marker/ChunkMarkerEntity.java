package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChunkMarkerEntity extends QpEntity implements QuarryMarker, ClientSync {
    @NotNull
    private Direction.AxisDirection xDirection = Direction.AxisDirection.POSITIVE;
    @NotNull
    private Direction.AxisDirection zDirection = Direction.AxisDirection.POSITIVE;
    int size = 16;
    int minY;
    int maxY;

    public ChunkMarkerEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fromClientTag(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        toClientTag(tag, registries);
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        xDirection = Direction.AxisDirection.valueOf(tag.getString("xDirection"));
        zDirection = Direction.AxisDirection.valueOf(tag.getString("zDirection"));
        size = tag.getInt("size");
        minY = tag.getInt("minY");
        maxY = tag.getInt("maxY");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("xDirection", xDirection.name());
        tag.putString("zDirection", zDirection.name());
        tag.putInt("size", size);
        tag.putInt("minY", minY);
        tag.putInt("maxY", maxY);
        return tag;
    }

    @Override
    public Optional<QuarryMarker.Link> getLink() {
        return Optional.of(createLink());
    }

    Link createLink() {
        var stack = new ItemStack(getBlockState().getBlock());
        return new Link(getBlockPos(), xDirection.getStep() * (size + 1), zDirection.getStep() * (size + 1), minY, maxY, stack);
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

    public void changeSize(int size, int minY, int maxY) {
        this.size = size;
        this.minY = minY;
        this.maxY = maxY;
    }

    record Link(BlockPos basePos, int xOffset, int zOffset, int minY, int maxY,
                ItemStack drop) implements QuarryMarker.Link {

        @Override
        public Area area() {
            var x2 = basePos.getX() + xOffset;
            var z2 = basePos.getZ() + zOffset;
            return new Area(
                Math.min(basePos.getX(), x2),
                minY,
                Math.min(basePos.getZ(), z2),
                Math.max(basePos.getX(), x2),
                maxY,
                Math.max(basePos.getZ(), z2),
                Direction.UP
            );
        }

        @Override
        public void remove(Level level) {
            level.removeBlock(basePos, false);
        }

        @Override
        public List<ItemStack> drops() {
            return List.of(drop);
        }
    }

    public AABB getRenderAabb() {
        var link = createLink();
        var area = link.area();
        return new AABB(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ());
    }
}
