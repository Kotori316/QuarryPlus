package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import static com.yogpc.qp.machine.QpBlockProperty.WORKING;

public class AdvPumpBlock extends QpEntityBlock {
    public static final String NAME = "adv_pump";

    public AdvPumpBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, AdvPumpItem::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false));
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new AdvPumpBlock();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, this.<AdvPumpEntity>getBlockEntityType().orElse(null), CombinedBlockEntityTicker.of(this, level,
            PowerEntity.logTicker(),
            AdvPumpEntity::serverTick,
            MachineStorage.pushFluidTicker()
        ));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AdvPumpEntity pump) {
            if (!level.isClientSide()) {
                if (pump.enabled) {
                    PlatformAccess.getAccess().openGui((ServerPlayer) player, new GeneralScreenHandler<>(pump, AdvPumpContainer::new));
                } else {
                    player.sendOverlayMessage(Component.translatable("quarryplus.chat.disable_message", getName()));
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AdvPumpEntity pump) {
            pump.updateMaxEnergyWithEnchantment(level);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (level.getBlockEntity(pos) instanceof AdvPumpEntity) {
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }
}
