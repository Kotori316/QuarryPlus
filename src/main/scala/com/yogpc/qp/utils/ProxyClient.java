package com.yogpc.qp.utils;

import java.util.Objects;
import java.util.Optional;

import com.yogpc.qp.Config;
import com.yogpc.qp.machines.GuiHandler;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.marker.TileMarker;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.machines.quarry.TileQuarry2;
import com.yogpc.qp.render.DummyBlockBakedModel;
import com.yogpc.qp.render.RenderAdvQuarry;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.RenderQuarry2;
import com.yogpc.qp.render.Sprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class ProxyClient extends ProxyCommon {
    private DummyBlockBakedModel dummyBlockBakedModel;
    private DummyBlockBakedModel dummyItemBakedModel;

    @Override
    public Optional<EntityPlayer> getPacketPlayer(final NetworkEvent.Context context) {
        if (context.getSender() != null) {
            return Optional.of(context.getSender());
        } else {
            return Optional.ofNullable(Minecraft.getInstance().player);
        }
    }

    @Override
    public Optional<World> getPacketWorld(NetworkEvent.Context context) {
        EntityPlayerMP sender = context.getSender();
        if (sender == null) {
            return Optional.of(getClientWorld());
        } else {
            return Optional.of(sender).map(Entity::getEntityWorld);
        }
    }

    @Override
    public void registerEvents(IEventBus bus) {
        super.registerEvents(bus);
        bus.addListener(Sprites::putTexture);
        bus.addListener(Sprites::registerTexture);
        bus.addListener(this::onBake);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> GuiHandler::getGui);
    }

    @Override
    public void removeEntity(final Entity e) {
        e.world.removeEntity(e);
        if (e.world.isRemote)
            ((WorldClient) e.world).removeEntityFromWorld(e.getEntityId());
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public void registerTextures(FMLCommonSetupEvent event) {
        if (Config.client().enableRender().get()) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileMarker.class, RenderMarker.instance());
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, RenderQuarry.instance());
            ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry2.class, RenderQuarry2.instance());
            ClientRegistry.bindTileEntitySpecialRenderer(TileAdvQuarry.class, RenderAdvQuarry.instance());
        }
//        if (!Config.content().disableRendering()) {
//            if (!Config.content().disableMapJ().get(TileAdvQuarry.SYMBOL()))
//                ClientRegistry.bindTileEntitySpecialRenderer(TileAdvQuarry.class, RenderAdvQuarry.instance());
//            if (!Config.content().disableMapJ().get(TileLaser.SYMBOL))
//                ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, RenderLaser.instance());
//            ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderDistiller.instance());
//        }
    }

    public void onBake(ModelBakeEvent event) {
        String itemTexName = "minecraft:glass";

        IBakedModel blockModel = getModel(event.getModelManager(), new ModelResourceLocation(itemTexName));
        IBakedModel itemModel = getModel(event.getModelManager(), new ModelResourceLocation(itemTexName, "inventory"));
        dummyBlockBakedModel = new DummyBlockBakedModel(blockModel);
        dummyItemBakedModel = new DummyBlockBakedModel(itemModel);

        ResourceLocation pathIn = Objects.requireNonNull(Holder.blockDummy().getRegistryName());
        event.getModelRegistry().put(new ModelResourceLocation(pathIn.toString()), dummyBlockBakedModel);
        event.getModelRegistry().put(new ModelResourceLocation(pathIn, "inventory"), dummyItemBakedModel);
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

        ModelManager manager = Minecraft.getInstance().getItemRenderer().getItemModelMesher().getModelManager();
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
