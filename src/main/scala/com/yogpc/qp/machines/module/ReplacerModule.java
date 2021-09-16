package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.yogpc.qp.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public final class ReplacerModule implements QuarryModule {
    public static final List<Predicate<BlockState>> rejects = List.of(
        BlockState::hasBlockEntity,
        state -> state.getMaterial() == Material.DECORATION,
        BlockState::isAir,
        s -> !s.getFluidState().isEmpty(),
        b -> false
    );
    private final Supplier<BlockState> supplier;

    public ReplacerModule(Supplier<BlockState> supplier) {
        this.supplier = supplier;
    }

    public ReplacerModule(BlockState state) {
        this(() -> state);
    }

    @Override
    public ResourceLocation moduleId() {
        return Holder.ITEM_REPLACER_MODULE.getRegistryName();
    }

    @Override
    public String toString() {
        return "ReplacerModule{" +
            "state=" + supplier.get() +
            '}';
    }

    public BlockState getState() {
        return supplier.get();
    }
}
