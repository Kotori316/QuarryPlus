package com.yogpc.qp.machines.mini_quarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.module.EnergyModuleItem;
import com.yogpc.qp.machines.module.QuarryModuleProvider;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class MiniQuarryInventory extends SimpleContainer {
    public MiniQuarryInventory() {
        super(5);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(Holder.ITEM_FUEL_MODULE_NORMAL) ||
                stack.getItem() instanceof DiggerItem;
    }

    List<ItemStack> tools() {
        return Stream.concat(
                IntStream.range(0, getContainerSize())
                        .mapToObj(this::getItem)
                        .filter(s -> s.getItem() instanceof DiggerItem),
                Stream.of(ItemStack.EMPTY)
        ).toList();
    }

    Optional<EnergyModuleItem.EnergyModule> getEnergyModule() {
        return IntStream.range(0, getContainerSize())
                .mapToObj(this::getItem)
                .filter(i -> i.getItem() instanceof QuarryModuleProvider.Item)
                .map(i -> ((QuarryModuleProvider.Item) i.getItem()).getModule(i))
                .mapMulti(MapMulti.cast(EnergyModuleItem.EnergyModule.class))
                .max(Comparator.comparingInt(EnergyModuleItem.EnergyModule::energy));
    }
}
