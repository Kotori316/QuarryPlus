package com.yogpc.qp.machines.quarry;

import java.util.List;
import java.util.Set;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.module.ModuleInventory;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

@SuppressWarnings("DuplicatedCode")
class QuarryItem extends QPBlock.QPBlockItem implements EnchantableItem {
    QuarryItem(QPBlock block) {
        super(block, new Item.Properties().fireResistant().tab(Holder.TAB));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        super.fillItemCategory(tab, stacks);
        if (this.allowdedIn(tab)) {
            var stack = new ItemStack(this);
            {
                var copy = stack.copy();
                copy.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
                copy.enchant(Enchantments.UNBREAKING, 3);
                copy.enchant(Enchantments.BLOCK_FORTUNE, 3);
                stacks.add(copy);
            }
            {
                var copy = stack.copy();
                copy.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
                copy.enchant(Enchantments.UNBREAKING, 3);
                copy.enchant(Enchantments.SILK_TOUCH, 1);
                stacks.add(copy);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        var entityTag = stack.getTagElement(BlockItem.BLOCK_ENTITY_TAG);
        if (entityTag != null) {
            var modules = ModuleInventory.loadModulesFromTag(entityTag.getCompound("moduleInventory"));
            modules.stream().map(Object::toString).map(TextComponent::new).forEach(list::add);
        }
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE, Enchantments.SILK_TOUCH);
    }
}
