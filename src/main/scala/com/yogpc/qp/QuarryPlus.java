package com.yogpc.qp;

import java.util.Optional;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.Holder;
import com.yogpc.qp.utils.ProxyClient;
import com.yogpc.qp.utils.ProxyCommon;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.collection.JavaConverters;

@Mod(QuarryPlus.modID)
public class QuarryPlus {
    public static final String Mod_Name = "QuarryPlus";
    public static final String modID = "quarryplus";
    public static final Logger LOGGER = LogManager.getLogger(Mod_Name);

    @SuppressWarnings("Convert2MethodRef") // To avoid class loading error.
    public static final ProxyCommon proxy = DistExecutor.runForDist(() -> () -> new ProxyClient(), () -> () -> new ProxyCommon());

    public QuarryPlus() {
        initConfig();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        proxy.registerEvents(MinecraftForge.EVENT_BUS);
    }

    private void initConfig() {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        Optional.of(common)
            .map(Config::commonBuild)
            .ifPresent(preloadConfig -> ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, preloadConfig.build()));
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientBuild(client).build());
    }

    public void setup(FMLCommonSetupEvent event) {
        proxy.registerTextures(event);
        PacketHandler.init();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            JavaConverters.seqAsJavaList(Holder.blocks()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            JavaConverters.seqAsJavaList(Holder.blocks()).stream().map(Block::asItem).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
            JavaConverters.seqAsJavaList(Holder.tiles()).forEach(event.getRegistry()::register);
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
        public static final String replacer = "quarryreplacer";
        public static final String solidquarry = "solidquarry";
        public static final String tool = "tool";
        public static final String workbench = "workbenchplus";
    }
}
