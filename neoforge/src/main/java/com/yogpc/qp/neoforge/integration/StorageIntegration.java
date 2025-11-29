package com.yogpc.qp.neoforge.integration;

import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import com.yogpc.qp.neoforge.machine.MachineStorageNeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class StorageIntegration {
    @SubscribeEvent
    public static void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, PlatformAccessNeoForge.RegisterObjectsNeoForge.DEBUG_STORAGE_TYPE.get(), (t, ignored) ->
            MachineStorageHolder.getHolder(t).map(h -> MachineStorageNeoForge.createItemHandler(h, t)).orElse(null)
        );
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, PlatformAccessNeoForge.RegisterObjectsNeoForge.DEBUG_STORAGE_TYPE.get(), (t, ignored) ->
            MachineStorageHolder.getHolder(t).map(h -> MachineStorageNeoForge.createFluidHandler(h, t)).orElse(null)
        );
    }
}
