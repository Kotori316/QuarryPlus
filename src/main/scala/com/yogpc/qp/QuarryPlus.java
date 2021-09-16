package com.yogpc.qp;

import com.yogpc.qp.machines.workbench.EnableCondition;
import com.yogpc.qp.machines.workbench.EnchantmentIngredient;
import com.yogpc.qp.machines.workbench.QuarryDebugCondition;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(QuarryPlus.modID)
public class QuarryPlus {
    public static final String Mod_Name = "QuarryPlus";
    public static final String modID = "quarryplus";
    public static final Logger LOGGER = LogManager.getLogger(Mod_Name);
    public static Config config;

    public QuarryPlus() {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        config = new Config(common);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, common.build());
    }

    @Mod.EventBusSubscriber(modid = modID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
}
