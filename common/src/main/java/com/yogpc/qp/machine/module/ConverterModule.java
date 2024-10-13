package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.ItemConverter;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        public FilterModule(@Nullable Tag tag) {
            this(
                Optional.ofNullable(tag)
                    .filter(ListTag.class::isInstance)
                    .map(ListTag.class::cast)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(t -> MachineStorage.ITEM_KEY_MAP_CODEC.codec().parse(NbtOps.INSTANCE, t).result())
                    .flatMap(Optional::stream)
                    .collect(Collectors.toUnmodifiableSet())
            );
        }

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
