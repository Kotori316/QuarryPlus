package com.yogpc.qp.machines.modules;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.EnchantmentHolder;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPItem;
import com.yogpc.qp.machines.pump.PumpModule;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import scala.Symbol;

public class ItemPumpModule extends QPItem implements IDisabled, IModuleItem {

    public ItemPumpModule() {
        super(QuarryPlus.Names.pumpModule, p -> p.rarity(Rarity.UNCOMMON));
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModulePump");
    }

    @Override
    public <T extends APowerTile & HasStorage & ContainerQuarryModule.HasModuleInventory> Function<T, IModule> getModule(ItemStack stack) {
        return t -> PumpModule.fromModule(t, () -> getUnbreakingLevel(t, stack));
    }

    private static int getUnbreakingLevel(APowerTile t, ItemStack stack) {
        int stackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        int tile;
        if (t instanceof EnchantmentHolder.EnchantmentProvider) {
            EnchantmentHolder.EnchantmentProvider p = (EnchantmentHolder.EnchantmentProvider) t;
            tile = p.getEnchantmentHolder().unbreaking();
        } else {
            tile = 0;
        }
        return Math.max(stackLevel, tile);
    }
}
