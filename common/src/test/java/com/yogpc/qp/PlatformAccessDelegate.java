package com.yogpc.qp;

import com.yogpc.qp.config.QuarryConfig;
import net.minecraft.world.item.ItemStack;

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
    public QuarryConfig quarryConfig() {
        return access.quarryConfig();
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
        public QuarryConfig quarryConfig() {
            return null;
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
