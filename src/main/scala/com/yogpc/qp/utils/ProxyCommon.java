package com.yogpc.qp.utils;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

public class ProxyCommon {
    public Optional<EntityPlayer> getPacketPlayer(final NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender());
    }

    public Optional<World> getPacketWorld(NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender()).map(Entity::getEntityWorld);
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

    public void setDummyTexture(String textureName) {
    }
}
