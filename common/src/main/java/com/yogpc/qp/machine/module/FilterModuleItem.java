package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class FilterModuleItem extends QpItem implements QuarryModuleProvider.Item {
    public static final String NAME = ConverterModule.FilterModule.NAME;

    public FilterModuleItem() {
        super(new Properties(), NAME);
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        Set<MachineStorage.ItemKey> targets = Set.copyOf(stack.getOrDefault(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT, List.of()));
        return new ConverterModule.FilterModule(targets);
    }
}
