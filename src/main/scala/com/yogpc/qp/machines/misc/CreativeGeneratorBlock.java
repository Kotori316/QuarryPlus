package com.yogpc.qp.machines.misc;

import java.util.List;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CreativeGeneratorBlock extends BlockWithEntity {
    public static final String NAME = "creative_generator";
    public final BlockItem blockItem = new BlockItem(this, new Item.Settings().group(QuarryPlus.CREATIVE_TAB));

    public CreativeGeneratorBlock() {
        super(FabricBlockSettings.of(Material.METAL)
            .strength(1f, 1f)
            .sounds(BlockSoundGroup.STONE));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return QuarryPlus.ModObjects.CREATIVE_GENERATOR_TYPE.instantiate(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, QuarryPlus.ModObjects.CREATIVE_GENERATOR_TYPE, CreativeGeneratorTile.TICKER);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(new LiteralText("Works only for Quarry"));
    }
}
