package com.yogpc.qp.machines.modules;

import java.util.function.Function;

import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.IModule;
import net.minecraft.item.ItemStack;
import scala.Option;

public interface IModuleItem extends IDisabled {

    <T extends APowerTile & HasStorage> Function<T, IModule> getModule(ItemStack stack);

    default <T extends APowerTile & HasStorage> Option<IModule> apply(ItemStack stack, T t) {
        if (enabled()) {
            return Option.apply(getModule(stack).apply(t));
        } else {
            return Option.empty();
        }
    }
}
