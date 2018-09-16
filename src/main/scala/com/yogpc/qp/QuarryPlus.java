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

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.gui.GuiFactory;
import com.yogpc.qp.gui.GuiHandler;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.tile.ItemDamage;
import com.yogpc.qp.tile.TileAdvPump;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.tile.TileBookMover;
import com.yogpc.qp.tile.TileBreaker;
import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.TileMiningWell;
import com.yogpc.qp.tile.TilePlacer;
import com.yogpc.qp.tile.TilePump;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.tile.TileRefinery;
import com.yogpc.qp.tile.TileSolidQuarry;
import com.yogpc.qp.tile.TileWorkbench;
import com.yogpc.qp.tile.WorkbenchRecipes;
import com.yogpc.qp.version.VersionDiff;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.yogpc.qp.QuarryPlusI.*;

@Mod(
    modid = QuarryPlus.modID,
    name = QuarryPlus.Mod_Name,
    version = "${version}",
    guiFactory = QuarryPlus.Optionals.configFactory,
    updateJSON = QuarryPlus.Optionals.updateJson,
    certificateFingerprint = "@FINGERPRINT@",
    dependencies = "required:forge@[14.23.4.2703,);"
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
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT && !Optionals.clientProxy.equals(ProxyClient.class.getName())) {
            throw new AssertionError("Client Proxy name doesn't match!");
        }
        if (!Optionals.serverProxy.equals(ProxyCommon.class.getName())) {
            throw new AssertionError("Server Proxy name doesn't match!");
        }
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT && !Optionals.configFactory.equals(GuiFactory.class.getName())) {
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
    }

    @Mod.InstanceFactory
    public static QuarryPlus instance() {
        return INSTANCE;
    }

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        Config.setConfigFile(event.getSuggestedConfigurationFile());
        ForgeChunkManager.setForcedChunkLoadingCallback(QuarryPlus.instance(), ChunkLoadingHandler.instance());
        MinecraftForge.EVENT_BUS.register(QuarryPlus.instance());
        if (!Config.content().disableDungeonLoot())
            MinecraftForge.EVENT_BUS.register(Loot.instance());
        proxy.registerTextures();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, GuiHandler.instance());
        inDev = ((Boolean) Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", Boolean.FALSE));
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        PacketHandler.init();
        WorkbenchRecipes.registerRecipes();
        if (inDev && ModAPIManager.INSTANCE.hasAPI(Optionals.Buildcraft_facades))
            BuildcraftHelper.disableFacade();
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
            blockQuarry(),
            blockPump(),
            blockMarker(),
            blockMover(),
            blockMiningWell(),
            blockPlacer(),
            blockBreaker(),
            blockPlainPipe(),
            blockFrame(),
            blockWorkbench(),
            blockController(),
            blockLaser(),
            blockRefinery(),
            blockChunkdestroyer(),
            blockStandalonePump(),
            blockBookMover(),
            blockSolidQuarry()
        );

        GameRegistry.registerTileEntity(TileWorkbench.class, new ResourceLocation(modID, QuarryPlus.Names.workbench));
        GameRegistry.registerTileEntity(TileQuarry.class, new ResourceLocation(modID, QuarryPlus.Names.quarry));
        GameRegistry.registerTileEntity(TileMarker.class, new ResourceLocation(modID, QuarryPlus.Names.marker));
        GameRegistry.registerTileEntity(TileMiningWell.class, new ResourceLocation(modID, QuarryPlus.Names.miningwell));
        GameRegistry.registerTileEntity(TilePump.class, new ResourceLocation(modID, QuarryPlus.Names.pump));
        GameRegistry.registerTileEntity(TileRefinery.class, new ResourceLocation(modID, QuarryPlus.Names.refinery));
        GameRegistry.registerTileEntity(TilePlacer.class, new ResourceLocation(modID, QuarryPlus.Names.placer));
        GameRegistry.registerTileEntity(TileBreaker.class, new ResourceLocation(modID, QuarryPlus.Names.breaker));
        GameRegistry.registerTileEntity(TileLaser.class, new ResourceLocation(modID, QuarryPlus.Names.laser));
        GameRegistry.registerTileEntity(TileAdvQuarry.class, new ResourceLocation(modID, QuarryPlus.Names.advquarry));
        GameRegistry.registerTileEntity(TileAdvPump.class, new ResourceLocation(modID, QuarryPlus.Names.advpump));
        GameRegistry.registerTileEntity(TileBookMover.class, new ResourceLocation(modID, QuarryPlus.Names.moverfrombook));
        GameRegistry.registerTileEntity(TileSolidQuarry.class, new ResourceLocation(modID, QuarryPlus.Names.solidquarry));
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            blockQuarry().itemBlock(),
            blockPump().itemBlock(),
            blockMarker().itemBlock,
            blockMover().itemBlock,
            blockMiningWell().itemBlock(),
            blockPlacer().itemBlock(),
            blockBreaker().itemBlock(),
            blockPlainPipe().itemBlock,
            blockFrame().itemBlock,
            blockWorkbench().itemBlock(),
            blockController().itemBlock,
            blockLaser().itemBlock(),
            blockRefinery().itemBlock(),
            blockChunkdestroyer().itemBlock(),
            blockStandalonePump().itemBlock(),
            blockBookMover().itemBlock(),
            blockSolidQuarry().itemBlock(),
            itemTool(),
            magicmirror(),
            debugItem()
        );
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        final String variantIn = "inventory";
        ModelLoader.setCustomModelResourceLocation(blockQuarry().itemBlock(), 0, proxy.fromEntry(blockQuarry()));
        ModelLoader.setCustomModelResourceLocation(blockFrame().itemBlock, 0, proxy.fromEntry(blockFrame()));
        ModelLoader.setCustomModelResourceLocation(blockFrame().itemBlock, 1, proxy.fromEntry(blockFrame()));
        ModelLoader.setCustomModelResourceLocation(blockMarker().itemBlock, 0, proxy.fromEntry(blockMarker()));
        ModelLoader.setCustomModelResourceLocation(blockWorkbench().itemBlock(), 0, proxy.fromEntry(blockWorkbench()));
        ModelLoader.setCustomModelResourceLocation(blockPump().itemBlock(), 0, proxy.fromEntry(blockPump()));
        ModelLoader.setCustomModelResourceLocation(blockMover().itemBlock, 0, proxy.fromEntry(blockMover()));
        ModelLoader.setCustomModelResourceLocation(blockBreaker().itemBlock(), 0, proxy.fromEntry(blockBreaker()));
        ModelLoader.setCustomModelResourceLocation(blockPlacer().itemBlock(), 0, proxy.fromEntry(blockPlacer()));
        ModelLoader.setCustomModelResourceLocation(blockMiningWell().itemBlock(), 0, proxy.fromEntry(blockMiningWell()));
        ModelLoader.setCustomModelResourceLocation(blockPlainPipe().itemBlock, 0, proxy.fromEntry(blockPlainPipe()));
        ModelLoader.setCustomModelResourceLocation(blockRefinery().itemBlock(), 0, proxy.fromEntry(blockRefinery()));
        ModelLoader.setCustomModelResourceLocation(blockController().itemBlock, 0, proxy.fromEntry(blockController()));
        ModelLoader.setCustomModelResourceLocation(blockLaser().itemBlock(), 0, proxy.fromEntry(blockLaser()));
        ModelLoader.setCustomModelResourceLocation(blockChunkdestroyer().itemBlock(), 0, proxy.fromEntry(blockChunkdestroyer()));
        ModelLoader.setCustomModelResourceLocation(blockStandalonePump().itemBlock(), 0, proxy.fromEntry(blockStandalonePump()));
        ModelLoader.setCustomModelResourceLocation(blockBookMover().itemBlock(), 0, proxy.fromEntry(blockBookMover()));
        ModelLoader.setCustomModelResourceLocation(blockSolidQuarry().itemBlock(), 0, proxy.fromEntry(blockSolidQuarry()));
        ModelLoader.setCustomModelResourceLocation(itemTool(), 0, new ModelResourceLocation(prefix + ItemTool.statuschecker(), variantIn));
        ModelLoader.setCustomModelResourceLocation(itemTool(), 1, new ModelResourceLocation(prefix + ItemTool.listeditor(), variantIn));
        ModelLoader.setCustomModelResourceLocation(itemTool(), 2, new ModelResourceLocation(prefix + ItemTool.liquidselector(), variantIn));
        ModelLoader.setCustomModelResourceLocation(magicmirror(), 0, proxy.fromEntry(magicmirror()));
        ModelLoader.setCustomModelResourceLocation(magicmirror(), 1, proxy.fromEntry(magicmirror()));
        ModelLoader.setCustomModelResourceLocation(magicmirror(), 2, proxy.fromEntry(magicmirror()));
        ModelLoader.setCustomModelResourceLocation(debugItem(), 0, proxy.fromEntry(debugItem()));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        TileMarker.Link[] la = TileMarker.linkList.toArray(new TileMarker.Link[0]);
        for (TileMarker.Link link : la) {
            if (link.w == event.getWorld()) link.removeConnection(false);
        }

        TileMarker.Laser[] lb = TileMarker.laserList.toArray(new TileMarker.Laser[0]);
        for (TileMarker.Laser laser : lb) {
            if (laser.w == event.getWorld()) laser.destructor();
        }
    }

    /**
     * Message key must be either {@code IMC_RemoveRecipe} or {@code IMC_AddRecipe}.
     * Message value must be NBTTag.
     *
     * @param event event
     */
    @Mod.EventHandler
    public void message(FMLInterModComms.IMCEvent event) {
        event.getMessages().forEach(imcMessage -> {
            NBTTagCompound nbtValue = imcMessage.getNBTValue();
            Function<NBTTagCompound, ItemStack> toStack = VersionUtil::fromNBTTag;
            if (Optionals.IMC_Remove.equals(imcMessage.key)) {
                WorkbenchRecipes.removeRecipe(ItemDamage.apply(toStack.apply(nbtValue)));
            } else if (Optionals.IMC_Add.equals(imcMessage.key)) {
                Function<ItemStack, IntFunction<ItemStack>> toFunc = stack -> (IntFunction<ItemStack>) integer ->
                    ItemHandlerHelper.copyStackWithSize(stack, VersionUtil.getCount(stack) * integer);

                NBTTagList list = nbtValue.getTagList(Optionals.IMC_Add, Constants.NBT.TAG_COMPOUND);
                ItemDamage result = ItemDamage.apply(toStack.apply(list.getCompoundTagAt(0)));
                List<IntFunction<ItemStack>> functionList = VersionUtil.nbtListStream(list).skip(1).map(toStack.andThen(toFunc)).collect(Collectors.toList());
                WorkbenchRecipes.addListRecipe(result, nbtValue.getInteger(Optionals.IMC_Energy), functionList, true, WorkbenchRecipes.UnitRF());
            }
        });
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        if (!inDev) {
            LOGGER.warn("Invalid fingerprint detected! The file " + event.getSource().getName() +
                " may have been tampered with. This version will NOT be supported by the author!");
        }
    }

    @SuppressWarnings("unused")
    public static class Optionals {
        public static final String BuildCraft_core = "BuildCraftAPI|core";
        public static final String Buildcraft_facades = "BuildCraftAPI|facades";
        public static final String Buildcraft_modID = "buildcraftcore";
        public static final String Buildcraft_recipes = "BuildCraftAPI|recipes";
        public static final String Buildcraft_tiles = "BuildCraftAPI|tiles";
        public static final String Buildcraft_tools = "BuildCraftAPI|tools";
        public static final String Buildcraft_transport = "BuildCraftAPI|transport";
        public static final String COFH_modID = "cofhcore";
        public static final String IC2_modID = "ic2";
        public static final String IMC_Add = "IMC_AddRecipe";
        public static final String IMC_Energy = "energy";
        public static final String IMC_Remove = "IMC_RemoveRecipe";
        public static final String Mekanism_modID = "mekanism";
        public static final String RedstoneFlux_modID = "redstoneflux";
        public static final String Thaumcraft_modID = "thaumcraft";
        public static final String clientProxy = "com.yogpc.qp.ProxyClient";
        public static final String configFactory = "com.yogpc.qp.gui.GuiFactory";
        public static final String serverProxy = "com.yogpc.qp.ProxyCommon";
        public static final String updateJson = "https://raw.githubusercontent.com/Kotori316/QuarryPlus/1.12/update.json";
    }

    public static class Names {
        public static final String advpump = "standalonepump";
        public static final String advquarry = "chunkdestroyer";
        public static final String breaker = "breakerplus";
        public static final String controller = "spawnercontroller";
        public static final String debug = "quarrydebug";
        public static final String frame = "quarryframe";
        public static final String laser = "laserplus";
        public static final String marker = "markerplus";
        public static final String miningwell = "miningwellplus";
        public static final String mirror = "magicmirror";
        public static final String mover = "enchantmover";
        public static final String moverfrombook = "enchantmoverfrombook";
        public static final String placer = "placerplus";
        public static final String plainpipe = "plainpipe";
        public static final String pump = "pumpplus";
        public static final String quarry = "quarryplus";
        public static final String refinery = "refineryplus";
        public static final String solidquarry = "solidquarry";
        public static final String tool = "tool";
        public static final String workbench = "workbenchplus";
    }
}
