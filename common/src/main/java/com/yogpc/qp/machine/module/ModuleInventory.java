package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ModuleInventory extends SimpleContainer {
    static final Set<ResourceLocation> allowMultiModule = Set.of(
        ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "void_module")
    );
    private final Predicate<QuarryModule> staticFilter;
    private final Function<ModuleInventory, Set<QuarryModule>> holdings;
    private final Runnable onChanged;

    public ModuleInventory(int size, Predicate<QuarryModule> staticFilter, Function<ModuleInventory, Set<QuarryModule>> holdings, Runnable onChanged) {
        super(size);
        this.staticFilter = staticFilter;
        this.holdings = holdings;
        this.onChanged = onChanged;
    }

    @VisibleForTesting
    ModuleInventory(int size) {
        this(size, q -> true, m -> Set.copyOf(m.getModules()), () -> {
        });
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (stack.getItem() instanceof QuarryModuleProvider.Item provider) {
            if (ItemStack.isSameItem(getItem(index), stack)) return true;
            var toAdd = provider.getModule(stack);
            if (!staticFilter.test(toAdd)) return false;

            return holdings.apply(this).stream()
                .map(QuarryModule::moduleId)
                .filter(Predicate.not(allowMultiModule::contains))
                .filter(Predicate.isEqual(toAdd.moduleId()))
                .findAny()
                .isEmpty()
                ;
        } else {
            return false;
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChanged.run();
    }

    public Set<QuarryModule> getModules() {
        return getModules(getItems().stream());
    }

    @VisibleForTesting
    static Set<QuarryModule> getModules(Stream<ItemStack> stream) {
        return stream
            .filter(s -> s.getItem() instanceof QuarryModuleProvider.Item)
            .mapMulti(ModuleInventory::itemValidation)
            .map(s -> ((QuarryModuleProvider.Item) s.getItem()).getModule(s))
            .collect(Collectors.toUnmodifiableSet());
    }

    static void itemValidation(ItemStack stack, Consumer<ItemStack> consumer) {
        if (!(stack.getItem() instanceof QpItem item) || item.isEnabled()) {
            consumer.accept(stack);
        }
    }
}
