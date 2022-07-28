package com.yogpc.qp;

import com.yogpc.qp.machines.advquarry.AdvQuarryScreen;
import com.yogpc.qp.machines.bookmover.BookMoverScreen;
import com.yogpc.qp.machines.filler.FillerScreen;
import com.yogpc.qp.machines.marker.Screen16Marker;
import com.yogpc.qp.machines.marker.ScreenFlexMarker;
import com.yogpc.qp.machines.mini_quarry.MiniQuarryScreen;
import com.yogpc.qp.machines.misc.CreativeGeneratorScreen;
import com.yogpc.qp.machines.misc.YSetterScreen;
import com.yogpc.qp.machines.module.ScreenQuarryModule;
import com.yogpc.qp.machines.mover.MoverScreen;
import com.yogpc.qp.machines.placer.PlacerScreen;
import com.yogpc.qp.machines.placer.RemotePlacerScreen;
import com.yogpc.qp.machines.quarry.SFQuarryScreen;
import com.yogpc.qp.machines.workbench.ScreenWorkbench;
import com.yogpc.qp.render.Render16Marker;
import com.yogpc.qp.render.RenderAdvQuarry;
import com.yogpc.qp.render.RenderFlexMarker;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.RenderSFQuarry;
import com.yogpc.qp.render.Sprites;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// @Mod.EventBusSubscriber(modid = QuarryPlus.modID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuarryPlusClient {

    static void registerClientBus() {
        FMLJavaModLoadingContext.get().getModEventBus().register(QuarryPlusClient.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(Sprites.class);
    }

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Holder.BLOCK_DUMMY, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(Holder.BLOCK_DUMMY_REPLACER, RenderType.translucent());

        BlockEntityRenderers.register(Holder.QUARRY_TYPE, RenderQuarry::new);
        BlockEntityRenderers.register(Holder.MARKER_TYPE, RenderMarker.constructor(QuarryPlus.config.common.reduceMarkerGuideLineIfPlayerIsFar.get()));
        BlockEntityRenderers.register(Holder.FLEX_MARKER_TYPE, RenderFlexMarker::new);
        BlockEntityRenderers.register(Holder.MARKER_16_TYPE, Render16Marker::new);
        BlockEntityRenderers.register(Holder.ADV_QUARRY_TYPE, RenderAdvQuarry::new);
        BlockEntityRenderers.register(Holder.SOLID_FUEL_QUARRY_TYPE, RenderSFQuarry::new);

        MenuScreens.register(Holder.FLEX_MARKER_MENU_TYPE, ScreenFlexMarker::new);
        MenuScreens.register(Holder.MARKER_16_MENU_TYPE, Screen16Marker::new);
        MenuScreens.register(Holder.Y_SETTER_MENU_TYPE, YSetterScreen::new);
        MenuScreens.register(Holder.WORKBENCH_MENU_TYPE, ScreenWorkbench::new);
        MenuScreens.register(Holder.MOVER_MENU_TYPE, MoverScreen::new);
        MenuScreens.register(Holder.MODULE_MENU_TYPE, ScreenQuarryModule::new);
        MenuScreens.register(Holder.PLACER_MENU_TYPE, PlacerScreen::new);
        MenuScreens.register(Holder.REMOTE_PLACER_MENU_TYPE, RemotePlacerScreen::new);
        MenuScreens.register(Holder.BOOK_MOVER_MENU_TYPE, BookMoverScreen::new);
        MenuScreens.register(Holder.CREATIVE_GENERATOR_MENU_TYPE, CreativeGeneratorScreen::new);
        MenuScreens.register(Holder.ADV_QUARRY_MENU_TYPE, AdvQuarryScreen::new);
        MenuScreens.register(Holder.MINI_QUARRY_MENU_TYPE, MiniQuarryScreen::new);
        MenuScreens.register(Holder.SOLID_FUEL_QUARRY_MENU_TYPE, SFQuarryScreen::new);
        MenuScreens.register(Holder.FILLER_MENU_TYPE, FillerScreen::new);
    }
}
