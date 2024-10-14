package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpBlockItem;

public class AdvQuarryItem extends QpBlockItem {
    public AdvQuarryItem(QpBlock block) {
        super(block, new Properties().fireResistant());
    }
}
