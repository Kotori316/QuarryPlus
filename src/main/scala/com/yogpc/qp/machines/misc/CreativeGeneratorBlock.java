package com.yogpc.qp.machines.misc;

import java.util.List;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.MachineBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class CreativeGeneratorBlock extends MachineBlock {
    public static final String NAME = "creative_generator";
    public final BlockItem blockItem = new BlockItem(this, new Item.Properties().tab(QuarryPlus.CREATIVE_TAB));

    public CreativeGeneratorBlock() {
        super(FabricBlockSettings.of(Material.METAL)
            .strength(1f, 1f)
            .sound(SoundType.STONE));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return QuarryPlus.ModObjects.CREATIVE_GENERATOR_TYPE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, QuarryPlus.ModObjects.CREATIVE_GENERATOR_TYPE, CreativeGeneratorTile.TICKER);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options) {
        super.appendHoverText(stack, world, tooltip, options);
        tooltip.add(new TextComponent("Works only for Quarry"));
    }
}
