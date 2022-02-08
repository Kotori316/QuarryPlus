package com.yogpc.qp.machines.filler;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

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
        List<Predicate<BlockState>> rejects = List.of(
            BlockState::hasBlockEntity,
            state -> state.getMaterial() == Material.DECORATION,
            BlockState::isAir,
            s -> !s.getFluidState().isEmpty(),
            b -> false
        );
        return pStack.getItem() instanceof BlockItem blockItem // Empty is also checked.
            && rejects.stream().noneMatch(p -> p.test(blockItem.getBlock().defaultBlockState()));
    }

    Optional<ItemStack> getFirstItem() {
        return IntStream.range(0, getContainerSize())
            .mapToObj(this::getItem)
            .filter(FillerContainer::canAccept)
            .findFirst();
    }
}
