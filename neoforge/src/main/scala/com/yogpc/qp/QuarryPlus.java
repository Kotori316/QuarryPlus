package com.yogpc.qp;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.serialization.Codec;
import com.yogpc.qp.machines.*;
import com.yogpc.qp.machines.workbench.EnableCondition;
import com.yogpc.qp.machines.workbench.EnchantmentIngredient;
import com.yogpc.qp.machines.workbench.QuarryDebugCondition;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.ConfigCommand;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.VisibleForTesting;

@Mod(QuarryPlus.modID)
public class QuarryPlus {
    public static final String Mod_Name = "QuarryPlus";
    public static final String modID = "quarryplus";
    public static final Logger LOGGER = getLogger(Mod_Name);
    public static Config config;
    public static ClientConfig clientConfig;
    public static ServerConfig serverConfig;

    public QuarryPlus(IEventBus modBus) {
        registerConfig(false);
        modBus.register(Register.class);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            QuarryPlusClient.registerClientBus(modBus);
        }
        NeoForge.EVENT_BUS.register(ConfigCommand.class);
        NeoForge.EVENT_BUS.register(QuarryFakePlayer.class);
        NeoForge.EVENT_BUS.addListener(QuarryPlus::onServerStart);
    }

    @VisibleForTesting
    static void registerConfig(boolean inJUnitTest) {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        ModConfigSpec.Builder server = new ModConfigSpec.Builder();
        config = new Config(common);
        clientConfig = new ClientConfig(client);
        serverConfig = new ServerConfig(server);
        ModConfigSpec build = common.build();
        ModConfigSpec clientBuild = client.build();
        ModConfigSpec serverBuild = server.build();
        if (inJUnitTest || GameTestHooks.isGametestServer()) {
            // In game test. Use in-memory config.
            final CommentedConfig commentedConfig = CommentedConfig.inMemory();
            build.correct(commentedConfig);
            build.acceptConfig(commentedConfig);
            config.common.enableChunkLoader.set(false);
            config.common.logAllQuarryWork.set(false);
            final CommentedConfig clientConfig = CommentedConfig.inMemory();
            clientBuild.correct(clientConfig);
            clientBuild.acceptConfig(clientConfig);
            final CommentedConfig serverConfig = CommentedConfig.inMemory();
            serverBuild.correct(serverConfig);
            serverBuild.acceptConfig(serverConfig);
        } else {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, build);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientBuild);
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverBuild);
        }
    }

    // @Mod.EventBusSubscriber(modid = modID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {

        @SubscribeEvent
        public static void registerAll(RegisterEvent event) {
            event.register(Registries.BLOCK, Register::registerBlocks);
            event.register(Registries.ITEM, Register::registerItems);
            event.register(Registries.BLOCK_ENTITY_TYPE, Register::registerTiles);
            event.register(Registries.MENU, Register::registerContainers);
            event.register(Registries.RECIPE_SERIALIZER, Register::registerRecipe);
            event.register(Registries.RECIPE_TYPE, Register::registerRecipeType);
            event.register(Registries.CREATIVE_MODE_TAB, Register::registerCreativeTab);
            event.register(Registries.COMMAND_ARGUMENT_TYPE, Register::registerArgument);
            event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, Register::registerCondition);
            event.register(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Register::registerIngredient);
        }

        public static void registerBlocks(RegisterEvent.RegisterHelper<Block> blockRegisterHelper) {
            Holder.blocks().forEach(Holder.NamedEntry.register(blockRegisterHelper));
            blockRegisterHelper.register(Holder.BLOCK_DUMMY.location, Holder.BLOCK_DUMMY);
            blockRegisterHelper.register(Holder.BLOCK_DUMMY_REPLACER.location, Holder.BLOCK_DUMMY_REPLACER);
        }

        public static void registerItems(RegisterEvent.RegisterHelper<Item> helper) {
            Holder.items().forEach(Holder.NamedEntry.register(helper));
            helper.register(Holder.BLOCK_DUMMY.location, Holder.BLOCK_DUMMY.blockItem);
            helper.register(Holder.BLOCK_DUMMY_REPLACER.location, Holder.BLOCK_DUMMY_REPLACER.blockItem);
        }

        public static void registerTiles(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
            Holder.entityTypes().forEach(Holder.NamedEntry.register(helper));
        }

        public static void registerContainers(RegisterEvent.RegisterHelper<MenuType<?>> helper) {
            Holder.menuTypes().forEach(Holder.NamedEntry.register(helper));
        }

        public static void registerRecipe(RegisterEvent.RegisterHelper<RecipeSerializer<?>> helper) {
            helper.register(WorkbenchRecipe.recipeLocation, WorkbenchRecipe.SERIALIZER);
        }

        public static void registerCondition(RegisterEvent.RegisterHelper<Codec<? extends ICondition>> helper) {
            helper.register(EnableCondition.NAME, EnableCondition.CODEC);
            helper.register(QuarryDebugCondition.NAME, QuarryDebugCondition.CODEC);
        }

        public static void registerIngredient(RegisterEvent.RegisterHelper<IngredientType<?>> helper) {
            helper.register(new ResourceLocation(modID, EnchantmentIngredient.NAME), EnchantmentIngredient.TYPE);
        }

        public static void registerRecipeType(RegisterEvent.RegisterHelper<RecipeType<?>> helper) {
            helper.register(WorkbenchRecipe.recipeLocation, WorkbenchRecipe.RECIPE_TYPE);
        }

        public static void registerArgument(RegisterEvent.RegisterHelper<ArgumentTypeInfo<?, ?>> helper) {
            helper.register(new ResourceLocation(modID, "config_argument"),
                ArgumentTypeInfos.registerByClass(ConfigCommand.SelectorArgument.class, ConfigCommand.INFO));
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            PacketHandler.init();
        }

        public static void registerCreativeTab(RegisterEvent.RegisterHelper<CreativeModeTab> helper) {
            helper.register(new ResourceLocation(QuarryPlus.modID, "tab"), Holder.createTab(CreativeModeTab.builder()));
        }

        @SuppressWarnings("unchecked")
        @SubscribeEvent
        public static void registerCapability(RegisterCapabilitiesEvent event) {
            for (var entityType : Holder.entityTypes()) {
                if (PowerTile.class.isAssignableFrom(entityType.relatedClass())) {
                    var powerTileType = (BlockEntityType<? extends PowerTile>) entityType.t();
                    event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, powerTileType, PowerTile::getEnergyCapability);
                }
                if (HasItemHandler.class.isAssignableFrom(entityType.relatedClass())) {
                    var storageTileType = (BlockEntityType<? extends HasItemHandler>) entityType.t();
                    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, storageTileType, HasItemHandler::getItemCapability);
                }
                if (MachineStorage.HasStorage.class.isAssignableFrom(entityType.relatedClass())) {
                    var storageTileType = (BlockEntityType<? extends MachineStorage.HasStorage>) entityType.t();
                    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, storageTileType, MachineStorage.HasStorage::getFluidCapability);
                }
            }
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        try {
            var field = Class.forName("net.neoforged.fml.ModLoader").getDeclaredField("LOGGER");
            field.setAccessible(true);
            var loaderLogger = (org.apache.logging.log4j.core.Logger) field.get(null);
            return loaderLogger.getContext().getLogger(name);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't access to LOGGER in loader.", e);
        }
    }

    static void onServerStart(ServerStartedEvent event) {
        TraceQuarryWork.initialLog(event.getServer());
    }
}
