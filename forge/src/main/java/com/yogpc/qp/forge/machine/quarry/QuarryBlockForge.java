package com.yogpc.qp.forge.machine.quarry;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.quarry.QuarryBlock;

public final class QuarryBlockForge extends QuarryBlock {
    public QuarryBlockForge() {
        super(QuarryItemForge::new);
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new QuarryBlockForge();
    }
}
