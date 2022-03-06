package com.yogpc.qp;

import com.yogpc.qp.data.QuarryPlusDataProvider;
import com.yogpc.qp.machines.workbench.EnableCondition;
import com.yogpc.qp.machines.workbench.EnchantmentIngredient;
import com.yogpc.qp.machines.workbench.QuarryDebugCondition;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.ConfigCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;

@Mod(QuarryPlus.modID)
public class QuarryPlus {
    public static final String Mod_Name = "QuarryPlus";
    public static final String modID = "quarryplus";
    public static final Logger LOGGER = getLogger(Mod_Name);
    public static Config config;

    public QuarryPlus() {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        config = new Config(common);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, common.build());
        FMLJavaModLoadingContext.get().getModEventBus().register(Register.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(QuarryPlusDataProvider.class);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> QuarryPlusClient::registerClientBus);
        MinecraftForge.EVENT_BUS.register(ConfigCommand.class);
    }

    // @Mod.EventBusSubscriber(modid = modID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            Holder.blocks().forEach(event.getRegistry()::register);
            event.getRegistry().register(Holder.BLOCK_DUMMY);
            event.getRegistry().register(Holder.BLOCK_DUMMY_REPLACER);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            Holder.items().forEach(event.getRegistry()::register);
            event.getRegistry().register(Holder.BLOCK_DUMMY.blockItem);
            event.getRegistry().register(Holder.BLOCK_DUMMY_REPLACER.blockItem);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<BlockEntityType<?>> event) {
            Holder.entityTypes().forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
            Holder.menuTypes().forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerRecipe(RegistryEvent.Register<RecipeSerializer<?>> event) {
            event.getRegistry().register(WorkbenchRecipe.SERIALIZER);
            CraftingHelper.register(new ResourceLocation(modID, EnchantmentIngredient.NAME), EnchantmentIngredient.Serializer.INSTANCE);
            CraftingHelper.register(new EnableCondition.Serializer());
            CraftingHelper.register(new QuarryDebugCondition.Serializer());
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            PacketHandler.init();
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
