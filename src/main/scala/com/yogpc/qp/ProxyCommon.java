package com.yogpc.qp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

public class ProxyCommon {
    public EntityPlayer getPacketPlayer(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).playerEntity;
        return null;
    }

    public World getPacketWorld(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).playerEntity.getEntityWorld();
        return null;
    }

    public void removeEntity(final Entity e) {
        e.world.removeEntity(e);
    }

    public World getClientWorld() {
        return null;
    }

    public void registerTextures() {
    }
}
