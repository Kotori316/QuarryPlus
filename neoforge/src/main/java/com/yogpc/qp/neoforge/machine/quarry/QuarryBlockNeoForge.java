package com.yogpc.qp.neoforge.machine.quarry;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.quarry.QuarryBlock;

public final class QuarryBlockNeoForge extends QuarryBlock {
    public QuarryBlockNeoForge() {
        super(QuarryItemNeoForge::new);
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new QuarryBlockNeoForge();
    }
}
