package com.yogpc.qp.machines.modules;

import java.util.function.Function;

import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IModule;
import net.minecraft.item.ItemStack;

public interface IModuleItem {

    <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack);
}
