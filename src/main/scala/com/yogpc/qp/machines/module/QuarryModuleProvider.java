package com.yogpc.qp.machines.module;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface QuarryModuleProvider {

    interface Block extends QuarryModuleProvider {
        QuarryModule getModule(@Nonnull Level level, BlockPos pos, BlockState state);

        static Set<QuarryModule> getModulesInWorld(@Nonnull Level level, @Nonnull BlockPos pos) {
            return Arrays.stream(Direction.values())
                .map(pos::relative)
                .mapMulti(findModule(level))
                .collect(Collectors.toSet());
        }

        private static BiConsumer<BlockPos, Consumer<QuarryModule>> findModule(@Nonnull Level level) {
            return (pos, consumer) -> {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof QuarryModuleProvider.Block block) {
                    consumer.accept(block.getModule(level, pos, state));
                }
            };
        }
    }

    interface Item extends QuarryModuleProvider {
        QuarryModule getModule(@Nonnull ItemStack stack);
    }
}
