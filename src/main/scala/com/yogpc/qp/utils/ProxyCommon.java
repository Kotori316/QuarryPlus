package com.yogpc.qp.utils;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

public class ProxyCommon {
    public Optional<PlayerEntity> getPacketPlayer(final NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender());
    }

    public Optional<World> getPacketWorld(NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender()).map(Entity::getEntityWorld);
    }

    public void registerEvents(IEventBus forgeBus) {
    }

    public void registerModBus(IEventBus modBus) {
    }

    public void removeEntity(final Entity e) {
        e.remove();
    }

    public World getClientWorld() {
        return null;
    }

    public void registerTextures(FMLCommonSetupEvent event) {
    }

    public void setDummyTexture(String textureName) {
    }
}
