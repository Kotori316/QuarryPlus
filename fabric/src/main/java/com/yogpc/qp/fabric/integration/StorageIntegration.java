package com.yogpc.qp.fabric.integration;

import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.fabric.machine.MachineStorageFabric;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

public final class StorageIntegration {
    public static void register() {
        MachineStorageRegister.register();
    }
}

class MachineStorageRegister {
    static void register() {
        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) ->
                MachineStorageHolder.getHolder(blockEntity)
                    .map(h -> new MachineStorageFabric.ItemStorageImpl<>(h, blockEntity))
                    .orElse(null),
            PlatformAccessFabric.RegisterObjectsFabric.entityTypes()
        );
        FluidStorage.SIDED.registerForBlockEntities((blockEntity, context) ->
                MachineStorageHolder.getHolder(blockEntity)
                    .map(h -> new MachineStorageFabric.FluidStorageImpl<>(h, blockEntity))
                    .orElse(null),
            PlatformAccessFabric.RegisterObjectsFabric.entityTypes()
        );
    }
}
