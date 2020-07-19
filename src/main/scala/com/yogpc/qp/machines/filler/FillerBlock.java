package com.yogpc.qp.machines.filler;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class FillerBlock extends QPBlock {
    public FillerBlock() {
        super(Properties.create(Material.IRON).hardnessAndResistance(1.5f, 10f), QuarryPlus.Names.filler, BlockItem::new);
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.fillerType();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (InvUtils.isDebugItem(player, hand).isSuccess()) return ActionResultType.SUCCESS;
        if (!player.isCrouching()) {
            if (!worldIn.isRemote) {
                NetworkHooks.openGui(((ServerPlayerEntity) player),
                    (FillerTile) worldIn.getTileEntity(pos),
                    pos);
            }
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state, worldIn, pos, player, hand, hit);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Optional.ofNullable((FillerTile) worldIn.getTileEntity(pos))
                .ifPresent(FillerTile.requestTicket);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!newState.isIn(state.getBlock())) {
            if (!worldIn.isRemote) {
                TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof FillerTile) {
                    FillerTile t = (FillerTile) entity;
                    InventoryHelper.dropInventoryItems(worldIn, pos, t.inventory());
                    InventoryHelper.dropInventoryItems(worldIn, pos, t.moduleInv());
                    worldIn.updateComparatorOutputLevel(pos, state.getBlock());
                }
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
