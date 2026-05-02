package com.yogpc.qp.machine.placer;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.QpBlockItem;
import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.TRIGGERED;

public abstract class AbstractPlacerBlock extends QpEntityBlock {
    public AbstractPlacerBlock(String name) {
        super(
            Properties.of()
                .mapColor(MapColor.METAL)
                .pushReaction(PushReaction.BLOCK).strength(1.2f),
            name,
            b -> new QpBlockItem(b, new Item.Properties())
        );
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isCrouching() && stack.is(Items.REDSTONE_TORCH)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AbstractPlacerTile placer) {
                if (placer.enabled) {
                    placer.cycleRedStoneMode();
                    player.sendOverlayMessage(Component.translatable("quarryplus.chat.placer_rs", placer.redstoneMode.toString()));
                } else {
                    player.sendSystemMessage(Component.translatable("quarryplus.chat.disable_message", getName()));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AbstractPlacerTile placer) {
            if (!level.isClientSide()) {
                if (placer.enabled) {
                    PlatformAccess.getAccess().openGui((ServerPlayer) player, createScreenHandler(placer));
                } else {
                    player.sendOverlayMessage(Component.translatable("quarryplus.chat.disable_message", getName()));
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    protected abstract GeneralScreenHandler<?> createScreenHandler(AbstractPlacerTile placer);

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof AbstractPlacerTile placer) {
            boolean isPowered = state.getValue(TRIGGERED);
            if (isPowered) {
                placer.placeBlock();
            } else {
                placer.breakBlock();
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AbstractPlacerTile placer) {
            boolean poweredNow = placer.isPowered();
            boolean poweredOld = state.getValue(TRIGGERED);
            if (poweredNow != poweredOld) {
                level.scheduleTick(pos, this, 1);
                level.setBlock(pos, state.setValue(TRIGGERED, poweredNow), Block.UPDATE_INVISIBLE);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (Minecraft.getInstance().hasShiftDown()) {
            tooltipComponents.add(Component.translatable("quarryplus.tooltip.placer_plus"));
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
