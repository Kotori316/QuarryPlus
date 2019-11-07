package com.yogpc.qp.machines.modules;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.utils.Holder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import scala.Symbol;

public class ItemBedrockModule extends Item implements IModuleItem {
    public ItemBedrockModule() {
        super(new Item.Properties().group(Holder.tab()).rarity(Rarity.UNCOMMON));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.bedrockModule);
    }

    @Override
    public <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack) {
        return RemoveBedrockModule::new;
    }

    @Override
    public Symbol getSymbol() {
        return Symbol.apply("ModuleRemoveBedrock");
    }

}
