package com.yogpc.qp;

import com.yogpc.qp.render.RenderAdvQuarry;
import com.yogpc.qp.render.RenderLaser;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.TileQuarry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
    @Override
    public EntityPlayer getPacketPlayer(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).playerEntity;
        return Minecraft.getMinecraft().player;
    }

    @Override
    public World getPacketWorld(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).playerEntity.getEntityWorld();
        return getClientWorld();
    }

    @Override
    public void removeEntity(final Entity e) {
        e.world.removeEntity(e);
        if (e.world.isRemote)
            ((WorldClient) e.world).removeEntityFromWorld(e.getEntityId());
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public void registerTextures() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, RenderQuarry.instance());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMarker.class, RenderMarker.instance());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, RenderLaser.instance());
        ClientRegistry.bindTileEntitySpecialRenderer(TileAdvQuarry.class, RenderAdvQuarry.instance());
//        ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderRefinery.INSTANCE);
    }
}
