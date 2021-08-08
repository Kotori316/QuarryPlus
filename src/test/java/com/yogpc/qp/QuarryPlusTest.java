package com.yogpc.qp;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;

public class QuarryPlusTest {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    public static void init() {
        if (!INITIALIZED.getAndSet(true)) {
            SharedConstants.createGameVersion();
            Bootstrap.initialize();
        }
    }
}