package com.yogpc.qp.machines.mini_quarry;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class MiniQuarryBlock extends QPBlock {
    public MiniQuarryBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.5f, 10f).sound(SoundType.STONE),
            QuarryPlus.Names.mini_quarry, MiniQuarryItem::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.miniQuarryType();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit).isSuccess()) return ActionResultType.SUCCESS;
        if (!player.isCrouching()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable(worldIn.getTileEntity(pos))
                    .flatMap(optCast(MiniQuarryTile.class))
                    .ifPresent(t -> NetworkHooks.openGui((ServerPlayerEntity) player, t, pos));
            }
            return ActionResultType.SUCCESS;
        } else
            return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            if (placer != null) {
                Direction facing = placer.getHorizontalFacing().getOpposite();
                worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            }
            Optional.ofNullable(worldIn.getTileEntity(pos))
                .flatMap(optCast(MiniQuarryTile.class))
                .ifPresent(t -> {
                    IEnchantableTile.Util.init(t, stack.getEnchantmentTagList());
                    MiniQuarryTile.requestTicket.accept(t);
                });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (!worldIn.isRemote) {
            boolean powered = worldIn.isBlockPowered(pos);
            Optional.ofNullable(worldIn.getTileEntity(pos))
                .flatMap(optCast(MiniQuarryTile.class))
                .ifPresent(t -> {
                    if (powered) {
                        if (!t.rs()) {
                            t.gotRSPulse();
                        }
                    }
                    t.rs_$eq(powered);
                });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.isIn(newState.getBlock())) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof MiniQuarryTile) {
                MiniQuarryTile tile = (MiniQuarryTile) entity;
                InvUtils.dropAndUpdateInv(worldIn, pos, tile.getInv(), this);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }
}
