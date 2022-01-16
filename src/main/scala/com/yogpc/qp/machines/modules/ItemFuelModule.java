package com.yogpc.qp.machines.modules;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPItem;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule.HasModuleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import scala.Symbol;

public class ItemFuelModule extends QPItem implements IModuleItem {

    private final FuelModule.Mode mode;

    public ItemFuelModule(FuelModule.Mode mode) {
        super(QuarryPlus.Names.fuelModule + "_" + mode.name(), p -> p.rarity(Rarity.UNCOMMON));
        this.mode = mode;
    }

    @Override
    public <T extends APowerTile & HasStorage & HasModuleInventory> Function<T, IModule> getModule(ItemStack stack) {
        return t -> getFuelModule(stack);
    }

    public FuelModule getFuelModule(ItemStack stack) {
        return new FuelModule(mode, stack.getCount());
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleFuel");
    }

    @Override
    public boolean isCompatibleWith(IModuleItem item) {
        if (item instanceof ItemFuelModule) {
            ItemFuelModule module = (ItemFuelModule) item;
            return module.mode == this.mode;
        }
        return true;
    }
}
