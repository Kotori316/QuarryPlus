package com.yogpc.qp.machines.modules;

import java.util.Optional;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.exppump.ExpPumpModule;
import com.yogpc.qp.utils.Holder;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTDynamicOps;
import scala.Symbol;

public class ItemExpPumpModule extends Item implements IDisabled, IModuleItem {
    public static final Symbol SYMBOL = Symbol.apply("ModuleExpPump");
    public static final String Key_xp = "xp";

    public ItemExpPumpModule() {
        super(new Item.Properties().group(Holder.tab()).rarity(EnumRarity.UNCOMMON));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.exppumpModule);
    }

    @Override
    public Symbol getSymbol() {
        return SYMBOL;
    }

    @Override
    public <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack) {
        int xp = Optional.ofNullable(stack.getTag())
            .map(tag -> tag.get(Key_xp))
            .flatMap(NBTDynamicOps.INSTANCE::getNumberValue)
            .map(Number::intValue)
            .orElse(0);
        return t -> {
            ExpPumpModule module = new ExpPumpModule(t);
            module.xp_$eq(xp);
            return module;
        };
    }
}
