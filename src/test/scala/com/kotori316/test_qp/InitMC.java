package com.kotori316.test_qp;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class InitMC {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void beforeAll() {
        init();
    }

    static synchronized void init() {
        if (!INITIALIZED.getAndSet(true)) {
            changeDist();
            assertEquals(Dist.CLIENT, FMLEnvironment.dist);
            Bootstrap.register();
        }
    }

    private static void changeDist() {
        try {
            Field dist = FMLLoader.class.getDeclaredField("dist");
            dist.setAccessible(true);
            dist.set(null, Dist.CLIENT);
        } catch (Exception e) {
            fail(e);
        }
    }
}
