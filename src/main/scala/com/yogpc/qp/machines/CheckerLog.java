package com.yogpc.qp.machines;

import java.util.List;

import net.minecraft.network.chat.Component;

public interface CheckerLog {
    List<? extends Component> getDebugLogs();
}
