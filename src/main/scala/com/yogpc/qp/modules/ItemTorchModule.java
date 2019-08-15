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

public class ItemTorchModule extends Item implements IDisabled, IModuleItem {

    public ItemTorchModule() {
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.torchModule);
        setUnlocalizedName(QuarryPlus.Names.torchModule);
        setCreativeTab(QuarryPlusI.creativeTab());
    }

    @Override
    public <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack) {
        return TorchModule::get;
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleTorch");
    }

}
