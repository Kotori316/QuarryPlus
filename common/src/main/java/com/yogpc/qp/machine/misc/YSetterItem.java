package com.yogpc.qp.machine.misc;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class YSetterItem extends QpItem {
    public static final String NAME = "y_setter";

    public YSetterItem() {
        super(new Properties(), NAME);
    }

    protected final InteractionResult interact(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        var entity = level.getBlockEntity(pos);
        var accessor = YAccessor.get(entity);
        if (entity == null || accessor == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            // accessor.entity().syncToClient();
            PlatformAccess.getAccess().openGui((ServerPlayer) player, new GeneralScreenHandler<>(entity, YSetterContainer::new));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
