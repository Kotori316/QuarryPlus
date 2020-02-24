package com.yogpc.qp.modules;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.APowerTile;
import com.yogpc.qp.tile.HasStorage;
import com.yogpc.qp.tile.IModule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import scala.Symbol;

public class ItemFuelModule extends Item implements IModuleItem {

    private final FuelModule.Mode mode;

    public ItemFuelModule(FuelModule.Mode mode) {
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.fuelModule + "_" + mode.name());
        setUnlocalizedName(QuarryPlus.Names.fuelModule + "_" + mode.name());
        setCreativeTab(QuarryPlusI.creativeTab());
        this.mode = mode;
    }

    @Override
    public <T extends APowerTile> Function<T, IModule> getModule(ItemStack stack) {
        return t -> new FuelModule(mode);
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleFuel");
    }
}
