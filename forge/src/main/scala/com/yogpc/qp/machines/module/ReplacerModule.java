package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ReplacerModule implements QuarryModule {
    public static final List<Predicate<BlockState>> rejects = List.of(
        BlockState::hasBlockEntity,
        ReplacerModule::invalidClass,
        BlockState::isAir,
        s -> !s.getFluidState().isEmpty(),
        b -> false
    );

    private static boolean invalidClass(BlockState state) {
        var classes = Stream.of(TorchBlock.class, BaseRailBlock.class, DiodeBlock.class);
        return classes.anyMatch(c -> c.isInstance(state.getBlock()));
    }

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
