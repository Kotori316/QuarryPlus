package com.yogpc.qp;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

public class ProxyCommon {
    private final Map<EntityPlayer, Integer> keys = new WeakHashMap<>();

    public boolean getKey(final EntityPlayer p, final Key k) {
        return this.keys.containsKey(p) && (this.keys.get(p) & 1 << k.ordinal()) != 0;
    }

    public int keysToInt(final EntityPlayer p) {
        return this.keys.getOrDefault(p, 0);
    }

    public void setKeys(final EntityPlayer p, final int r) {
        this.keys.put(p, r);
    }

    public EntityPlayer getPacketPlayer(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).playerEntity;
        return null;
    }

    //No used?
    public int addNewArmourRendererPrefix(final String s) {
        return 0;
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
