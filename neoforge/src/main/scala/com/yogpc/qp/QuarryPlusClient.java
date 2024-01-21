package com.yogpc.qp;

import com.yogpc.qp.machines.advpump.AdvPumpScreen;
import com.yogpc.qp.machines.advquarry.AdvQuarryScreen;
import com.yogpc.qp.machines.bookmover.BookMoverScreen;
import com.yogpc.qp.machines.filler.FillerScreen;
import com.yogpc.qp.machines.marker.Screen16Marker;
import com.yogpc.qp.machines.marker.ScreenFlexMarker;
import com.yogpc.qp.machines.mini_quarry.MiniQuarryScreen;
import com.yogpc.qp.machines.misc.CreativeGeneratorScreen;
import com.yogpc.qp.machines.misc.YSetterScreen;
import com.yogpc.qp.machines.module.FilterModuleScreen;
import com.yogpc.qp.machines.module.ScreenQuarryModule;
import com.yogpc.qp.machines.mover.MoverScreen;
import com.yogpc.qp.machines.placer.PlacerScreen;
import com.yogpc.qp.machines.placer.RemotePlacerScreen;
import com.yogpc.qp.machines.quarry.SFQuarryScreen;
import com.yogpc.qp.machines.workbench.ScreenWorkbench;
import com.yogpc.qp.render.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// @Mod.EventBusSubscriber(modid = QuarryPlus.modID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuarryPlusClient {

    static void registerClientBus(IEventBus modBus) {
        modBus.register(QuarryPlusClient.class);
        modBus.register(Sprites.class);
    }

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(Holder.QUARRY_TYPE, RenderQuarry::new);
        BlockEntityRenderers.register(Holder.MARKER_TYPE, RenderMarker.constructor(QuarryPlus.config.common.reduceMarkerGuideLineIfPlayerIsFar.get()));
        BlockEntityRenderers.register(Holder.FLEX_MARKER_TYPE, RenderFlexMarker::new);
        BlockEntityRenderers.register(Holder.MARKER_16_TYPE, Render16Marker::new);
        BlockEntityRenderers.register(Holder.ADV_QUARRY_TYPE, RenderAdvQuarry::new);
        BlockEntityRenderers.register(Holder.SOLID_FUEL_QUARRY_TYPE, RenderSFQuarry::new);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Holder.FLEX_MARKER_MENU_TYPE, ScreenFlexMarker::new);
        event.register(Holder.MARKER_16_MENU_TYPE, Screen16Marker::new);
        event.register(Holder.Y_SETTER_MENU_TYPE, YSetterScreen::new);
        event.register(Holder.WORKBENCH_MENU_TYPE, ScreenWorkbench::new);
        event.register(Holder.MOVER_MENU_TYPE, MoverScreen::new);
        event.register(Holder.MODULE_MENU_TYPE, ScreenQuarryModule::new);
        event.register(Holder.PLACER_MENU_TYPE, PlacerScreen::new);
        event.register(Holder.REMOTE_PLACER_MENU_TYPE, RemotePlacerScreen::new);
        event.register(Holder.BOOK_MOVER_MENU_TYPE, BookMoverScreen::new);
        event.register(Holder.CREATIVE_GENERATOR_MENU_TYPE, CreativeGeneratorScreen::new);
        event.register(Holder.ADV_QUARRY_MENU_TYPE, AdvQuarryScreen::new);
        event.register(Holder.MINI_QUARRY_MENU_TYPE, MiniQuarryScreen::new);
        event.register(Holder.SOLID_FUEL_QUARRY_MENU_TYPE, SFQuarryScreen::new);
        event.register(Holder.FILLER_MENU_TYPE, FillerScreen::new);
        event.register(Holder.FILTER_MODULE_MENU_TYPE, FilterModuleScreen::new);
        event.register(Holder.ADV_PUMP_MENU_TYPE, AdvPumpScreen::new);
    }
}
