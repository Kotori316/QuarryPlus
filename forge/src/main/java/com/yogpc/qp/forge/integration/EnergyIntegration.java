package com.yogpc.qp.forge.integration;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EnergyIntegration {
    public static final ResourceLocation POWER_ENTITY_STORAGE = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "power_entity_storage");

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof PowerEntity e) {
            event.addCapability(POWER_ENTITY_STORAGE, new PowerEntityStorage(e));
        }
    }

    record PowerEntityStorage(PowerEntity entity) implements IEnergyStorage, ICapabilityProvider {
        static int clamp(long value) {
            return Math.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            var received = entity.addEnergy(maxReceive * PowerEntity.ONE_FE, simulate);
            return clamp(received / PowerEntity.ONE_FE);
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

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> this));
        }
    }
}
