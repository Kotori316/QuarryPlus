package com.yogpc.qp.machines.filler;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FillerBlock extends QPBlock {
    public FillerBlock() {
        super(Properties.create(Material.IRON).hardnessAndResistance(1.5f, 10f), QuarryPlus.Names.filler, BlockItem::new);
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.fillerType();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Optional.ofNullable((FillerTile) worldIn.getTileEntity(pos))
                .ifPresent(FillerTile.requestTicket);
        }
    }
}
