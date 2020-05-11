package com.yogpc.qp.machines.modules;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule.HasModuleInventory;
import com.yogpc.qp.utils.Holder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import scala.Symbol;

public class ItemFuelModule extends Item implements IModuleItem {

    private final FuelModule.Mode mode;

    public ItemFuelModule(FuelModule.Mode mode) {
        super(new Item.Properties().group(Holder.tab()).rarity(Rarity.UNCOMMON));
        this.mode = mode;
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.fuelModule + "_" + mode.name());
    }

    @Override
    public <T extends APowerTile & HasStorage & HasModuleInventory> Function<T, IModule> getModule(ItemStack stack) {
        return t -> new FuelModule(mode);
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleFuel");
    }
}
