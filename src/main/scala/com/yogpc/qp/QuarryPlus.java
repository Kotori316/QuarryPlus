package com.yogpc.qp;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.integration.EnergyIntegration;
import com.yogpc.qp.integration.QuarryFluidTransfer;
import com.yogpc.qp.integration.QuarryItemTransfer;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QPItem;
import com.yogpc.qp.machines.advpump.BlockAdvPump;
import com.yogpc.qp.machines.advpump.TileAdvPump;
import com.yogpc.qp.machines.advquarry.AdvQuarryMenu;
import com.yogpc.qp.machines.advquarry.BlockAdvQuarry;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.checker.ItemChecker;
import com.yogpc.qp.machines.marker.BlockExMarker;
import com.yogpc.qp.machines.marker.BlockMarker;
import com.yogpc.qp.machines.marker.ContainerMarker;
import com.yogpc.qp.machines.marker.Tile16Marker;
import com.yogpc.qp.machines.marker.TileFlexMarker;
import com.yogpc.qp.machines.marker.TileMarker;
import com.yogpc.qp.machines.misc.BlockDummy;
import com.yogpc.qp.machines.misc.CreativeGeneratorBlock;
import com.yogpc.qp.machines.misc.CreativeGeneratorTile;
import com.yogpc.qp.machines.misc.YSetterContainer;
import com.yogpc.qp.machines.misc.YSetterItem;
import com.yogpc.qp.machines.placer.PlacerBlock;
import com.yogpc.qp.machines.placer.PlacerContainer;
import com.yogpc.qp.machines.placer.PlacerTile;
import com.yogpc.qp.machines.quarry.BlockFrame;
import com.yogpc.qp.machines.quarry.BlockQuarry;
import com.yogpc.qp.machines.quarry.QuarryLootFunction;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.recipe.QuarryBedrockModuleRecipe;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class QuarryPlus implements ModInitializer {
    public static final String modID = "quarryplus";
    public static final String MOD_NAME = "QuarryPlus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final CreativeModeTab CREATIVE_TAB = FabricItemGroupBuilder.build(
        new ResourceLocation(modID, modID), () -> new ItemStack(ModObjects.BLOCK_QUARRY)
    );
    public static QuarryConfig config = null;

    @Override
    public void onInitialize() {
        QuarryPlus.LOGGER.info("Common init is called. {} ", QuarryPlus.modID);
        AutoConfig.register(QuarryConfig.class, Toml4jConfigSerializer::new);
        QuarryPlus.config = AutoConfig.getConfigHolder(QuarryConfig.class).getConfig();

        register(ModObjects.BLOCK_QUARRY, ModObjects.QUARRY_TYPE);
        register(ModObjects.BLOCK_FRAME, null);
        Registry.register(Registry.ITEM, ModObjects.ITEM_CHECKER.getRegistryName(), ModObjects.ITEM_CHECKER);
        register(ModObjects.BLOCK_MARKER, ModObjects.MARKER_TYPE);
        Registry.register(Registry.ITEM, ModObjects.ITEM_Y_SETTER.getRegistryName(), ModObjects.ITEM_Y_SETTER);
        Registry.register(Registry.ITEM, ModObjects.ITEM_BEDROCK_MODULE.getRegistryName(), ModObjects.ITEM_BEDROCK_MODULE);
        register(ModObjects.BLOCK_FLEX_MARKER, ModObjects.FLEX_MARKER_TYPE);
        register(ModObjects.BLOCK_16_MARKER, ModObjects.MARKER_16_TYPE);
        register(ModObjects.BLOCK_CREATIVE_GENERATOR, ModObjects.CREATIVE_GENERATOR_TYPE);
        register(ModObjects.BLOCK_ADV_PUMP, ModObjects.ADV_PUMP_TYPE);
        register(ModObjects.BLOCK_PLACER, ModObjects.PLACER_TYPE);
        register(ModObjects.BLOCK_ADV_QUARRY, ModObjects.ADV_QUARRY_TYPE);
        Registry.register(Registry.BLOCK, new ResourceLocation(modID, BlockDummy.NAME), ModObjects.BLOCK_DUMMY);
        Registry.register(Registry.ITEM, new ResourceLocation(modID, BlockDummy.NAME), ModObjects.BLOCK_DUMMY.blockItem);

        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(modID, "drop_function"), ModObjects.ENCHANTED_LOOT_TYPE);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(modID, "drop_function_quarry"), ModObjects.QUARRY_LOOT_TYPE);

        Registry.register(Registry.RECIPE_SERIALIZER, QuarryBedrockModuleRecipe.NAME, QuarryBedrockModuleRecipe.SERIALIZER);

        PacketHandler.Server.initServer();
        EnergyIntegration.register();
        QuarryFluidTransfer.register();
        QuarryItemTransfer.register();
    }

    private static void register(QPBlock block, @Nullable BlockEntityType<?> entityType) {
        Registry.register(Registry.BLOCK, block.getRegistryName(), block);
        Registry.register(Registry.ITEM, block.getRegistryName(), block.blockItem);
        if (entityType != null) {
            if (entityType.isValid(block.defaultBlockState())) {
                Registry.register(Registry.BLOCK_ENTITY_TYPE, block.getRegistryName(), entityType);
            } else {
                throw new IllegalArgumentException("Invalid block entity(%s) for %s".formatted(entityType, block));
            }
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    public static class ModObjects {
        public static final BlockQuarry BLOCK_QUARRY = new BlockQuarry();
        public static final BlockEntityType<TileQuarry> QUARRY_TYPE = FabricBlockEntityTypeBuilder.create(TileQuarry::new, BLOCK_QUARRY).build(DSL.emptyPartType());

        public static final BlockFrame BLOCK_FRAME = new BlockFrame();
        public static final ItemChecker ITEM_CHECKER = new ItemChecker();

        public static final BlockMarker BLOCK_MARKER = new BlockMarker();
        public static final BlockEntityType<TileMarker> MARKER_TYPE = FabricBlockEntityTypeBuilder.create(TileMarker::new, BLOCK_MARKER).build(DSL.emptyPartType());

        public static final YSetterItem ITEM_Y_SETTER = new YSetterItem();
        public static final MenuType<YSetterContainer> Y_SETTER_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(YSetterContainer.GUI_ID,
            (syncId, inventory, buf) -> new YSetterContainer(syncId, inventory.player, buf.readBlockPos()));

        public static final QPItem ITEM_BEDROCK_MODULE = new QPItem(new QPItem.Properties().tab(QuarryPlus.CREATIVE_TAB), "remove_bedrock_module");

        public static final PlacerBlock BLOCK_PLACER = new PlacerBlock();
        public static final BlockEntityType<PlacerTile> PLACER_TYPE = FabricBlockEntityTypeBuilder.create(PlacerTile::new, BLOCK_PLACER).build(DSL.emptyPartType());
        public static final MenuType<PlacerContainer> PLACER_MENU_TYPE = ScreenHandlerRegistry.registerExtended(new ResourceLocation(PlacerContainer.GUI_ID),
            (syncId, inventory, buf) -> new PlacerContainer(syncId, inventory.player, buf.readBlockPos()));

        public static final BlockAdvQuarry BLOCK_ADV_QUARRY = new BlockAdvQuarry();
        public static final BlockEntityType<TileAdvQuarry> ADV_QUARRY_TYPE = FabricBlockEntityTypeBuilder.create(TileAdvQuarry::new, BLOCK_ADV_QUARRY).build(DSL.emptyPartType());
        public static final MenuType<AdvQuarryMenu> ADV_QUARRY_MENU_TYPE = ScreenHandlerRegistry.registerExtended(new ResourceLocation(AdvQuarryMenu.GUI_ID),
            (syncId, inventory, buf) -> new AdvQuarryMenu(syncId, inventory.player, buf.readBlockPos()));

        public static final LootItemFunctionType ENCHANTED_LOOT_TYPE = new LootItemFunctionType(EnchantedLootFunction.SERIALIZER);
        public static final LootItemFunctionType QUARRY_LOOT_TYPE = new LootItemFunctionType(QuarryLootFunction.SERIALIZER);

        public static final BlockExMarker.BlockFlexMarker BLOCK_FLEX_MARKER = new BlockExMarker.BlockFlexMarker();
        public static final BlockExMarker.Block16Marker BLOCK_16_MARKER = new BlockExMarker.Block16Marker();
        public static final BlockEntityType<TileFlexMarker> FLEX_MARKER_TYPE = FabricBlockEntityTypeBuilder.create(TileFlexMarker::new, BLOCK_FLEX_MARKER).build(DSL.emptyPartType());
        public static final BlockEntityType<Tile16Marker> MARKER_16_TYPE = FabricBlockEntityTypeBuilder.create(Tile16Marker::new, BLOCK_16_MARKER).build(DSL.emptyPartType());
        public static final MenuType<ContainerMarker> FLEX_MARKER_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(new ResourceLocation(BlockExMarker.GUI_FLEX_ID),
            (syncId, inventory, buf) -> new ContainerMarker(syncId, inventory.player, buf.readBlockPos(), ModObjects.FLEX_MARKER_HANDLER_TYPE));
        public static final MenuType<ContainerMarker> MARKER_16_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(new ResourceLocation(BlockExMarker.GUI_16_ID),
            (syncId, inventory, buf) -> new ContainerMarker(syncId, inventory.player, buf.readBlockPos(), ModObjects.MARKER_16_HANDLER_TYPE));

        public static final CreativeGeneratorBlock BLOCK_CREATIVE_GENERATOR = new CreativeGeneratorBlock();
        public static final BlockEntityType<CreativeGeneratorTile> CREATIVE_GENERATOR_TYPE = FabricBlockEntityTypeBuilder.create(CreativeGeneratorTile::new, BLOCK_CREATIVE_GENERATOR).build(DSL.emptyPartType());

        public static final BlockAdvPump BLOCK_ADV_PUMP = new BlockAdvPump();
        public static final BlockEntityType<TileAdvPump> ADV_PUMP_TYPE = FabricBlockEntityTypeBuilder.create(TileAdvPump::new, BLOCK_ADV_PUMP).build(DSL.emptyPartType());
        public static final BlockDummy BLOCK_DUMMY = new BlockDummy();
    }
}
