package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.ItemConverter;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ConverterModule extends QuarryModule {
    ItemConverter.Conversion conversion();

    static List<ItemConverter.Conversion> findConversions(Collection<QuarryModule> modules) {
        return modules.stream()
            .<ItemConverter.Conversion>mapMulti((module, conversionConsumer) -> {
                if (module instanceof ConverterModule c) {
                    conversionConsumer.accept(c.conversion());
                }
            }).toList();
    }

    record FilterModule(Set<MachineStorage.ItemKey> itemKeys) implements ConverterModule {
        public static final String NAME = "filter_module";

        @Override
        public ItemConverter.Conversion conversion() {
            return new ItemConverter.ToEmptyConverter(itemKeys);
        }

        @Override
        public ResourceLocation moduleId() {
            return ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);
        }
    }
}
