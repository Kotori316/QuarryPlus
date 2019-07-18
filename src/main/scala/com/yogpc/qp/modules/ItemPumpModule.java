package com.yogpc.qp.modules;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.APowerTile;
import com.yogpc.qp.tile.HasStorage;
import com.yogpc.qp.tile.IModule;
import com.yogpc.qp.tile.PumpModule;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import scala.Symbol;

public class ItemPumpModule extends Item implements IDisabled, IModuleItem {
    public static final Symbol SYMBOL = Symbol.apply("ModulePump");

    public ItemPumpModule() {
        setUnlocalizedName(QuarryPlus.Names.pumpModule);
        setCreativeTab(QuarryPlusI.creativeTab());
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.pumpModule);
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public Symbol getSymbol() {
        return SYMBOL;
    }

    @Override
    public <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack) {
        return t -> PumpModule.fromModule(t, () -> EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack));
    }
}
