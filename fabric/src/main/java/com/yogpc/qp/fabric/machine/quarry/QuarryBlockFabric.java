package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.machine.quarry.QuarryBlock;

public final class QuarryBlockFabric extends QuarryBlock {
    public QuarryBlockFabric() {
        super(QuarryItemFabric::new);
    }

    @Override
    protected QuarryBlockFabric createBlock(Properties properties) {
        return new QuarryBlockFabric();
    }

}
