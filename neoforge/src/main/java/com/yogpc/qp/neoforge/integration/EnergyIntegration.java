package com.yogpc.qp.neoforge.integration;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public final class EnergyIntegration {
    @SubscribeEvent
    public static void attachCapabilities(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<?> blockEntityType : PlatformAccess.getAccess().registerObjects().getBlockEntityTypes()) {
            event.registerBlockEntity(Capabilities.Energy.BLOCK, blockEntityType, EnergyIntegration::provider);
        }
    }

    @Nullable
    private static EnergyHandler provider(Object entity, Direction direction) {
        if (entity instanceof PowerEntity powerEntity) {
            return new PowerEntityStorage(powerEntity, direction);
        }
        return null;
    }

    private static final class PowerEntityStorage extends SnapshotJournal<Long> implements EnergyHandler {
        private final PowerEntity entity;

        PowerEntityStorage(PowerEntity entity, Direction ignored) {
            this.entity = entity;
        }

        static int clamp(long value) {
            return Math.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            updateSnapshots(transaction);
            var received = entity.addEnergy(amount * PowerEntity.ONE_FE, true);
            return clamp(received / PowerEntity.ONE_FE);
        }

        @Override
        public long getAmountAsLong() {
            return entity.getEnergy() / PowerEntity.ONE_FE;
        }

        @Override
        public long getCapacityAsLong() {
            return entity.getMaxEnergy() / PowerEntity.ONE_FE;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected Long createSnapshot() {
            return this.entity.getEnergy();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            this.entity.setEnergy(snapshot, false);
        }
    }
}
