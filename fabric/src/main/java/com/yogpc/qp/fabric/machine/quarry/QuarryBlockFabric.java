package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class QuarryBlockFabric extends QuarryBlock {
    public QuarryBlockFabric() {
        super(QuarryItemFabric::new);
    }

    @Override
    protected QuarryBlockFabric createBlock(Properties properties) {
        return new QuarryBlockFabric();
    }

    @Override
    protected void openGui(ServerPlayer player, Level level, BlockPos pos, QuarryEntity quarry) {

    }
}
