package com.yogpc.qp.machines.module;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class ReplacerDummyBlock extends AbstractGlassBlock {
    public static final String NAME = "dummy_replacer";
    public final ResourceLocation location = new ResourceLocation(QuarryPlus.modID, NAME);
    public final BlockItem blockItem;

    public ReplacerDummyBlock() {
        super(Properties.of(Material.GLASS)
            .noOcclusion()
            .noLootTable()
            .isValidSpawn((state, world, pos, type) -> false)
            .isSuffocating((state, world, pos) -> false)
            .isRedstoneConductor((state, world, pos) -> false)
            .isViewBlocking((state, world, pos) -> false)
            .lightLevel(value -> 15)
            .strength(1.0f)
        );
        blockItem = new BlockItem(this, new Item.Properties());
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return false;
    }
}
