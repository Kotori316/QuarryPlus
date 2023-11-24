package com.yogpc.qp;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;

public class QuarryPlusTest {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    public static synchronized void init() {
        if (!INITIALIZED.getAndSet(true)) {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            QuarryPlus.config = new QuarryConfig();
        }
    }
}