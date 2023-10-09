package com.yogpc.qp;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.serialization.Codec;
import com.yogpc.qp.machines.QuarryFakePlayer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.ingredients.IIngredientSerializer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
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

    public QuarryPlus() {
        registerConfig(false);
        FMLJavaModLoadingContext.get().getModEventBus().register(Register.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> QuarryPlusClient::registerClientBus);
        MinecraftForge.EVENT_BUS.register(ConfigCommand.class);
        MinecraftForge.EVENT_BUS.register(QuarryFakePlayer.class);
    }

    @VisibleForTesting
    static void registerConfig(boolean inJUnitTest) {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();
        config = new Config(common);
        clientConfig = new ClientConfig(client);
        serverConfig = new ServerConfig(server);
        ForgeConfigSpec build = common.build();
        ForgeConfigSpec clientBuild = client.build();
        ForgeConfigSpec serverBuild = server.build();
        if (inJUnitTest || ForgeGameTestHooks.isGametestServer()) {
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
            event.register(ForgeRegistries.Keys.CONDITION_SERIALIZERS, Register::registerCondition);
            event.register(ForgeRegistries.Keys.INGREDIENT_SERIALIZERS, Register::registerIngredient);
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

        public static void registerIngredient(RegisterEvent.RegisterHelper<IIngredientSerializer<?>> helper) {
            helper.register(new ResourceLocation(modID, EnchantmentIngredient.NAME), EnchantmentIngredient.Serializer.INSTANCE);
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
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        try {
            var field = Class.forName("net.minecraftforge.fml.ModLoader").getDeclaredField("LOGGER");
            field.setAccessible(true);
            var loaderLogger = (org.apache.logging.log4j.core.Logger) field.get(null);
            return loaderLogger.getContext().getLogger(name);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't access to LOGGER in loader.", e);
        }
    }
}
