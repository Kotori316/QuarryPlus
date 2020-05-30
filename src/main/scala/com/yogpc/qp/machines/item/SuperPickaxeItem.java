package com.yogpc.qp.machines.item;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class SuperPickaxeItem extends ToolItem {
    public SuperPickaxeItem() {
        super(0, 0, ItemTier.DIAMOND, Collections.emptySet(),
            new Properties().addToolType(ToolType.AXE, 5).addToolType(ToolType.PICKAXE, 5).addToolType(ToolType.SHOVEL, 5)
                .group(Holder.tab()));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.quarry_pickaxe);
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        return 5;
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {
        Stream<Item> itemStream = Stream.of(Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.SHEARS);
        return itemStream.anyMatch(i -> i.canHarvestBlock(state));
    }

    private Set<ToolType> toolTypes = null;

    @SuppressWarnings("unchecked")
    private Set<ToolType> getToolTypes() {
        try {
            Field valuesField = ToolType.class.getDeclaredField("values");
            valuesField.setAccessible(true);
            Map<String, ToolType> toolTypeMap = ((Map<String, ToolType>) valuesField.get(null));
            return new HashSet<>(toolTypeMap.values());
        } catch (ReflectiveOperationException e) {
            QuarryPlus.LOGGER.error("Errored in getting tool types.", e);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<ToolType> getToolTypes(ItemStack stack) {
        if (toolTypes == null)
            toolTypes = getToolTypes();
        return toolTypes;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent("Quarry Debug Pickaxe"));
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            super.fillItemGroup(group, items);
            try {
                if (Config.common().debug()) {
                    ItemStack stack = new ItemStack(this);
                    stack.addEnchantment(Enchantments.EFFICIENCY, 5);
                    stack.addEnchantment(Enchantments.UNBREAKING, 3);
                    {
                        ItemStack stack1 = stack.copy();
                        stack1.addEnchantment(Enchantments.FORTUNE, 3);
                        items.add(stack1);
                    }
                    {
                        ItemStack stack1 = stack.copy();
                        stack1.addEnchantment(Enchantments.SILK_TOUCH, 1);
                        items.add(stack1);
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }
}
