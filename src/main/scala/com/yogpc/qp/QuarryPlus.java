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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.IC2Harvest;
import com.yogpc.qp.gui.GuiFactory;
import com.yogpc.qp.gui.GuiHandler;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.tile.ItemDamage;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.WorkbenchRecipes;
import com.yogpc.qp.version.VersionDiff;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
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
import scala.collection.JavaConverters;

import static com.yogpc.qp.QuarryPlusI.blockBookMover;
import static com.yogpc.qp.QuarryPlusI.blockBreaker;
import static com.yogpc.qp.QuarryPlusI.blockChunkDestroyer;
import static com.yogpc.qp.QuarryPlusI.blockController;
import static com.yogpc.qp.QuarryPlusI.blockExpPump;
import static com.yogpc.qp.QuarryPlusI.blockFiller;
import static com.yogpc.qp.QuarryPlusI.blockFrame;
import static com.yogpc.qp.QuarryPlusI.blockLaser;
import static com.yogpc.qp.QuarryPlusI.blockList;
import static com.yogpc.qp.QuarryPlusI.blockMarker;
import static com.yogpc.qp.QuarryPlusI.blockMiningWell;
import static com.yogpc.qp.QuarryPlusI.blockMover;
import static com.yogpc.qp.QuarryPlusI.blockPlacer;
import static com.yogpc.qp.QuarryPlusI.blockPlainPipe;
import static com.yogpc.qp.QuarryPlusI.blockPump;
import static com.yogpc.qp.QuarryPlusI.blockQuarry;
import static com.yogpc.qp.QuarryPlusI.blockQuarry2;
import static com.yogpc.qp.QuarryPlusI.blockRefinery;
import static com.yogpc.qp.QuarryPlusI.blockReplacer;
import static com.yogpc.qp.QuarryPlusI.blockSolidQuarry;
import static com.yogpc.qp.QuarryPlusI.blockStandalonePump;
import static com.yogpc.qp.QuarryPlusI.blockWorkbench;
import static com.yogpc.qp.QuarryPlusI.debugItem;
import static com.yogpc.qp.QuarryPlusI.dummyBlock;
import static com.yogpc.qp.QuarryPlusI.expPumpModule;
import static com.yogpc.qp.QuarryPlusI.fuelModuleCreative;
import static com.yogpc.qp.QuarryPlusI.fuelModuleNormal;
import static com.yogpc.qp.QuarryPlusI.itemList;
import static com.yogpc.qp.QuarryPlusI.itemQuarryPickaxe;
import static com.yogpc.qp.QuarryPlusI.itemTemplate;
import static com.yogpc.qp.QuarryPlusI.itemTool;
import static com.yogpc.qp.QuarryPlusI.magicMirror;
import static com.yogpc.qp.QuarryPlusI.pumpModule;
import static com.yogpc.qp.QuarryPlusI.replacerModule;
import static com.yogpc.qp.QuarryPlusI.tileIdMap;
import static com.yogpc.qp.QuarryPlusI.torchModule;

