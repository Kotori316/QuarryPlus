package com.yogpc.qp.utils;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ProxyCommon extends ProxyProvider.AbstractProxy {
    @Override
    public Optional<PlayerEntity> getPacketPlayer(final NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender());
    }

    @Override
    public Optional<World> getPacketWorld(NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender()).map(Entity::getEntityWorld);
    }

    @Override
    public void removeEntity(final Entity e) {
        e.remove();
    }

    @Override
    public World getClientWorld() {
        return null;
    }

    @Override
    public void setDummyTexture(String textureName) {
    }

    public static int toInt(long l) {
        return ProxyProvider.AbstractProxy.toInt(l);
    }
}
