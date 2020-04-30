package com.yogpc.qp.item;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemQuarryPickaxe extends ItemTool {
    public ItemQuarryPickaxe() {
        super(ToolMaterial.DIAMOND, Collections.emptySet());
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.quarryPickaxe);
        setCreativeTab(QuarryPlusI.creativeTab());
        setUnlocalizedName(QuarryPlus.Names.quarryPickaxe);
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        return 5;
    }

    private Set<String> toolClasses = null;

    private Set<String> findTools() {
        return ForgeRegistries.ITEMS.getValuesCollection().stream()
            .filter(i -> i != this)
            .flatMap(i -> i.getToolClasses(new ItemStack(i)).stream())
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        if (toolClasses == null) {
            toolClasses = findTools();
        }
        return toolClasses;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Debug Pickaxe for Quarry");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubItems(tab, items);
        if (this.isInCreativeTab(tab) && Config.content().debug()) {
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
    }
}
