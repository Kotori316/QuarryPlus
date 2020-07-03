package com.kotori316.marker;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.kotori316.marker.gui.ContainerMarker;
import com.kotori316.marker.gui.Gui16Marker;
import com.kotori316.marker.gui.GuiMarker;
import com.kotori316.marker.packet.PacketHandler;
import com.kotori316.marker.render.Render16Marker;
import com.kotori316.marker.render.RenderMarker;
import com.kotori316.marker.render.Resources;

public class Marker {
    public static final String modID = QuarryPlus.modID;
    public static final String ModName = "FlexibleMarker";
    public static final Group ITEM_GROUP = new Group();

    // Called via reflection in constructor of QuarryPlus.
    public Marker() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(Caps.Event.class);
    }

    @SubscribeEvent
    public void preInit(FMLCommonSetupEvent event) {
//        NetworkRegistry.INSTANCE.registerGuiHandler(getInstance(), new GuiHandler());
        PacketHandler.init();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientInit(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(Entries.TYPE, RenderMarker::new);
        ClientRegistry.bindTileEntityRenderer(Entries.TYPE16, Render16Marker::new);
        FMLJavaModLoadingContext.get().getModEventBus().register(Resources.getInstance());
        ScreenManager.registerFactory(Entries.CONTAINER_TYPE, GuiMarker::new);
        ScreenManager.registerFactory(Entries.CONTAINER16_TYPE, Gui16Marker::new);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(Entries.blockMarker);
        event.getRegistry().register(Entries.block16Marker);
    }

    @SubscribeEvent
    public void registerTile(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(Entries.TYPE.setRegistryName(modID + ":flexiblemarker"));
        event.getRegistry().register(Entries.TYPE16.setRegistryName(modID + ":marker16"));
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(Entries.blockMarker.itemBlock);
        event.getRegistry().register(Entries.block16Marker.itemBlock);
        event.getRegistry().register(Entries.remoteControlItem);
    }

    @SubscribeEvent
    public void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(Entries.CONTAINER_TYPE.setRegistryName(BlockMarker.GUI_ID));
        event.getRegistry().register(Entries.CONTAINER16_TYPE.setRegistryName(BlockMarker.GUI16_ID));
    }

    public static class Entries {
        public static final BlockMarker blockMarker = new BlockMarker.BlockFlexMarker();
        public static final TileEntityType<TileFlexMarker> TYPE = TileEntityType.Builder.create(TileFlexMarker::new, blockMarker).build(DSL.emptyPartType());
        public static final BlockMarker block16Marker = new BlockMarker.Block16Marker();
        public static final TileEntityType<Tile16Marker> TYPE16 = TileEntityType.Builder.create(Tile16Marker::new, block16Marker).build(DSL.emptyPartType());
        public static final ContainerType<ContainerMarker> CONTAINER_TYPE = IForgeContainerType.create((windowId, inv, data) ->
            new ContainerMarker(windowId, inv.player, data.readBlockPos(), Entries.CONTAINER_TYPE));
        public static final ContainerType<ContainerMarker> CONTAINER16_TYPE = IForgeContainerType.create((windowId, inv, data) ->
            new ContainerMarker(windowId, inv.player, data.readBlockPos(), Entries.CONTAINER16_TYPE));
        public static final RemoteControlItem remoteControlItem = new RemoteControlItem();
    }

    public static final class Group extends ItemGroup {
        public Group() {
            super("flexiblemarker");
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Entries.blockMarker);
        }
    }
}
