package com.yogpc.qp;

import com.yogpc.qp.gui.GuiAdvQuarryFluid;
import com.yogpc.qp.gui.GuiP_List;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.advquarry.AdvContentMessage;
import com.yogpc.qp.packet.advquarry.AdvFilterMessage;
import com.yogpc.qp.render.DummyBlockBakedModel;
import com.yogpc.qp.render.RenderAdvQuarry;
import com.yogpc.qp.render.RenderDistiller;
import com.yogpc.qp.render.RenderFiller;
import com.yogpc.qp.render.RenderLaser;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.RenderQuarry2;
import com.yogpc.qp.render.Sprites;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.tile.TileFiller;
import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.TilePump;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.tile.TileQuarry2;
import com.yogpc.qp.tile.TileRefinery;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {

    private DummyBlockBakedModel dummyBlockBakedModel;
    private DummyBlockBakedModel dummyItemBakedModel;

    @Override
    public void openPumpGui(World worldIn, EntityPlayer playerIn, EnumFacing facing, TilePump pump) {
        if (!worldIn.isRemote) {
            pump.S_OpenGUI(facing, playerIn);
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiP_List(facing.ordinal(), pump));
        }
    }

    @Override
    public void openAdvQuarryPumpGui(World worldIn, EntityPlayer player, TileAdvQuarry quarry, EnumFacing facing) {
        if (!worldIn.isRemote) {
            PacketHandler.sendToClient(AdvContentMessage.create(quarry), (EntityPlayerMP) player);
            PacketHandler.sendToClient(AdvFilterMessage.create(quarry), (EntityPlayerMP) player);
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiAdvQuarryFluid(quarry, player, facing));
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
        if (!Config.content().disableRendering()) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, RenderQuarry.instance());
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry2.class, RenderQuarry2.instance());
            ClientRegistry.bindTileEntitySpecialRenderer(TileMarker.class, RenderMarker.instance());
            if (!Config.content().disableMapJ().get(TileAdvQuarry.SYMBOL()))
                ClientRegistry.bindTileEntitySpecialRenderer(TileAdvQuarry.class, RenderAdvQuarry.instance());
            if (!Config.content().disableMapJ().get(TileLaser.SYMBOL))
                ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, RenderLaser.instance());
            if (!Config.content().disableMapJ().get(TileFiller.SYMBOL()))
                ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, RenderFiller.instance());
            ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderDistiller.instance());
            MinecraftForge.EVENT_BUS.register(Sprites.instance());
        }
    }

    @Override
    public ModelResourceLocation fromEntry(IForgeRegistryEntry<?> entry) {
        return new ModelResourceLocation(VersionUtil.getRegistryName(entry), "inventory");
    }

    @SubscribeEvent
    public void onBake(ModelBakeEvent event) {
        String itemTexName = Config.content().dummyBlockTextureName().split("#")[0];

        IBakedModel blockModel = getModel(event.getModelManager(), new ModelResourceLocation(Config.content().dummyBlockTextureName()));
        IBakedModel itemModel = getModel(event.getModelManager(), new ModelResourceLocation(itemTexName, "inventory"));
        dummyBlockBakedModel = new DummyBlockBakedModel(blockModel);
        dummyItemBakedModel = new DummyBlockBakedModel(itemModel);
        String pathIn = VersionUtil.getRegistryName(QuarryPlusI.dummyBlock()).toString();
        event.getModelRegistry().putObject(new ModelResourceLocation(pathIn), dummyBlockBakedModel);
        event.getModelRegistry().putObject(new ModelResourceLocation(pathIn, "inventory"), dummyItemBakedModel);
    }

    @Override
    public void setDummyTexture(String textureName) {
        /*String itemTexName = Optional.of(textureName)
            .map(ResourceLocation::new)
            .filter(ForgeRegistries.BLOCKS::containsKey)
            .map(ForgeRegistries.BLOCKS::getValue)
            // Null check is inside of Optional.
            .map(Block::getRegistryName)
            .map(ResourceLocation::toString)
            .orElse("minecraft:glass");*/
        String itemTexName = textureName.split("#")[0];

        ModelManager manager = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
        dummyBlockBakedModel.model = getModel(manager, new ModelResourceLocation(textureName));
        dummyItemBakedModel.model = getModel(manager, new ModelResourceLocation(itemTexName, "inventory"));
    }

    private IBakedModel getModel(ModelManager manager, ModelResourceLocation location) {
//        IBakedModel model = manager.getModel(location);
//        if (model == manager.getMissingModel()) {
        return manager.getModel(new ModelResourceLocation("minecraft:glass", location.getVariant()));
//        } else {
//            return model;
//        }
    }
}
