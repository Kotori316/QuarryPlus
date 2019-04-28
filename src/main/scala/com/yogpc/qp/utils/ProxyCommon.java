package com.yogpc.qp.utils;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ProxyCommon {
    public EntityPlayer getPacketPlayer(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).player;
        return null;
    }

    public World getPacketWorld(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).player.getEntityWorld();
        return null;
    }

    public void registerEvents(IEventBus bus) {
    }

    public void removeEntity(final Entity e) {
        e.world.removeEntity(e);
    }

    public World getClientWorld() {
        return null;
    }

    public void registerTextures(FMLCommonSetupEvent event) {
    }

    public ModelResourceLocation fromEntry(IForgeRegistryEntry<?> entry) {
        return null;
    }

    public void setDummyTexture(String textureName) {
    }
}
