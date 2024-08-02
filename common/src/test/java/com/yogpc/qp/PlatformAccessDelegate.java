package com.yogpc.qp;

import com.yogpc.qp.config.QuarryConfig;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.function.Supplier;

public class PlatformAccessDelegate implements PlatformAccess {
    private PlatformAccess access;
    private Path configPath;

    public PlatformAccessDelegate() {
        this.access = new VanillaImpl();
    }

    public void reset() {
        this.access = new VanillaImpl();
    }

    public void setAccess(PlatformAccess access) {
        this.access = access;
    }

    public void setConfigPath(Path configPath) {
        this.configPath = configPath;
    }

    @Override
    public int priority() {
        return -1;
    }

    @Override
    public String platformName() {
        return "test";
    }

    @Override
    public RegisterObjects registerObjects() {
        return access.registerObjects();
    }

    @Override
    public Packet packetHandler() {
        return access.packetHandler();
    }

    @Override
    public Path configPath() {
        if (configPath == null) {
            return access.configPath();
        }
        return configPath;
    }

    @Override
    public Supplier<? extends QuarryConfig> getConfig() {
        return () -> QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment);
    }

    @Override
    public boolean isInDevelopmentEnvironment() {
        return access.isInDevelopmentEnvironment();
    }

    @Override
    public Transfer transfer() {
        return access.transfer();
    }

    @Override
    public FluidStackLike getFluidInItem(ItemStack stack) {
        return access.getFluidInItem(stack);
    }

    static final class VanillaImpl implements PlatformAccess {

        @Override
        public String platformName() {
            return "vanilla";
        }

        @Override
        public RegisterObjects registerObjects() {
            return null;
        }

        @Override
        public Packet packetHandler() {
            return null;
        }

        @Override
        public Path configPath() {
            return Path.of("logs", "config", "quarry_test.toml");
        }

        @Override
        public Supplier<? extends QuarryConfig> getConfig() {
            return () -> QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment);
        }

        @Override
        public boolean isInDevelopmentEnvironment() {
            return false;
        }

        @Override
        public Transfer transfer() {
            return null;
        }

        @Override
        public FluidStackLike getFluidInItem(ItemStack stack) {
            return null;
        }
    }
}
