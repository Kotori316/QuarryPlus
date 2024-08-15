package com.yogpc.qp.neoforge;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.YSetterScreen;
import com.yogpc.qp.machine.module.ModuleScreen;
import com.yogpc.qp.machine.mover.MoverScreen;
import com.yogpc.qp.neoforge.render.RenderMarkerNeoForge;
import com.yogpc.qp.neoforge.render.RenderQuarryNeoForge;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class QuarryPlusClientNeoForge {
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        QuarryPlus.LOGGER.info("Initialize Client");
        BlockEntityRenderers.register(PlatformAccessNeoForge.RegisterObjectsNeoForge.QUARRY_ENTITY_TYPE.get(), RenderQuarryNeoForge::new);
        BlockEntityRenderers.register(PlatformAccessNeoForge.RegisterObjectsNeoForge.MARKER_ENTITY_TYPE.get(), RenderMarkerNeoForge::new);
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }

    @SubscribeEvent
    public static void registerMenu(RegisterMenuScreensEvent event) {
        event.register(PlatformAccessNeoForge.RegisterObjectsNeoForge.Y_SET_MENU_TYPE.get(), YSetterScreen::new);
        event.register(PlatformAccessNeoForge.RegisterObjectsNeoForge.MOVER_MENU_TYPE.get(), MoverScreen::new);
        event.register(PlatformAccessNeoForge.RegisterObjectsNeoForge.MODULE_MENU_TYPE.get(), ModuleScreen::new);
    }
}
