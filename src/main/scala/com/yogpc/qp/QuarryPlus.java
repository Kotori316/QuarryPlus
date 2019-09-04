package com.yogpc.qp;

import java.lang.reflect.Method;

import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.advquarry.BlockWrapper;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.workbench.WorkbenchRecipes;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.EnableCondition;
import com.yogpc.qp.utils.Holder;
import com.yogpc.qp.utils.ProxyClient;
import com.yogpc.qp.utils.ProxyCommon;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
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
        IEventBus modBus = QuarryPlus.modBus();
        initConfig(modBus);
        MinecraftForge.EVENT_BUS.register(this);
        proxy.registerEvents(MinecraftForge.EVENT_BUS);
        proxy.registerModBus(modBus);
        modBus.register(Register.class);
    }

    private void initConfig(IEventBus modEventBus) {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonBuild(common).build());
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientBuild(client).build());
        modEventBus.addListener(Config.reloadHandler());
    }

    @SubscribeEvent
    public void serverStart(FMLServerAboutToStartEvent event) {
        event.getServer().getResourceManager().addReloadListener(WorkbenchRecipes.Reload$.MODULE$);
        event.getServer().getResourceManager().addReloadListener(BlockWrapper.Reload$.MODULE$);
    }

//    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            JavaConverters.seqAsJavaList(Holder.blocks()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            JavaConverters.seqAsJavaList(Holder.blocks()).stream().map(Block::asItem).forEach(event.getRegistry()::register);
            JavaConverters.seqAsJavaList(Holder.items()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
            JavaConverters.asJavaCollection(Holder.tiles().keys()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
            JavaConverters.asJavaCollection(Holder.containers()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerRecipe(RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(WorkbenchRecipes.Serializer$.MODULE$);
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            proxy.registerTextures(event);
            PowerManager.configRegister();
            PacketHandler.init();
            CraftingHelper.register(new EnableCondition.Serializer());
            LootFunctionManager.registerFunction(new IEnchantableTile.DropFunction.Serializer());
        }

        @SubscribeEvent
        public static void loadComplete(FMLLoadCompleteEvent event) {
            proxy.setDummyTexture(Config.client().dummyTexture().get());
        }

    }

    private static IEventBus modBus() {
        Object o = ModLoadingContext.get().extension();
        try {
            Method busGetter = o.getClass().getMethod("getModEventBus");
            return (IEventBus) busGetter.invoke(o);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error in getting event bus.", e);
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
        public static final String updateJson = "https://raw.githubusercontent.com/Kotori316/QuarryPlus/1.13.2/update.json";
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
        public static final String laser = "laserplus";
        public static final String listeditor = "listeditor";
        public static final String liquidselector = "liquidselector";
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
