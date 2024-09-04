package com.yogpc.qp.forge.integration;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class StorageIntegration {
    public static final ResourceLocation MACHINE_STORAGE = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "machine_storage");

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        MachineStorageHolder.getHolder(event.getObject())
            .ifPresent(h -> event.addCapability(MACHINE_STORAGE, new MachineStorageHandler<>(h, event.getObject())));
    }
}
