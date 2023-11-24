package com.yogpc.qp.machines.filler;

import com.yogpc.qp.machines.module.ReplacerModule;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Optional;
import java.util.stream.IntStream;

public final class FillerContainer extends SimpleContainer {
    public FillerContainer(int size) {
        super(size);
    }

    @Override
    public boolean canPlaceItem(int pIndex, ItemStack pStack) {
        return canAccept(pStack);
    }

    @VisibleForTesting
    static boolean canAccept(@NotNull ItemStack pStack) {
        return pStack.getItem() instanceof BlockItem blockItem // Empty is also checked.
            && ReplacerModule.rejects.stream().noneMatch(p -> p.test(blockItem.getBlock().defaultBlockState()));
    }

    Optional<ItemStack> getFirstItem() {
        return IntStream.range(0, getContainerSize())
            .mapToObj(this::getItem)
            .filter(FillerContainer::canAccept)
            .findFirst();
    }

    LazyOptional<IItemHandler> createHandler() {
        return LazyOptional.of(() -> new InvWrapper(this));
    }
}
