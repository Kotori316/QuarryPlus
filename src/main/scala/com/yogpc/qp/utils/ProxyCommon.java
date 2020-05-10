package com.yogpc.qp.utils;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ProxyCommon {
    public Optional<PlayerEntity> getPacketPlayer(final NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender());
    }

    public Optional<World> getPacketWorld(NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender()).map(Entity::getEntityWorld);
    }

    public void removeEntity(final Entity e) {
        e.remove();
    }

    public World getClientWorld() {
        return null;
    }

    public void setDummyTexture(String textureName) {
    }

    public static int toInt(long l) {
        if (l > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        else if (l < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        else return (int) l;
    }
}
