package com.yogpc.qp;

import com.yogpc.qp.machines.bookmover.BookMoverScreen;
import com.yogpc.qp.machines.marker.Screen16Marker;
import com.yogpc.qp.machines.marker.ScreenFlexMarker;
import com.yogpc.qp.machines.misc.CreativeGeneratorScreen;
import com.yogpc.qp.machines.misc.YSetterScreen;
import com.yogpc.qp.machines.module.ScreenQuarryModule;
import com.yogpc.qp.machines.mover.MoverScreen;
import com.yogpc.qp.machines.placer.PlacerScreen;
import com.yogpc.qp.machines.workbench.ScreenWorkbench;
import com.yogpc.qp.render.Render16Marker;
import com.yogpc.qp.render.RenderFlexMarker;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = QuarryPlus.modID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuarryPlusClient {
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Holder.BLOCK_FRAME, RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(Holder.BLOCK_DUMMY, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(Holder.BLOCK_DUMMY_REPLACER, RenderType.translucent());

        BlockEntityRenderers.register(Holder.QUARRY_TYPE, RenderQuarry::new);
        BlockEntityRenderers.register(Holder.MARKER_TYPE, RenderMarker::new);
        BlockEntityRenderers.register(Holder.FLEX_MARKER_TYPE, RenderFlexMarker::new);
        BlockEntityRenderers.register(Holder.MARKER_16_TYPE, Render16Marker::new);

        MenuScreens.register(Holder.FLEX_MARKER_MENU_TYPE, ScreenFlexMarker::new);
        MenuScreens.register(Holder.MARKER_16_MENU_TYPE, Screen16Marker::new);
        MenuScreens.register(Holder.Y_SETTER_MENU_TYPE, YSetterScreen::new);
        MenuScreens.register(Holder.WORKBENCH_MENU_TYPE, ScreenWorkbench::new);
        MenuScreens.register(Holder.MOVER_MENU_TYPE, MoverScreen::new);
        MenuScreens.register(Holder.MODULE_MENU_TYPE, ScreenQuarryModule::new);
        MenuScreens.register(Holder.PLACER_MENU_TYPE, PlacerScreen::new);
        MenuScreens.register(Holder.BOOK_MOVER_MENU_TYPE, BookMoverScreen::new);
        MenuScreens.register(Holder.CREATIVE_GENERATOR_MENU_TYPE, CreativeGeneratorScreen::new);
    }
}
