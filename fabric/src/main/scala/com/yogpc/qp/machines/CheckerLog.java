package com.yogpc.qp.machines;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface CheckerLog {
    List<? extends Component> getDebugLogs();
}
