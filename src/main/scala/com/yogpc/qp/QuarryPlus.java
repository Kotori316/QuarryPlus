/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import com.yogpc.qp.gui.GuiFactory;
import com.yogpc.qp.gui.GuiHandler;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.render.Sprites;
import com.yogpc.qp.tile.TileBreaker;
import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.TileMiningWell;
import com.yogpc.qp.tile.TilePlacer;
import com.yogpc.qp.tile.TilePump;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.tile.TileRefinery;
import com.yogpc.qp.tile.TileWorkbench;
import com.yogpc.qp.tile.WorkbenchRecipes;
import com.yogpc.qp.version.VersionDiff;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.yogpc.qp.QuarryPlusI.*;

@Mod(
        modid = QuarryPlus.modID,
        name = QuarryPlus.Mod_Name,
        version = "${version}",
        dependencies = "required-after:buildcraftcore;after:ic2",
        guiFactory = QuarryPlus.Optionals.configFactory
)
public class QuarryPlus {

    @SidedProxy(clientSide = Optionals.clientProxy, serverSide = Optionals.serverProxy)
    public static ProxyCommon proxy;
    public static final QuarryPlus INSTANCE;
    public static final VersionDiff DIFF;

    public static final String Mod_Name = "QuarryPlus";
    public static final String modID = "quarryplus";
    private static final String prefix = modID + ":";
    public static final Logger LOGGER = LogManager.getLogger(Mod_Name);
    public boolean inDev;

    static {
        if (!Optionals.clientProxy.equals(ProxyClient.class.getName())) {
            throw new AssertionError("Client Proxy name doesn't match!");
        }
        if (!Optionals.serverProxy.equals(ProxyCommon.class.getName())) {
            throw new AssertionError("Server Proxy name doesn't match!");
        }
        if (!Optionals.configFactory.equals(GuiFactory.class.getName())) {
            throw new AssertionError("GuiFactory name doesn't match!");
        }
        INSTANCE = new QuarryPlus();
        VersionDiff diff;
        try {
            diff = (VersionDiff) Class.forName("com.yogpc.qp.version.Diff" + String.valueOf(ForgeVersion.getMajorVersion() - 2)).newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("VersionDiff doesn't exist!", e);
        }
        DIFF = diff;
    }

