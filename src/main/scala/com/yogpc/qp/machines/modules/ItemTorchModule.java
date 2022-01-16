package com.yogpc.qp.machines.modules;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPItem;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import scala.Symbol;

public class ItemTorchModule extends QPItem implements IDisabled, IModuleItem {

    public ItemTorchModule() {
        super(QuarryPlus.Names.torchModule, p -> p.rarity(Rarity.UNCOMMON));
    }

    @Override
    public <T extends APowerTile & HasStorage & ContainerQuarryModule.HasModuleInventory> Function<T, IModule> getModule(ItemStack stack) {
        return TorchModule::get;
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleTorch");
    }

}
