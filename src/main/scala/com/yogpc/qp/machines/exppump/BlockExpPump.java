package com.yogpc.qp.machines.exppump;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.pump.BlockItemPump;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import scala.Symbol;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static net.minecraft.state.properties.BlockStateProperties.ENABLED;

/*
State ENABLED means ExpPump has exp to give player.
State WORKING means ExpPump has a connection.
 */
public class BlockExpPump extends QPBlock {
    public static final Symbol SYMBOL = Symbol.apply("ExpPump");

    public BlockExpPump() {
        super(Properties.create(Material.ANVIL).hardnessAndResistance(3.0f), QuarryPlus.Names.exppump, BlockItemPump::new);
        setDefaultState(getStateContainer().getBaseState().with(ENABLED, false).with(QPBlock.WORKING(), false));
    }

    //---------- Setting of BlockState ----------
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ENABLED, QPBlock.WORKING());
    }

    //---------- Setting of TileEntity ----------
    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.expPumpTileType();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit).isSuccess()) return ActionResultType.SUCCESS;
        if (player.getHeldItem(hand).getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable(worldIn.getTileEntity(pos))
                    .flatMap(optCast(TileExpPump.class))
                    .ifPresent(t -> player.sendStatusMessage(new StringTextComponent(
                        "Xp Amount: " + t.getXpAmount()), false));
            }
            return ActionResultType.SUCCESS;
        }
        if (!player.isCrouching()) {
            if (!worldIn.isRemote) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity instanceof TileExpPump) {
                    TileExpPump expPump = (TileExpPump) tileEntity;
                    expPump.onActivated(worldIn, pos, player);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof IEnchantableTile) {
            IEnchantableTile.Util.initConsumer(stack).accept(((IEnchantableTile) entity));
        }
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        Optional.ofNullable((TileExpPump) worldIn.getTileEntity(pos)).ifPresent(TileExpPump::G_ReInit);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluid);
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.removeBlock(pos, false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof TileExpPump) {
                    TileExpPump pump = (TileExpPump) entity;
                    pump.onBreak(worldIn);
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }

    }
}
