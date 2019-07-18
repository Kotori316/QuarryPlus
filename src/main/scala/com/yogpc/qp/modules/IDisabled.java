package com.yogpc.qp.modules;

import com.yogpc.qp.Config;

interface IDisabled {
    scala.Symbol getSymbol();

    default boolean enabled() {
        return Config.content().disableMapJ().getOrDefault(getSymbol(), true);
    }

    default boolean defaultDisableMachine() {
        return false;
    }
}