@Mod(
    modid = QuarryPlus.modID,
    name = QuarryPlus.Mod_Name,
    version = "${version}",
    guiFactory = QuarryPlus.Optionals.configFactory,
    updateJSON = QuarryPlus.Optionals.updateJson,
    certificateFingerprint = "@FINGERPRINT@",
    dependencies = "required:forge@[14.23.4.2705,);"
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
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT &&
            !Optionals.clientProxy.toLowerCase(Locale.ROOT).equals(ProxyClient.class.getName().toLowerCase(Locale.ROOT))) {
            throw new AssertionError("Client Proxy name doesn't match!");
        }
        if (!Optionals.serverProxy.toLowerCase(Locale.ROOT).equals(ProxyCommon.class.getName().toLowerCase(Locale.ROOT))) {
            throw new AssertionError("Server Proxy name doesn't match!");
        }
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT &&
            !Optionals.configFactory.toLowerCase(Locale.ROOT).equals(GuiFactory.class.getName().toLowerCase(Locale.ROOT))) {
            throw new AssertionError("GuiFactory name doesn't match!");
        }
        INSTANCE = new QuarryPlus();
        VersionDiff diff;
        try {
            diff = (VersionDiff) Class.forName("com.yogpc.qp.version.Diff" + (ForgeVersion.getMajorVersion() - 2)).newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("VersionDiff doesn't exist!", e);
        }
        DIFF = diff;
        FMLCommonHandler.instance().getDataFixer().init(modID, 5).registerFix(FixTypes.BLOCK_ENTITY, FixData.EnergyNBTFix$.MODULE$);
    }

    private QuarryPlus() {
    }

    @Mod.InstanceFactory
    public static QuarryPlus instance() {
        return INSTANCE;
    }

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        Config.setConfigFile(event.getSuggestedConfigurationFile(), new File(event.getModConfigurationDirectory(), modID + "/" + modID + ".cfg"));
        ForgeChunkManager.setForcedChunkLoadingCallback(QuarryPlus.instance(), ChunkLoadingHandler.instance());
        MinecraftForge.EVENT_BUS.register(QuarryPlus.instance());
        if (!Config.content().disableDungeonLoot())
            MinecraftForge.EVENT_BUS.register(Loot.instance());
        proxy.registerTextures();
        MinecraftForge.EVENT_BUS.register(proxy);
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, GuiHandler.instance());
        inDev = ((Boolean) Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", Boolean.FALSE));
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        PacketHandler.init();
        WorkbenchRecipes.registerRecipes();
        Config.content().outputRecipeJson();
        Config.recipeSync();
        // TODO change to net.minecraftforge.fml.common.ModAPIManager
        if (inDev && Loader.isModLoaded(Optionals.Buildcraft_facades))
            BuildcraftHelper.disableFacade();
        if (Loader.isModLoaded(Optionals.IC2_modID)) {
            MinecraftForge.EVENT_BUS.register(IC2Harvest.class);
        }
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
            blockList().toArray(new Block[0])
        );
        JavaConverters.mapAsJavaMapConverter(tileIdMap()).asJava().forEach(GameRegistry::registerTileEntity);
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
            blockChunkDestroyer().itemBlock(),
            blockStandalonePump().itemBlock(),
            blockBookMover().itemBlock(),
            blockExpPump().itemBlock(),
            blockSolidQuarry().itemBlock(),
            dummyBlock().itemBlock(),
            blockReplacer().itemBlock(),
            blockQuarry2().itemBlock(),
            blockFiller().itemBlock()
        );
        event.getRegistry().registerAll(
            itemList().toArray(new Item[0])
        );
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
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
        ModelLoader.setCustomModelResourceLocation(blockChunkDestroyer().itemBlock(), 0, proxy.fromEntry(blockChunkDestroyer()));
        ModelLoader.setCustomModelResourceLocation(blockStandalonePump().itemBlock(), 0, proxy.fromEntry(blockStandalonePump()));
        ModelLoader.setCustomModelResourceLocation(blockBookMover().itemBlock(), 0, proxy.fromEntry(blockBookMover()));
        ModelLoader.setCustomModelResourceLocation(blockExpPump().itemBlock(), 0, proxy.fromEntry(blockExpPump()));
        ModelLoader.setCustomModelResourceLocation(blockSolidQuarry().itemBlock(), 0, proxy.fromEntry(blockSolidQuarry()));
        ModelLoader.setCustomModelResourceLocation(dummyBlock().itemBlock(), 0, proxy.fromEntry(dummyBlock()));
        ModelLoader.setCustomModelResourceLocation(blockReplacer().itemBlock(), 0, proxy.fromEntry(blockReplacer()));
        ModelLoader.setCustomModelResourceLocation(blockQuarry2().itemBlock(), 0, proxy.fromEntry(blockQuarry2()));
        ModelLoader.setCustomModelResourceLocation(blockFiller().itemBlock(), 0, proxy.fromEntry(blockFiller()));
        ModelLoader.setCustomModelResourceLocation(itemTool(), ItemTool.meta_StatusChecker(), ModelLoader.getInventoryVariant(prefix + ItemTool.statuschecker()));
        ModelLoader.setCustomModelResourceLocation(itemTool(), ItemTool.meta_ListEditor(), ModelLoader.getInventoryVariant(prefix + ItemTool.listeditor()));
        ModelLoader.setCustomModelResourceLocation(itemTool(), ItemTool.meta_LiquidSelector(), ModelLoader.getInventoryVariant(prefix + ItemTool.liquidselector()));
        ModelLoader.setCustomModelResourceLocation(itemTool(), ItemTool.meta_YSetter(), ModelLoader.getInventoryVariant(prefix + ItemTool.ySetter()));
        ModelLoader.setCustomModelResourceLocation(itemQuarryPickaxe(), 0, proxy.fromEntry(itemQuarryPickaxe()));
        ModelLoader.setCustomModelResourceLocation(magicMirror(), 0, proxy.fromEntry(magicMirror()));
        ModelLoader.setCustomModelResourceLocation(magicMirror(), 1, proxy.fromEntry(magicMirror()));
        ModelLoader.setCustomModelResourceLocation(magicMirror(), 2, proxy.fromEntry(magicMirror()));
        ModelLoader.setCustomModelResourceLocation(debugItem(), 0, proxy.fromEntry(debugItem()));
        ModelLoader.setCustomModelResourceLocation(itemTemplate(), 0, proxy.fromEntry(itemTemplate()));
        ModelLoader.setCustomModelResourceLocation(pumpModule(), 0, proxy.fromEntry(pumpModule()));
        ModelLoader.setCustomModelResourceLocation(expPumpModule(), 0, proxy.fromEntry(expPumpModule()));
        ModelLoader.setCustomModelResourceLocation(replacerModule(), 0, proxy.fromEntry(replacerModule()));
        ModelLoader.setCustomModelResourceLocation(torchModule(), 0, proxy.fromEntry(torchModule()));
        ModelLoader.setCustomModelResourceLocation(fuelModuleNormal(), 0, proxy.fromEntry(fuelModuleNormal()));
        ModelLoader.setCustomModelResourceLocation(fuelModuleCreative(), 0, proxy.fromEntry(fuelModuleCreative()));
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
     * Message value must be NBTTag. The NBTTag must have recipe id ({@link ResourceLocation}).
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
                ResourceLocation location = new ResourceLocation(nbtValue.getString("id"));
                ItemDamage result = ItemDamage.apply(toStack.apply(list.getCompoundTagAt(0)));
                List<IntFunction<ItemStack>> functionList = VersionUtil.nbtListStream(list).skip(1).map(toStack.andThen(toFunc)).collect(Collectors.toList());
                WorkbenchRecipes.addListRecipe(location, result, nbtValue.getInteger(Optionals.IMC_Energy), functionList, true, WorkbenchRecipes.UnitRF());
            }
        });
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        if (!event.isDirectory()) {
            LOGGER.warn("Invalid fingerprint detected! The file " + event.getSource().getName() +
                " may have been tampered with. This version will NOT be supported by the author!" + System.lineSeparator() +
                "Download: https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner");
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess", "SpellCheckingInspection"})
    public static class Optionals {
        public static final String Buildcraft_modID = "buildcraftlib";
        public static final String BuildCraft_core = Buildcraft_modID; // buildcraftapi_core
        public static final String Buildcraft_facades = Buildcraft_modID; // buildcraftapi_facades
        public static final String Buildcraft_recipes = Buildcraft_modID; // buildcraftapi_recipes
        public static final String Buildcraft_tiles = Buildcraft_modID; // buildcraftapi_tiles
        public static final String Buildcraft_tools = Buildcraft_modID; // buildcraftapi_tools
        public static final String Buildcraft_transport = Buildcraft_modID; // buildcraftapi_transport
        public static final String Buildcraft_silicon_modID = "buildcraftsilicon";
        public static final String Buildcraft_factory_modID = "buildcraftfactory";
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

    @SuppressWarnings("SpellCheckingInspection")
    public static class Names {
        public static final String advpump = "standalonepump";
        public static final String advquarry = "chunkdestroyer";
        public static final String breaker = "breakerplus";
        public static final String controller = "spawnercontroller";
        public static final String debug = "quarrydebug";
        public static final String dummyblock = "dummyblock";
        public static final String exppump = "exppump";
        public static final String exppumpModule = "exppump_module";
        public static final String frame = "quarryframe";
        public static final String fuelModule = "fuel_module";
        public static final String filler = "filler";
        public static final String laser = "laserplus";
        public static final String marker = "markerplus";
        public static final String miningwell = "miningwellplus";
        public static final String mirror = "magicmirror";
        public static final String mover = "enchantmover";
        public static final String moverfrombook = "enchantmoverfrombook";
        public static final String placer = "placerplus";
        public static final String plainpipe = "plainpipe";
        public static final String pump = "pumpplus";
        public static final String pumpModule = "pump_module";
        public static final String quarry = "quarryplus";
        public static final String quarry2 = "quarry";
        public static final String quarryPickaxe = "quarry_pickaxe";
        public static final String refinery = "refineryplus";
        public static final String replacer = "quarryreplacer";
        public static final String replacerModule = "replacer_module";
        public static final String solidquarry = "solidquarry";
        public static final String template = "template";
        public static final String tool = "tool";
        public static final String torchModule = "torch_module";
        public static final String workbench = "workbenchplus";
    }
}
