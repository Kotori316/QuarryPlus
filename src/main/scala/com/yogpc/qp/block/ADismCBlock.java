package com.yogpc.qp.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import cofh.api.block.IDismantleable;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.tile.IEnchantableTile;
import ic2.api.tile.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import scala.Function1;

@Optional.InterfaceList({
    @Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_modID),
    @Optional.Interface(iface = "ic2.api.tile.IWrenchable", modid = QuarryPlus.Optionals.IC2_modID)})
public abstract class ADismCBlock extends QPBlock implements IDismantleable, IWrenchable {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool ACTING = PropertyBool.create("acting");

    protected ADismCBlock(final Material material, String name, Function<QPBlock, ? extends ItemBlock> generator) {
        super(material, name, generator);
    }

    protected ADismCBlock(final Material material, String name, Function1<QPBlock, ? extends ItemBlock> generator, boolean dummy) {
        super(material, name, generator, dummy);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
    public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean returnDrops) {
        return dismantle(world, pos, state, returnDrops);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return this != QuarryPlusI.blockChunkDestroyer();
    }

    public static ArrayList<ItemStack> dismantle(World world, BlockPos pos, IBlockState state, boolean returnDrops) {
        NonNullList<ItemStack> list = NonNullList.create();
        state.getBlock().getDrops(list, world, pos, state, 0);
        world.setBlockToAir(pos);
        if (!returnDrops) {
            for (ItemStack drop : list) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
        }
        return new ArrayList<>(list);
    }

    protected void addEnchantedItem(World worldIn, IBlockState state, IEnchantableTile tile, List<ItemStack> drops) {
        final int count = quantityDropped(state, 0, worldIn.rand);
        final Item it = getItemDropped(state, worldIn.rand, 0);
        for (int i = 0; i < count; i++) {
            final ItemStack is = new ItemStack(it, 1, damageDropped(state));
            IEnchantableTile.Util.enchantmentToIS(tile, is);
            drops.add(is);
        }
    }

    protected static EnumFacing get2dOrientation(final double x1, final double z1, final double x2, final double z2) {
        final double Dx = x1 - x2;
        final double Dz = z1 - z2;
        final double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

        if (angle < 45 || angle > 315)
            return EnumFacing.EAST;
        else if (angle < 135)
            return EnumFacing.SOUTH;
        else if (angle < 225)
            return EnumFacing.WEST;
        else
            return EnumFacing.NORTH;
    }

    protected abstract boolean canRotate();

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public EnumFacing getFacing(World world, BlockPos pos) {
        if (canRotate()) {
            return world.getBlockState(pos).getValue(FACING);
        } else {
            return EnumFacing.UP;
        }
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public boolean setFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player) {
        if (canRotate()) {
            TileEntity entity = world.getTileEntity(pos);
            IBlockState state = world.getBlockState(pos);
            if (entity != null) {
                InvUtils.setNewState(world, pos, entity, state.withProperty(FACING, newDirection));
            } else {
                world.setBlockState(pos, state.withProperty(FACING, newDirection));
            }
            return true;
        }
        return false;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public boolean wrenchCanRemove(World world, BlockPos pos, EntityPlayer player) {
        return !canRotate();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public List<ItemStack> getWrenchDrops(World world, BlockPos pos, IBlockState state, TileEntity te, EntityPlayer player, int fortune) {
        Block block = state.getBlock();
        if (te instanceof IEnchantableTile) {
            IEnchantableTile tile = (IEnchantableTile) te;
            ItemStack stack = new ItemStack(block);
            IEnchantableTile.Util.enchantmentToIS(tile, stack);
            if (world.getBlockState(pos) == state) {
                world.setBlockToAir(pos);
            }
            return Collections.singletonList(stack);
        } else {
            NonNullList<ItemStack> list = NonNullList.create();
            state.getBlock().getDrops(list, world, pos, state, fortune);
            return list;
        }
    }
}
