package com.yogpc.qp.utils;

import java.util.Objects;
import java.util.Optional;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.advpump.GuiAdvPump;
import com.yogpc.qp.machines.advquarry.GuiAdvQuarry;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.base.StatusGui;
import com.yogpc.qp.machines.bookmover.GuiBookMover;
import com.yogpc.qp.machines.item.GuiEnchList;
import com.yogpc.qp.machines.item.GuiListTemplate;
import com.yogpc.qp.machines.item.GuiQuarryLevel;
import com.yogpc.qp.machines.marker.TileMarker;
import com.yogpc.qp.machines.mover.GuiMover;
import com.yogpc.qp.machines.quarry.GuiQuarryModule;
import com.yogpc.qp.machines.quarry.GuiSolidQuarry;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.machines.quarry.TileQuarry2;
import com.yogpc.qp.machines.workbench.GuiWorkbench;
import com.yogpc.qp.render.DummyBlockBakedModel;
import com.yogpc.qp.render.RenderAdvQuarry;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.RenderQuarry2;
import com.yogpc.qp.render.Sprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class ProxyClient extends ProxyCommon {

    @Override
    public Optional<PlayerEntity> getPacketPlayer(final NetworkEvent.Context context) {
        if (context.getSender() != null) {
            return Optional.of(context.getSender());
        } else {
            return Optional.ofNullable(Minecraft.getInstance().player);
        }
    }

    @Override
    public Optional<World> getPacketWorld(NetworkEvent.Context context) {
        ServerPlayerEntity sender = context.getSender();
        if (sender == null) {
            return Optional.of(getClientWorld());
        } else {
            return Optional.of(sender).map(Entity::getEntityWorld);
        }
    }

    @Override
    public void removeEntity(final Entity e) {
        e.remove();
        if (e.world.isRemote)
            ((ClientWorld) e.world).removeEntityFromWorld(e.getEntityId());
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID, value = Dist.CLIENT)
    static class ClientModEventHandlers {

        @SubscribeEvent
        public static void registerTextures(FMLCommonSetupEvent event) {
            // Register TileEntity Special Render
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
            // Register GUI
            ScreenManager.registerFactory(Holder.moverContainerType(), GuiMover::new);
            ScreenManager.registerFactory(Holder.workbenchContainerType(), GuiWorkbench::new);
            ScreenManager.registerFactory(Holder.bookMoverContainerType(), GuiBookMover::new);
            ScreenManager.registerFactory(Holder.ySetterContainerType(), GuiQuarryLevel::new);
            ScreenManager.registerFactory(Holder.solidQuarryContainerType(), GuiSolidQuarry::new);
            ScreenManager.registerFactory(Holder.quarryModuleContainerType(), GuiQuarryModule::new);
            ScreenManager.registerFactory(Holder.enchListContainerType(), GuiEnchList::new);
            ScreenManager.registerFactory(Holder.templateContainerType(), GuiListTemplate::new);
            ScreenManager.registerFactory(Holder.advPumpContainerType(), GuiAdvPump::new);
            ScreenManager.registerFactory(Holder.advQuarryContainerType(), GuiAdvQuarry::new);
            ScreenManager.registerFactory(Holder.statusContainerType(), StatusGui::new);
        }

        @SubscribeEvent
        public static void onBake(ModelBakeEvent event) {
            String itemTexName = "minecraft:glass";

            IBakedModel blockModel = getModel(event.getModelManager(), new ModelResourceLocation(itemTexName));
            IBakedModel itemModel = getModel(event.getModelManager(), new ModelResourceLocation(itemTexName, "inventory"));
            DummyBlockBakedModel dummyBlockBakedModel = new DummyBlockBakedModel(blockModel);
            DummyBlockBakedModel dummyItemBakedModel = new DummyBlockBakedModel(itemModel);

            ResourceLocation pathIn = Objects.requireNonNull(Holder.blockDummy().getRegistryName());
            event.getModelRegistry().put(new ModelResourceLocation(pathIn.toString()), dummyBlockBakedModel);
            event.getModelRegistry().put(new ModelResourceLocation(pathIn, "inventory"), dummyItemBakedModel);
        }

        private static IBakedModel getModel(ModelManager manager, ModelResourceLocation location) {
//        IBakedModel model = manager.getModel(location);
//        if (model == manager.getMissingModel()) {
            return manager.getModel(new ModelResourceLocation("minecraft:glass", location.getVariant()));
//        } else {
//            return model;
//        }
        }

        @SubscribeEvent
        public static void registerTexture(TextureStitchEvent.Pre event) {
            Sprites.registerTexture(event);
        }

        @SubscribeEvent
        public static void putTexture(TextureStitchEvent.Post event) {
            Sprites.putTexture(event);
        }
    }

}
