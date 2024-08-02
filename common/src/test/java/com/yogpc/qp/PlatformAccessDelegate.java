package com.yogpc.qp;

import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;

public final class PlatformAccessDelegate implements PlatformAccess {
    private PlatformAccess access;

    public PlatformAccessDelegate() {
        this.access = new VanillaImpl();
    }

    public void reset() {
        this.access = new VanillaImpl();
    }

    public void setAccess(PlatformAccess access) {
        this.access = access;
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
        return access.configPath();
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
