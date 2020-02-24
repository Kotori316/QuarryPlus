package com.yogpc.qp.modules;

import java.util.function.Function;

import com.yogpc.qp.tile.APowerTile;
import com.yogpc.qp.tile.HasStorage;
import com.yogpc.qp.tile.IModule;
import net.minecraft.item.ItemStack;
import scala.Option;

public interface IModuleItem extends IDisabled {

    <T extends APowerTile> Function<T, IModule> getModule(ItemStack stack);

    default <T extends APowerTile> Option<IModule> apply(ItemStack stack, T t) {
        if (enabled()) {
            return Option.apply(getModule(stack).apply(t));
        } else {
            return Option.empty();
        }
    }
}
