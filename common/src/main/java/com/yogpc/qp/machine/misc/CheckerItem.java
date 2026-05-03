package com.yogpc.qp.machine.misc;

import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CheckerItem extends QpItem {
    public static final String NAME = "status_checker";

    public CheckerItem() {
        super(new Properties(), NAME);
    }

    protected final InteractionResult outputLog(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player, @NotNull ItemStack stack) {
        if (!isEnabled()) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("quarryplus.chat.disable_message", stack.getItemName()));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        if (level.getBlockEntity(pos) instanceof QpEntity e) {
            if (!level.isClientSide() && player != null) {
                e.checkerLogs().forEach(c -> player.sendSystemMessage(c));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
