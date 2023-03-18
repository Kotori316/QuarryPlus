package com.yogpc.qp;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.yogpc.qp.machines.workbench.EnableCondition;
import com.yogpc.qp.machines.workbench.EnchantmentIngredient;
import com.yogpc.qp.machines.workbench.QuarryDebugCondition;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.ConfigCommand;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.gametest.ForgeGameTestHooks;
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

    public QuarryPlus() {
        registerConfig(false);
        FMLJavaModLoadingContext.get().getModEventBus().register(Register.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> QuarryPlusClient::registerClientBus);
        MinecraftForge.EVENT_BUS.register(ConfigCommand.class);
    }

    @VisibleForTesting
    static void registerConfig(boolean inJUnitTest) {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        config = new Config(common);
        clientConfig = new ClientConfig(client);
        ForgeConfigSpec build = common.build();
        ForgeConfigSpec clientBuild = client.build();
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
        } else {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, build);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientBuild);
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
            CraftingHelper.register(new ResourceLocation(modID, EnchantmentIngredient.NAME), EnchantmentIngredient.Serializer.INSTANCE);
            CraftingHelper.register(new EnableCondition.Serializer());
            CraftingHelper.register(new QuarryDebugCondition.Serializer());
        }

        public static void registerRecipeType(RegisterEvent.RegisterHelper<RecipeType<?>> helper) {
            helper.register(WorkbenchRecipe.recipeLocation, WorkbenchRecipe.RECIPE_TYPE);
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            PacketHandler.init();
        }

        @SubscribeEvent
        public static void registerCreativeTab(CreativeModeTabEvent.Register event) {
            event.registerCreativeModeTab(new ResourceLocation(QuarryPlus.modID, "tab"), Holder::createTab);
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
