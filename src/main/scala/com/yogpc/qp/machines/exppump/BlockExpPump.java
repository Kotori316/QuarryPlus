package com.yogpc.qp.machines.exppump;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
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
        super(Properties.create(Material.ANVIL).hardnessAndResistance(5.0f), QuarryPlus.Names.exppump, ItemBlockExpPump::new);
        setDefaultState(getStateContainer().getBaseState().with(ENABLED, false).with(QPBlock.WORKING(), false));
    }

    //---------- Setting of BlockState ----------
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(ENABLED, QPBlock.WORKING());
    }

    //---------- Setting of TileEntity ----------
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.expPumpTileType().create();
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) return true;
        if (player.getHeldItem(hand).getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable(worldIn.getTileEntity(pos))
                    .flatMap(optCast(TileExpPump.class))
                    .ifPresent(t -> player.sendStatusMessage(new TextComponentString(
                        "Xp Amount: " + t.getXpAmount()), false));
            }
            return true;
        }
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity instanceof TileExpPump) {
                    TileExpPump expPump = (TileExpPump) tileEntity;
                    expPump.onActivated(worldIn, pos, player);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof IEnchantableTile) {
            IEnchantableTile.Util.initConsumer(stack).accept(((IEnchantableTile) entity));
        }
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        Optional.ofNullable((TileExpPump) worldIn.getTileEntity(pos)).ifPresent(TileExpPump::G_ReInit);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest, IFluidState fluid) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluid);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.removeBlock(pos);
    }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileExpPump) {
            TileExpPump pump = (TileExpPump) entity;
            ItemStack stack = new ItemStack(this, 1);
            IEnchantableTile.Util.enchantmentToIS(pump, stack);
            drops.add(stack);
        }
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
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
