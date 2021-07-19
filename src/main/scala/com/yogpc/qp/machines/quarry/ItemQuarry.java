package com.yogpc.qp.machines.quarry;

import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

class ItemQuarry extends BlockItem {
    ItemQuarry(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantability() {
        return 25;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        var tag = Optional.ofNullable(stack.getSubNbt(BLOCK_ENTITY_TAG_KEY)).orElse(new NbtCompound());
        if (tag.getBoolean("bedrockRemove")) {
            tooltip.add(new LiteralText("BedrockRemove on"));
        }
    }
}
