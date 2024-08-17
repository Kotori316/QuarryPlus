package com.yogpc.qp.machine.misc;

import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CheckerItem extends QpItem {
    public static final String NAME = "status_checker";

    public CheckerItem() {
        super(new Properties(), NAME);
    }

    protected final InteractionResult outputLog(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        if (!isEnabled()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getDescription()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (level.getBlockEntity(pos) instanceof PowerEntity e) {
            if (!level.isClientSide() && player != null) {
                e.checkerLogs().forEach(c -> player.displayClientMessage(c, false));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
