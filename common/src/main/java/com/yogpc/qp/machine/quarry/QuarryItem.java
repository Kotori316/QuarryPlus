package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpBlockItem;

public abstract class QuarryItem extends QpBlockItem {
    protected QuarryItem(QpBlock block) {
        super(block, new Properties().fireResistant());
    }
}
