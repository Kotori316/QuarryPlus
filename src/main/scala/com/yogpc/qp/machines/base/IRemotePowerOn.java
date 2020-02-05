package com.yogpc.qp.machines.base;

import java.util.Objects;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface IRemotePowerOn {

    void setArea(Area area);

    default Area getArea() {
        return Area.zeroArea();
    }

    void startWorking();

    void startWaiting();

    default void setAndStart(Area area) {
        setArea(area);
        startWorking();
    }

    final class Cap implements Capability.IStorage<IRemotePowerOn>, Callable<IRemotePowerOn> {

        // Capability
        @CapabilityInject(IRemotePowerOn.class)
        public static final Capability<IRemotePowerOn> CAPABILITY = null;

        @Override
        public IRemotePowerOn call() {
            return new RemotePowerOnImpl();
        }

        @Override
        public INBT writeNBT(Capability<IRemotePowerOn> capability, IRemotePowerOn instance, Direction side) {
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<IRemotePowerOn> capability, IRemotePowerOn instance, Direction side, INBT nbt) {
        }

        public static Capability<IRemotePowerOn> REMOTE_CAPABILITY() {
            return CAPABILITY;
        }

        public static void register() {
            Cap cap = new Cap();
            CapabilityManager.INSTANCE.register(IRemotePowerOn.class, cap, cap);
        }
    }

    class RemotePowerOnImpl implements IRemotePowerOn {
        private static final Logger LOGGER = LogManager.getLogger(RemotePowerOnImpl.class);
        @Nonnull
        private Area area = Area.zeroArea();

        @Override
        public void setArea(@Nonnull Area area) {
            this.area = area;
        }

        @Nonnull
        @Override
        public Area getArea() {
            return area;
        }

        @Override
        public void startWorking() {
            LOGGER.info("Dummy RemotePowerOnImpl instance {} has started working.", this);
        }

        @Override
        public void startWaiting() {
            LOGGER.info("Dummy RemotePowerOnImpl instance {} has changed its mode to waiting.", this);
        }

        @Override
        public String toString() {
            return "RemotePowerOnImpl{" + "area=" + area + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RemotePowerOnImpl that = (RemotePowerOnImpl) o;
            return area.equals(that.area);
        }

        @Override
        public int hashCode() {
            return Objects.hash(area);
        }
    }
}
