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

public final class FlexibleMarkerBlock extends ExMarkerBlock {
    public static final String NAME = "flexible_marker";

    public FlexibleMarkerBlock() {
        super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel(value -> 7).noCollission(), NAME);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        float rotationYawHead = placer != null ? placer.getYHeadRot() : 0f;
        var direction = Direction.fromYRot(rotationYawHead);
        this.<FlexibleMarkerEntity>getBlockEntityType().map(e -> e.getBlockEntity(level, pos)).ifPresent(m -> m.init(direction));
    }

    @Override
    protected GeneralScreenHandler<?> getScreenHandler(QpEntity entity) {
        return new GeneralScreenHandler<>(entity, MarkerContainer::createFlexibleMarkerContainer);
    }
}
