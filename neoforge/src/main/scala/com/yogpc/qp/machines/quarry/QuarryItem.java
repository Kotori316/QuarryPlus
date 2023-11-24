package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.module.ModuleInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
class QuarryItem extends QPBlock.QPBlockItem implements EnchantableItem {
    QuarryItem(QPBlock block) {
        super(block, new Item.Properties().fireResistant());
    }

    @Override
    public List<ItemStack> creativeTabItem() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(this));
        {
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
        return stacks;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        var entityTag = BlockItem.getBlockEntityData(stack);
        if (entityTag != null) {
            var modules = ModuleInventory.loadModulesFromTag(entityTag.getCompound("moduleInventory"));
            modules.stream().map(Object::toString).map(Component::literal).forEach(list::add);
        }
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        // return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE, Enchantments.SILK_TOUCH);
        return QuarryPlus.config.acceptableEnchantmentsMap.getAllowedEnchantments(getRegistryName());
    }
}
