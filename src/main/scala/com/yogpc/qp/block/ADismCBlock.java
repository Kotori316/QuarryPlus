package com.yogpc.qp.block;

import java.util.function.Function;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.item.ItemBlock;

public abstract class ADismCBlock extends QPBlock {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool ACTING = PropertyBool.create("acting");

    protected ADismCBlock(final Material material, String name, Function<QPBlock, ? extends ItemBlock> generator) {
        super(material, name, generator);
    }

    /*@Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_block)
    public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean returnDrops) {
        return dismantle(world, pos, state, returnDrops);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_block)
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    public static ArrayList<ItemStack> dismantle(World world, BlockPos pos, IBlockState state, boolean returnDrops) {
        List<ItemStack> drops = state.getBlock().getDrops(world, pos, state, 0);
        world.setBlockToAir(pos);
        if (!returnDrops) {
            for (ItemStack drop : drops) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
        }
        return new ArrayList<>(drops);
    }*/
}
