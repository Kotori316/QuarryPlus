package com.yogpc.qp.machine.storage;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpBlockItem;
import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public final class DebugStorageBlock extends QpEntityBlock {
    public static final String NAME = "debug_storage";

    public DebugStorageBlock() {
        super(Properties.of().noLootTable(), NAME, b -> new QpBlockItem(b, new Item.Properties()));
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new DebugStorageBlock();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof DebugStorageEntity storage) {
            if (!level.isClientSide) {
                PlatformAccess.getAccess().openGui((ServerPlayer) player, new GeneralScreenHandler<>(storage, DebugStorageContainer::new));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Insertion Only. No extraction"));
        tooltipComponents.add(Component.literal("Just for debug").withStyle(ChatFormatting.RED));
    }
}
