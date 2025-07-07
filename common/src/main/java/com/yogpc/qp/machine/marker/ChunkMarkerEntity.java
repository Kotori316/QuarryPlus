package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fromClientTag(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        toClientTag(output);
    }

    @Override
    public void fromClientTag(ValueInput input) {
        xDirection = input.getString("xDirection").map(Direction.AxisDirection::valueOf).orElse(xDirection);
        zDirection = input.getString("zDirection").map(Direction.AxisDirection::valueOf).orElse(zDirection);
        size = input.getIntOr("size", size);
        minY = input.getIntOr("minY", minY);
        maxY = input.getIntOr("maxY", maxY);
    }

    @Override
    public ValueOutput toClientTag(ValueOutput output) {
        output.putString("xDirection", xDirection.name());
        output.putString("zDirection", zDirection.name());
        output.putInt("size", size);
        output.putInt("minY", minY);
        output.putInt("maxY", maxY);
        return output;
    }

    @Override
    public Stream<MutableComponent> checkerLogs() {
        return Stream.concat(super.checkerLogs(), Stream.of(
            detail(ChatFormatting.GREEN, "xDirection", String.valueOf(xDirection)),
            detail(ChatFormatting.GREEN, "zDirection", String.valueOf(zDirection)),
            detail(ChatFormatting.GREEN, "size", String.valueOf(size)),
            detail(ChatFormatting.GREEN, "minY", String.valueOf(minY)),
            detail(ChatFormatting.GREEN, "maxY", String.valueOf(maxY))
        ));
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
