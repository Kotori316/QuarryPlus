package com.yogpc.qp.neoforge.integration;

import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class EnergyIntegration {
    @SubscribeEvent
    public static void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PlatformAccessNeoForge.RegisterObjectsNeoForge.QUARRY_ENTITY_TYPE.get(), PowerEntityStorage::new);
    }

    record PowerEntityStorage(PowerEntity entity) implements IEnergyStorage {
        PowerEntityStorage(PowerEntity entity, Direction ignored) {
            this(entity);
        }

        static int clamp(long value) {
            return Math.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            var received = entity.addEnergy(maxReceive * PowerEntity.ONE_FE, simulate);
            return clamp(received);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // not extractable
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return clamp(entity.getEnergy() / PowerEntity.ONE_FE);
        }

        @Override
        public int getMaxEnergyStored() {
            return clamp(entity.getMaxEnergy() / PowerEntity.ONE_FE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
