package com.yogpc.qp.machines.misc;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class CreativeGeneratorBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "creative_generator";

    public CreativeGeneratorBlock() {
        super(QPBlock.Properties.of(Material.METAL)
            .strength(1f, 1f)
            .sound(SoundType.STONE), NAME);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.CREATIVE_GENERATOR_TYPE.create(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                world.getBlockEntity(pos, Holder.CREATIVE_GENERATOR_TYPE).ifPresent(o ->
                    NetworkHooks.openScreen(((ServerPlayer) player), o, pos));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public Set<ResourceLocation> disallowedDim() {
        // This machine should work in any dimensions.
        return Set.of();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : checkType(type, Holder.CREATIVE_GENERATOR_TYPE,
            CombinedBlockEntityTicker.of(
                this, world,
                CreativeGeneratorTile.TICKER));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options) {
        super.appendHoverText(stack, world, tooltip, options);
        tooltip.add(Component.literal("Works only for Quarry"));
    }

}
