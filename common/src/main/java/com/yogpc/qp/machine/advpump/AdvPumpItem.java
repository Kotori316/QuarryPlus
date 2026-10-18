package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpBlockItem;

public class AdvPumpItem extends QpBlockItem {
    public AdvPumpItem(QpBlock block) {
        super(block, new Properties().fireResistant());
    }
}
