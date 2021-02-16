package com.kotori316.test_qp;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import com.yogpc.qp.QuarryPlus;
import cpw.mods.modlauncher.Launcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class InitMC {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void beforeAll() {
        init();
    }

    static synchronized void init() {
        if (!INITIALIZED.getAndSet(true)) {
            initLoader();
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

    private static void initLoader() {
        try {
            Constructor<Launcher> constructor = Launcher.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            fail(e);
        }
    }

    protected static ResourceLocation id(String s) {
        return new ResourceLocation(QuarryPlus.modID, s.toLowerCase(Locale.ROOT));
    }
}
