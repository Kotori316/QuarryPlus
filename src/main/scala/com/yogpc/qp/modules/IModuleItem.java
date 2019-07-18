package com.yogpc.qp.modules;

import java.util.function.Function;

import com.yogpc.qp.tile.APowerTile;
import com.yogpc.qp.tile.HasStorage;
import com.yogpc.qp.tile.IModule;
import net.minecraft.item.ItemStack;

public interface IModuleItem {

    <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack);
}
