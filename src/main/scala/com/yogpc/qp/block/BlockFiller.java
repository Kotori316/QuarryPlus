package com.yogpc.qp.block;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFiller extends ADismCBlock {
    public BlockFiller() {
        super(Material.IRON, QuarryPlus.Names.filler, ItemBlock::new);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
    }

    @Override
    protected boolean canRotate() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }
}
