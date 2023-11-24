package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ReplacerBlock extends QPBlock implements QuarryModuleProvider.Block {
    public static final String NAME = "replacer";

    public ReplacerBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK).strength(3.0f), NAME);
    }

    @Override
    public ReplacerModule getModule(@NotNull Level level, BlockPos pos, BlockState state) {
        Predicate<BlockState> accept = ReplacerModule.rejects
            .stream().reduce(s -> false, Predicate::or).negate();
        return new ReplacerModule(() -> {
            var up = level.getBlockState(pos.above());
            return accept.test(up) ? up : Holder.BLOCK_DUMMY_REPLACER.defaultBlockState();
        });
    }

}
