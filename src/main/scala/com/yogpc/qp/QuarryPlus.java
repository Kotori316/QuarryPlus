package com.yogpc.qp;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.integration.EnergyIntegration;
import com.yogpc.qp.integration.QuarryFluidTransfer;
import com.yogpc.qp.integration.QuarryItemTransfer;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.advpump.BlockAdvPump;
import com.yogpc.qp.machines.advpump.TileAdvPump;
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
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QuarryPlus implements ModInitializer {
    public static final String modID = "quarryplus";
    public static final String MOD_NAME = "QuarryPlus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final ItemGroup CREATIVE_TAB = FabricItemGroupBuilder.build(
        new Identifier(modID, modID), () -> new ItemStack(ModObjects.BLOCK_QUARRY)
    );
    public static QuarryConfig config = null;

    @Override
    public void onInitialize() {
        QuarryPlus.LOGGER.info("Common init is called. {} ", QuarryPlus.modID);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockQuarry.NAME), ModObjects.BLOCK_QUARRY);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockQuarry.NAME), ModObjects.BLOCK_QUARRY.blockItem);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, BlockQuarry.NAME), ModObjects.QUARRY_TYPE);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockFrame.NAME), ModObjects.BLOCK_FRAME);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockFrame.NAME), ModObjects.BLOCK_FRAME.blockItem);
        Registry.register(Registry.ITEM, new Identifier(modID, ItemChecker.NAME), ModObjects.ITEM_CHECKER);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockMarker.NAME), ModObjects.BLOCK_MARKER);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockMarker.NAME), ModObjects.BLOCK_MARKER.blockItem);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, BlockMarker.NAME), ModObjects.MARKER_TYPE);
        Registry.register(Registry.ITEM, new Identifier(modID, YSetterItem.NAME), ModObjects.ITEM_Y_SETTER);
        Registry.register(Registry.ITEM, new Identifier(modID, "remove_bedrock_module"), ModObjects.ITEM_BEDROCK_MODULE);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockExMarker.BlockFlexMarker.NAME), ModObjects.BLOCK_FLEX_MARKER);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockExMarker.BlockFlexMarker.NAME), ModObjects.BLOCK_FLEX_MARKER.blockItem);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, BlockExMarker.BlockFlexMarker.NAME), ModObjects.FLEX_MARKER_TYPE);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockExMarker.Block16Marker.NAME), ModObjects.BLOCK_16_MARKER);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockExMarker.Block16Marker.NAME), ModObjects.BLOCK_16_MARKER.blockItem);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, BlockExMarker.Block16Marker.NAME), ModObjects.MARKER_16_TYPE);
        Registry.register(Registry.BLOCK, new Identifier(modID, CreativeGeneratorBlock.NAME), ModObjects.BLOCK_CREATIVE_GENERATOR);
        Registry.register(Registry.ITEM, new Identifier(modID, CreativeGeneratorBlock.NAME), ModObjects.BLOCK_CREATIVE_GENERATOR.blockItem);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, CreativeGeneratorBlock.NAME), ModObjects.CREATIVE_GENERATOR_TYPE);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockAdvPump.NAME), ModObjects.BLOCK_ADV_PUMP);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockAdvPump.NAME), ModObjects.BLOCK_ADV_PUMP.blockItem);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, BlockAdvPump.NAME), ModObjects.ADV_PUMP_TYPE);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockDummy.NAME), ModObjects.BLOCK_DUMMY);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockDummy.NAME), ModObjects.BLOCK_DUMMY.blockItem);

        Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier(modID, "drop_function"), ModObjects.ENCHANTED_LOOT_TYPE);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier(modID, "drop_function_quarry"), ModObjects.QUARRY_LOOT_TYPE);

        Registry.register(Registry.RECIPE_SERIALIZER, QuarryBedrockModuleRecipe.NAME, QuarryBedrockModuleRecipe.SERIALIZER);

        PacketHandler.Server.initServer();
        EnergyIntegration.register();
        QuarryFluidTransfer.register();
        QuarryItemTransfer.register();
        AutoConfig.register(QuarryConfig.class, Toml4jConfigSerializer::new);
        QuarryPlus.config = AutoConfig.getConfigHolder(QuarryConfig.class).getConfig();
    }

    public static class ModObjects {
        public static final BlockQuarry BLOCK_QUARRY = new BlockQuarry();
        public static final BlockEntityType<TileQuarry> QUARRY_TYPE = FabricBlockEntityTypeBuilder.create(TileQuarry::new, BLOCK_QUARRY).build(DSL.emptyPartType());

        public static final BlockFrame BLOCK_FRAME = new BlockFrame();
        public static final ItemChecker ITEM_CHECKER = new ItemChecker();

        public static final BlockMarker BLOCK_MARKER = new BlockMarker();
        public static final BlockEntityType<TileMarker> MARKER_TYPE = FabricBlockEntityTypeBuilder.create(TileMarker::new, BLOCK_MARKER).build(DSL.emptyPartType());

        public static final YSetterItem ITEM_Y_SETTER = new YSetterItem();
        public static final ScreenHandlerType<YSetterContainer> Y_SETTER_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(YSetterContainer.GUI_ID,
            (syncId, inventory, buf) -> new YSetterContainer(syncId, inventory.player, buf.readBlockPos()));

        public static final Item ITEM_BEDROCK_MODULE = new Item(new Item.Settings().group(QuarryPlus.CREATIVE_TAB));

        public static final LootFunctionType ENCHANTED_LOOT_TYPE = new LootFunctionType(EnchantedLootFunction.SERIALIZER);
        public static final LootFunctionType QUARRY_LOOT_TYPE = new LootFunctionType(QuarryLootFunction.SERIALIZER);

        public static final BlockExMarker.BlockFlexMarker BLOCK_FLEX_MARKER = new BlockExMarker.BlockFlexMarker();
        public static final BlockExMarker.Block16Marker BLOCK_16_MARKER = new BlockExMarker.Block16Marker();
        public static final BlockEntityType<TileFlexMarker> FLEX_MARKER_TYPE = FabricBlockEntityTypeBuilder.create(TileFlexMarker::new, BLOCK_FLEX_MARKER).build(DSL.emptyPartType());
        public static final BlockEntityType<Tile16Marker> MARKER_16_TYPE = FabricBlockEntityTypeBuilder.create(Tile16Marker::new, BLOCK_16_MARKER).build(DSL.emptyPartType());
        public static final ScreenHandlerType<ContainerMarker> FLEX_MARKER_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(new Identifier(BlockExMarker.GUI_FLEX_ID),
            (syncId, inventory, buf) -> new ContainerMarker(syncId, inventory.player, buf.readBlockPos(), ModObjects.FLEX_MARKER_HANDLER_TYPE));
        public static final ScreenHandlerType<ContainerMarker> MARKER_16_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(new Identifier(BlockExMarker.GUI_16_ID),
            (syncId, inventory, buf) -> new ContainerMarker(syncId, inventory.player, buf.readBlockPos(), ModObjects.MARKER_16_HANDLER_TYPE));

        public static final CreativeGeneratorBlock BLOCK_CREATIVE_GENERATOR = new CreativeGeneratorBlock();
        public static final BlockEntityType<CreativeGeneratorTile> CREATIVE_GENERATOR_TYPE = FabricBlockEntityTypeBuilder.create(CreativeGeneratorTile::new, BLOCK_CREATIVE_GENERATOR).build(DSL.emptyPartType());

        public static final BlockAdvPump BLOCK_ADV_PUMP = new BlockAdvPump();
        public static final BlockEntityType<TileAdvPump> ADV_PUMP_TYPE = FabricBlockEntityTypeBuilder.create(TileAdvPump::new, BLOCK_ADV_PUMP).build(DSL.emptyPartType());
        public static final BlockDummy BLOCK_DUMMY = new BlockDummy();
    }
}
