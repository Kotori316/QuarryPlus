package com.yogpc.qp.machines.quarry;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

class ItemQuarry extends QPBlock.QPBlockItem implements EnchantableItem {
    ItemQuarry(QPBlock block) {
        super(block, new FabricItemSettings().tab(QuarryPlus.ModObjects.CREATIVE_TAB).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        CompoundTag tag = Optional.ofNullable(getBlockEntityData(stack)).orElse(new CompoundTag());
        if (tag.getBoolean("bedrockRemove")) {
            tooltip.add(new TextComponent("BedrockRemove on"));
        }
        if (tag.contains("digMinY")) {
            tooltip.add(new TextComponent("DigMinY " + tag.getInt("digMinY")));
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
        super.fillItemCategory(group, stacks);
        if (this.allowdedIn(group)) {
            ItemStack stack = new ItemStack(this);
            {
                ItemStack copy = stack.copy();
                copy.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
                copy.enchant(Enchantments.UNBREAKING, 3);
                copy.enchant(Enchantments.BLOCK_FORTUNE, 3);
                stacks.add(copy);
            }
            {
                ItemStack copy = stack.copy();
                copy.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
                copy.enchant(Enchantments.UNBREAKING, 3);
                copy.enchant(Enchantments.SILK_TOUCH, 1);
                stacks.add(copy);
            }
        }
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE, Enchantments.SILK_TOUCH);
    }
}
