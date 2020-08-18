package com.yogpc.qp;

import java.nio.file.Paths;

import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.advquarry.BlockWrapper;
import com.yogpc.qp.machines.base.IMarker;
import com.yogpc.qp.machines.base.IRemotePowerOn;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.machines.workbench.WorkbenchRecipes;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.EnableCondition;
import com.yogpc.qp.utils.EnchantmentIngredient;
import com.yogpc.qp.utils.Holder;
import com.yogpc.qp.utils.ProxyProvider;
import com.yogpc.qp.utils.QuarryConfigCondition;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

@Mod(QuarryPlus.modID)
public class QuarryPlus {
    public static final String Mod_Name = "QuarryPlus";
    public static final String modID = "quarryplus";
    public static final Logger LOGGER = LogManager.getLogger(Mod_Name);

    public static final ProxyProvider.AbstractProxy proxy = ProxyProvider.getInstance();

    public QuarryPlus() {
        initConfig();
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
        callMarker();
    }

    private void initConfig() {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonBuild(common).build());
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientBuild(client).build());
    }

    public void serverStart(AddReloadListenerEvent event) {
        event.addListener(BlockWrapper.Reload$.MODULE$);
        event.addListener(QuarryBlackList.Reload$.MODULE$);
    }

    private static void callMarker() {
        try {
            Class.forName("com.kotori316.marker.Marker").getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Error loading in marker.", e);
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID)
    public static class Register {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            CollectionConverters.asJava(Holder.blocks()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            CollectionConverters.asJava(Holder.blocks()).stream().map(Block::asItem).forEach(event.getRegistry()::register);
            CollectionConverters.asJava(Holder.items()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
            CollectionConverters.asJava(Holder.tiles().keys()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
            CollectionConverters.asJava(Holder.containers()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerRecipe(RegistryEvent.Register<IRecipeSerializer<?>> event) {
            CraftingHelper.register(new EnableCondition.Serializer());
            CraftingHelper.register(new QuarryConfigCondition.Serializer());
            CraftingHelper.register(new ResourceLocation(modID, "enchantment_ingredient"), EnchantmentIngredient.Serializer.INSTANCE);
            event.getRegistry().register(WorkbenchRecipes.Serializer$.MODULE$);
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            PowerManager.configRegister();
            PacketHandler.init();
            Config.common().outputPowerDetail(Paths.get("config\\quarryplus"));
            IMarker.Cap.register();
            IRemotePowerOn.Cap.register();
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
        public static final String Mekanism_modID = "mekanism";
        public static final String RedstoneFlux_modID = "redstoneflux";
        public static final String Thaumcraft_modID = "thaumcraft";
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static class Names {
        public static final String advpump = "standalonepump";
        public static final String advquarry = "chunkdestroyer";
        public static final String bedrockModule = "remove_bedrock_module";
        public static final String breaker = "breakerplus";
        public static final String controller = "spawnercontroller";
        public static final String creative_generator = "creative_generator";
        public static final String debug = "quarrydebug";
        public static final String dummyblock = "dummyblock";
        public static final String exppump = "exppump";
        public static final String exppumpModule = "exppump_module";
        public static final String frame = "quarryframe";
        public static final String fuelModule = "fuel_module";
        public static final String laser = "laser_plus";
        public static final String listeditor = "listeditor";
        public static final String liquidselector = "liquidselector";
        public static final String marker = "markerplus";
        public static final String miningwell = "miningwellplus";
        public static final String mini_quarry = "mini_quarry";
        public static final String mirror = "magicmirror";
        public static final String mover = "enchantmover";
        public static final String moverfrombook = "enchantmoverfrombook";
        public static final String placer = "placer_plus";
        public static final String plainpipe = "plainpipe";
        public static final String pump = "pumpplus";
        public static final String pumpModule = "pump_module";
        public static final String quarry = "quarryplus";
        public static final String quarry2 = "quarry";
        public static final String quarry_pickaxe = "quarry_pickaxe";
        public static final String refinery = "refineryplus";
        public static final String replacer = "quarryreplacer";
        public static final String replacerModule = "replacer_module";
        public static final String solidquarry = "solidquarry";
        public static final String statuschecker = "statuschecker";
        public static final String template = "template";
        public static final String torchModule = "torch_module";
        public static final String ySetter = "y_setter";
        public static final String workbench = "workbenchplus";
    }
}