    private QuarryPlus() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.InstanceFactory
    public static QuarryPlus getInstance() {
        return INSTANCE;
    }

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        Config.setConfigFile(event.getSuggestedConfigurationFile());
        ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, ChunkLoadingHandler.instance());
        proxy.registerTextures();
        MinecraftForge.EVENT_BUS.register(QuarryPlusI.INSANCE);
        MinecraftForge.EVENT_BUS.register(Loot.instance());
        MinecraftForge.EVENT_BUS.register(Config.instance());
        if (event.getSide() == Side.CLIENT)
            MinecraftForge.EVENT_BUS.register(Sprites.instance());
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
        inDev = ((Boolean) Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", Boolean.FALSE));
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        PacketHandler.init();
        GameRegistry.addRecipe(new ItemStack(workbench, 1),
                "III", "GDG", "RRR",
                'D', Blocks.DIAMOND_BLOCK, 'R', Items.REDSTONE,
                'I', Blocks.IRON_BLOCK, 'G', Blocks.GOLD_BLOCK);
        WorkbenchRecipes.registerRecipes();
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(blockQuarry);
        event.getRegistry().register(blockPump);
        event.getRegistry().register(blockMarker);
        event.getRegistry().register(blockMover);
        event.getRegistry().register(blockMiningWell);
        event.getRegistry().register(blockPlacer);
        event.getRegistry().register(blockBreaker);
        event.getRegistry().register(blockPlainPipe);
        event.getRegistry().register(blockFrame);
        event.getRegistry().register(workbench);
        event.getRegistry().register(controller);
        event.getRegistry().register(blockLaser);
        event.getRegistry().register(blockRefinery);
        GameRegistry.registerTileEntity(TileWorkbench.class, prefix + QuarryPlus.Names.workbench);
        GameRegistry.registerTileEntity(TileQuarry.class, prefix + QuarryPlus.Names.quarry);
        GameRegistry.registerTileEntity(TileMarker.class, prefix + QuarryPlus.Names.marker);
        GameRegistry.registerTileEntity(TileMiningWell.class, prefix + QuarryPlus.Names.miningwell);
        GameRegistry.registerTileEntity(TilePump.class, prefix + QuarryPlus.Names.pump);
        GameRegistry.registerTileEntity(TileRefinery.class, prefix + QuarryPlus.Names.refinery);
        GameRegistry.registerTileEntity(TilePlacer.class, prefix + QuarryPlus.Names.placer);
        GameRegistry.registerTileEntity(TileBreaker.class, prefix + QuarryPlus.Names.breaker);
        GameRegistry.registerTileEntity(TileLaser.class, prefix + QuarryPlus.Names.laser);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(blockQuarry.itemBlock());
        event.getRegistry().register(blockPump.itemBlock());
        event.getRegistry().register(blockMarker.itemBlock);
        event.getRegistry().register(blockMover.itemBlock);
        event.getRegistry().register(blockMiningWell.itemBlock());
        event.getRegistry().register(blockPlacer.itemBlock());
        event.getRegistry().register(blockBreaker.itemBlock());
        event.getRegistry().register(blockPlainPipe.itemBlock);
        event.getRegistry().register(blockFrame.itemBlock);
        event.getRegistry().register(workbench.itemBlock());
        event.getRegistry().register(controller.itemBlock);
        event.getRegistry().register(blockLaser.itemBlock());
        event.getRegistry().register(blockRefinery.itemBlock());
        event.getRegistry().register(itemTool);
        event.getRegistry().register(magicmirror);
        event.getRegistry().register(debugItem);
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(blockQuarry.itemBlock(), 0, new ModelResourceLocation(blockQuarry.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockFrame.itemBlock, 0, new ModelResourceLocation(blockFrame.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockFrame.itemBlock, 1, new ModelResourceLocation(blockFrame.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockMarker.itemBlock, 0, new ModelResourceLocation(blockMarker.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(workbench.itemBlock(), 0, new ModelResourceLocation(workbench.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockPump.itemBlock(), 0, new ModelResourceLocation(blockPump.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockMover.itemBlock, 0, new ModelResourceLocation(blockMover.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockBreaker.itemBlock(), 0, new ModelResourceLocation(blockBreaker.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockPlacer.itemBlock(), 0, new ModelResourceLocation(blockPlacer.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockMiningWell.itemBlock(), 0, new ModelResourceLocation(blockMiningWell.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockPlainPipe.itemBlock, 0, new ModelResourceLocation(blockPlainPipe.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockRefinery.itemBlock(), 0, new ModelResourceLocation(blockRefinery.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(controller.itemBlock, 0, new ModelResourceLocation(controller.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(blockLaser.itemBlock(), 0, new ModelResourceLocation(blockLaser.getRegistryName(), "inventory"));
//        ModelLoader.setCustomModelResourceLocation(blockChunkdestoryer.itemBlock, 0, new ModelResourceLocation(blockChunkdestoryer.getRegistryName, "inventory"))
        ModelLoader.setCustomModelResourceLocation(itemTool, 0, new ModelResourceLocation(prefix + ItemTool.statuschecker(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemTool, 1, new ModelResourceLocation(prefix + ItemTool.listeditor(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(itemTool, 2, new ModelResourceLocation(prefix + ItemTool.liquidselector(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(magicmirror, 0, new ModelResourceLocation(magicmirror.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(magicmirror, 1, new ModelResourceLocation(magicmirror.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(magicmirror, 2, new ModelResourceLocation(magicmirror.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(debugItem, 0, new ModelResourceLocation(debugItem.getRegistryName(), "inventory"));
    }

    @SuppressWarnings("unused")
    public static class Optionals {

        public static final String clientProxy = "com.yogpc.qp.ProxyClient";
        public static final String serverProxy = "com.yogpc.qp.ProxyCommon";
        public static final String configFactory = "com.yogpc.qp.gui.GuiFactory";
        public static final String IC2_modID = "ic2";
        public static final String Buildcraft_modID = "buildcraftcore";
        public static final String COFH_modID = "cofhcore";
        public static final String COFH_block = "cofhapi|block";
        public static final String COFH_energy = "cofhapi|energy";
        public static final String COFH_tileentity = "cofhapi|tileentity";
        public static final String BuildCraft_core = "BuildCraftAPI|core";
        public static final String Buildcraft_tools = "BuildCraftAPI|tools";
        public static final String Buildcraft_recipes = "BuildCraftAPI|recipes";
        public static final String Buildcraft_transport = "BuildCraftAPI|transport";
        public static final String Buildcraft_tiles = "BuildCraftAPI|tiles";
    }

    public static class Names {
        public static final String breaker = "breakerplus";
        public static final String placer = "placerplus";
        public static final String frame = "quarryframe";
        public static final String marker = "markerplus";
        public static final String plainpipe = "plainpipe";
        public static final String miningwell = "miningwellplus";
        public static final String quarry = "quarryplus";
        public static final String pump = "pumpplus";
        public static final String refinery = "refineryplus";
        public static final String workbench = "workbenchplus";
        public static final String controller = "spawnercontroller";
        public static final String mover = "enchantmover";
        public static final String laser = "laserplus";
        public static final String mirror = "magicmirror";
        public static final String tool = "tool";
        public static final String debug = "quarrydebug";
    }
}
