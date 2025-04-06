package com.yogpc.qp.machine;

import com.mojang.serialization.MapCodec;
import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Function;

public abstract class QpBlock extends Block implements InCreativeTabs {
    public final ResourceLocation name;
    public final BlockItem blockItem;

    public QpBlock(Properties properties, String name, Function<? super QpBlock, ? extends BlockItem> itemGenerator) {
        super(properties.setId(QuarryPlus.blockKey(name)));
        this.name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, name);
        this.blockItem = itemGenerator.apply(this);
    }

    protected abstract QpBlock createBlock(Properties properties);

    private final MapCodec<QpBlock> codec = simpleCodec(this::createBlock);

    @Override
    protected MapCodec<? extends Block> codec() {
        return this.codec;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    }
}
