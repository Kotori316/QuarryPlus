package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.fabric.ExtendedGeneralScreenHandler;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class QuarryBlockFabric extends QuarryBlock {
    public QuarryBlockFabric() {
        super(QuarryItemFabric::new);
    }

    @Override
    protected QuarryBlockFabric createBlock(Properties properties) {
        return new QuarryBlockFabric();
    }

    @Override
    protected void openGui(ServerPlayer player, Level level, BlockPos pos, QuarryEntity quarry) {
        player.openMenu(new ExtendedGeneralScreenHandler<>(new GeneralScreenHandler<>(quarry, QuarryMenuFabric::new)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var item = PlatformAccess.getAccess().registerObjects().bedrockModuleItem().get();
        if (stack.is(item)) {
            var entity = this.<QuarryEntityFabric>getBlockEntityType().map(t -> t.getBlockEntity(level, pos)).orElse(null);
            if (entity != null && !level.isClientSide()) {
                if (!item.isEnabled()) {
                    player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", Component.translatable(stack.getDescriptionId())), true);
                } else if (!entity.enabled) {
                    player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
                } else {
                    entity.shouldRemoveBedrock = !entity.shouldRemoveBedrock;
                    player.displayClientMessage(Component.literal("Set removeBedrock: %s".formatted(entity.shouldRemoveBedrock)), false);
                }
            }
            return ItemInteractionResult.SUCCESS_SERVER;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (dropExperience && level.getBlockEntity(pos) instanceof QuarryEntityFabric q) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), q.getExp());
        }
    }
}
