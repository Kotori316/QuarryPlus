package com.yogpc.qp.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import cofh.api.block.IDismantleable;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_block)
public abstract class ADismCBlock extends QPBlock implements IDismantleable {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool ACTING = PropertyBool.create("acting");

    protected ADismCBlock(final Material material, String name, Function<QPBlock, ? extends ItemBlock> generator) {
        super(material, name, generator);
    }

    @Override
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
    }
}
