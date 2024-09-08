package com.yogpc.qp.fabric.machine;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MachineStorageFabricTest {
    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    @Test
    void instance() {
        var storage = MachineStorage.of();
        assertInstanceOf(MachineStorageFabric.class, storage);
    }

    @Nested
    class ItemTest {
        @Test
        void addItemSimulate() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.ItemStorageImpl<>(ACCESSOR, holder);

            assertTrue(s.supportsInsertion());
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(ItemVariant.of(Items.APPLE), 4, transaction);
                assertEquals(4, inserted);
                assertEquals(4, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
            }

            assertEquals(0, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
        }

        @Test
        void addItemExecute() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.ItemStorageImpl<>(ACCESSOR, holder);

            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(ItemVariant.of(Items.APPLE), 4, transaction);
                assertEquals(4, inserted);
                assertEquals(4, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
                transaction.commit();
            }

            assertEquals(4, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
        }

        @Test
        void addMoreItemSimulate() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.ItemStorageImpl<>(ACCESSOR, holder);
            storage.addItem(new ItemStack(Items.APPLE, 5));

            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(ItemVariant.of(Items.APPLE), 4, transaction);
                assertEquals(4, inserted);
                assertEquals(9, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
            }

            assertEquals(5, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
        }

        @Test
        void addMoreItemExecute() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.ItemStorageImpl<>(ACCESSOR, holder);
            storage.addItem(new ItemStack(Items.APPLE, 5));

            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(ItemVariant.of(Items.APPLE), 4, transaction);
                assertEquals(4, inserted);
                assertEquals(9, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
                transaction.commit();
            }

            assertEquals(9, storage.getItemCount(Items.APPLE, DataComponentPatch.EMPTY));
        }
    }

    @Nested
    class FluidTest {
        @Test
        void addFluidSimulate() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.FluidStorageImpl<>(ACCESSOR, holder);

            assertTrue(s.supportsInsertion());
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
                assertEquals(FluidConstants.BUCKET, inserted);
                assertEquals(FluidConstants.BUCKET, storage.getFluidCount(Fluids.WATER));
            }

            assertEquals(0, storage.getFluidCount(Fluids.WATER));
        }

        @Test
        void addFluidExecute() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.FluidStorageImpl<>(ACCESSOR, holder);

            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
                assertEquals(FluidConstants.BUCKET, inserted);
                assertEquals(FluidConstants.BUCKET, storage.getFluidCount(Fluids.WATER));
                transaction.commit();
            }
            assertEquals(FluidConstants.BUCKET, storage.getFluidCount(Fluids.WATER));
        }

        @Test
        void addMoreFluidSimulate() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.FluidStorageImpl<>(ACCESSOR, holder);
            storage.addFluid(Fluids.WATER, FluidConstants.BUCKET * 4);

            assertTrue(s.supportsInsertion());
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
                assertEquals(FluidConstants.BUCKET, inserted);
                assertEquals(FluidConstants.BUCKET * 5, storage.getFluidCount(Fluids.WATER));
            }

            assertEquals(FluidConstants.BUCKET * 4, storage.getFluidCount(Fluids.WATER));
        }

        @Test
        void addMoreFluidExecute() {
            var storage = new MachineStorageFabric();
            var holder = new MachineStorageHolder.Constant(storage);
            var s = new MachineStorageFabric.FluidStorageImpl<>(ACCESSOR, holder);
            storage.addFluid(Fluids.WATER, FluidConstants.BUCKET * 4);

            assertTrue(s.supportsInsertion());
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = s.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
                assertEquals(FluidConstants.BUCKET, inserted);
                assertEquals(FluidConstants.BUCKET * 5, storage.getFluidCount(Fluids.WATER));
                transaction.commit();
            }

            assertEquals(FluidConstants.BUCKET * 5, storage.getFluidCount(Fluids.WATER));
        }
    }
}
