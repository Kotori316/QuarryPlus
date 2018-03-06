package com.yogpc.qp;

import com.yogpc.qp.gui.GuiP_List;
import com.yogpc.qp.render.RenderAdvQuarry;
import com.yogpc.qp.render.RenderDistiller;
import com.yogpc.qp.render.RenderLaser;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.Sprites;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.TilePump;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.tile.TileRefinery;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
    @Override
    public void openPumpGui(World worldIn, EntityPlayer playerIn, EnumFacing facing, TilePump pump) {
        if (!worldIn.isRemote) {
            pump.S_OpenGUI(facing, playerIn);
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiP_List(facing.ordinal(), pump));
        }
    }

    @Override
    public EntityPlayer getPacketPlayer(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).player;
        return Minecraft.getMinecraft().player;
    }

    @Override
    public World getPacketWorld(final INetHandler inh) {
        if (inh instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) inh).player.getEntityWorld();
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
        ClientRegistry.bindTileEntitySpecialRenderer(TileAdvQuarry.class, RenderAdvQuarry.instance());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, RenderLaser.instance());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderDistiller.instance());
        MinecraftForge.EVENT_BUS.register(Sprites.instance());
    }

    @Override
    public ModelResourceLocation fromEntry(IForgeRegistryEntry<?> entry) {
        return new ModelResourceLocation(VersionUtil.getRegistryName(entry), "inventory");
    }
}
