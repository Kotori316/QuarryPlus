package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public final class ChunkMarkerBlock extends ExMarkerBlock {
    public static final String NAME = "chunk_marker";

    public ChunkMarkerBlock() {
        super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel(value -> 7).noCollission(), NAME);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        var angle = inRange(placer != null ? placer.getYHeadRot() : 0, 0, 360);
        Direction.AxisDirection z = angle < 90 || angle >= 270 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        Direction.AxisDirection x = angle > 180 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        this.<ChunkMarkerEntity>getBlockEntityType().map(t -> t.getBlockEntity(level, pos)).ifPresent(t -> t.init(x, z));
    }

    @Override
    protected GeneralScreenHandler<?> getScreenHandler(QpEntity entity) {
        return new GeneralScreenHandler<>(entity, MarkerContainer::createChunkMarkerContainer);
    }

    static double inRange(double value, double min, double max) {
        var range = max - min;
        return value - (range * Math.floor((value - min) / range));
    }
}
