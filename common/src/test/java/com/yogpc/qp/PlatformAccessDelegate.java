package com.yogpc.qp;

import com.yogpc.qp.machine.PowerMap;
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
            return new QuarryConfig() {
                @Override
                public boolean isDebug() {
                    return false;
                }

                @Override
                public PowerMap getPowerMap() {
                    return new PowerMap() {
                        @Override
                        public Quarry quarry() {
                            return Default.QUARRY;
                        }
                    };
                }
            };
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
