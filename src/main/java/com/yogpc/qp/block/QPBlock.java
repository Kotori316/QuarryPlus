package com.yogpc.qp.block;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class QPBlock extends BlockContainer {

    public ItemBlock itemBlock;

    public QPBlock(Material materialIn, String name) {
        super(materialIn);
        setUnlocalizedName(name);
        setRegistryName(QuarryPlus.modID, name);
        setCreativeTab(QuarryPlusI.ct);
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(QuarryPlus.modID, name);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }
}
