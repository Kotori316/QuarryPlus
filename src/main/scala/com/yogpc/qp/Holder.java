package com.yogpc.qp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.advpump.BlockAdvPump;
import com.yogpc.qp.machines.advpump.TileAdvPump;
import com.yogpc.qp.machines.checker.ItemChecker;
import com.yogpc.qp.machines.marker.BlockExMarker;
import com.yogpc.qp.machines.marker.BlockMarker;
import com.yogpc.qp.machines.marker.ContainerMarker;
import com.yogpc.qp.machines.marker.Tile16Marker;
import com.yogpc.qp.machines.marker.TileFlexMarker;
import com.yogpc.qp.machines.marker.TileMarker;
import com.yogpc.qp.machines.miningwell.MiningWellBlock;
import com.yogpc.qp.machines.miningwell.MiningWellTile;
import com.yogpc.qp.machines.misc.BlockDummy;
import com.yogpc.qp.machines.misc.CreativeGeneratorBlock;
import com.yogpc.qp.machines.misc.CreativeGeneratorTile;
import com.yogpc.qp.machines.misc.YSetterContainer;
import com.yogpc.qp.machines.misc.YSetterItem;
import com.yogpc.qp.machines.module.BedrockModuleItem;
import com.yogpc.qp.machines.module.ContainerQuarryModule;
import com.yogpc.qp.machines.module.EnergyModuleItem;
import com.yogpc.qp.machines.module.ExpModuleItem;
import com.yogpc.qp.machines.module.ExpPumpBlock;
import com.yogpc.qp.machines.module.ExpPumpTile;
import com.yogpc.qp.machines.module.ModuleLootFunction;
import com.yogpc.qp.machines.module.PumpModuleItem;
import com.yogpc.qp.machines.module.PumpPlusBlock;
import com.yogpc.qp.machines.module.ReplacerBlock;
import com.yogpc.qp.machines.module.ReplacerDummyBlock;
import com.yogpc.qp.machines.module.ReplacerModuleItem;
import com.yogpc.qp.machines.mover.BlockMover;
import com.yogpc.qp.machines.mover.ContainerMover;
import com.yogpc.qp.machines.placer.PlacerBlock;
import com.yogpc.qp.machines.placer.PlacerContainer;
import com.yogpc.qp.machines.placer.PlacerTile;
import com.yogpc.qp.machines.quarry.FrameBlock;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.QuarryLootFunction;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.machines.workbench.BlockWorkbench;
import com.yogpc.qp.machines.workbench.ContainerWorkbench;
import com.yogpc.qp.machines.workbench.TileWorkbench;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fmllegacy.network.IContainerFactory;

public class Holder {
    public static final CreativeModeTab TAB = new QuarryCreativeTab();
    private static final List<QPBlock> BLOCKS = new ArrayList<>();
    private static final List<Item> ITEMS = new ArrayList<>();
    private static final List<BlockEntityType<?>> ENTITY_TYPES = new ArrayList<>();
    private static final List<MenuType<?>> MENU_TYPES = new ArrayList<>();
    private static final List<EntryConditionHolder> CONDITION_HOLDERS = new ArrayList<>();

    private static <T extends QPBlock> T registerBlock(T block, EnableOrNot condition) {
        BLOCKS.add(block);
        CONDITION_HOLDERS.add(new EntryConditionHolder(block.getRegistryName(), condition));
        return block;
    }

    private static <T extends QPBlock & EntityBlock> T registerBlock(T block) {
        BLOCKS.add(block);
        return block;
    }

    private static <T extends Item> T registerItem(T item, EnableOrNot condition) {
        ITEMS.add(item);
        CONDITION_HOLDERS.add(new EntryConditionHolder(item.getRegistryName(), condition));
        return item;
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerEntityType(BlockEntityType.BlockEntitySupplier<T> supplier, Block block, EnableOrNot condition) {
        var type = BlockEntityType.Builder.of(supplier, block).build(DSL.emptyPartType());
        type.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        ENTITY_TYPES.add(type);
        CONDITION_HOLDERS.add(new EntryConditionHolder(block.getRegistryName(), condition));
        return type;
    }

    private static <T extends AbstractContainerMenu> MenuType<T> registerMenuType(IContainerFactory<T> factory, String guiId) {
        MenuType<T> type = IForgeContainerType.create(factory);
        type.setRegistryName(guiId);
        MENU_TYPES.add(type);
        return type;
    }

    public static List<Block> blocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    public static List<Item> items() {
        return Stream.concat(BLOCKS.stream().map(q -> q.blockItem), ITEMS.stream()).toList();
    }

    public static List<BlockEntityType<?>> entityTypes() {
        return Collections.unmodifiableList(ENTITY_TYPES);
    }

    public static List<EntryConditionHolder> conditionHolders() {
        return Collections.unmodifiableList(CONDITION_HOLDERS);
    }

    public static List<MenuType<?>> menuTypes() {
        return Collections.unmodifiableList(MENU_TYPES);
    }

    public static final QuarryBlock BLOCK_QUARRY = registerBlock(new QuarryBlock());
    public static final BlockWorkbench BLOCK_WORKBENCH = registerBlock(new BlockWorkbench());
    public static final MiningWellBlock BLOCK_MINING_WELL = registerBlock(new MiningWellBlock());
    public static final BlockMover BLOCK_MOVER = registerBlock(new BlockMover(), EnableOrNot.CONFIG_ON);
    public static final PumpPlusBlock BLOCK_PUMP = registerBlock(new PumpPlusBlock(), EnableOrNot.CONFIG_ON);
    public static final ReplacerBlock BLOCK_REPLACER = registerBlock(new ReplacerBlock(), EnableOrNot.CONFIG_OFF);
    public static final ExpPumpBlock BLOCK_EXP_PUMP = registerBlock(new ExpPumpBlock());
    public static final BlockAdvPump BLOCK_ADV_PUMP = registerBlock(new BlockAdvPump());
    public static final BlockMarker BLOCK_MARKER = registerBlock(new BlockMarker());
    public static final BlockExMarker.BlockFlexMarker BLOCK_FLEX_MARKER = registerBlock(new BlockExMarker.BlockFlexMarker());
    public static final BlockExMarker.Block16Marker BLOCK_16_MARKER = registerBlock(new BlockExMarker.Block16Marker());
    public static final PlacerBlock BLOCK_PLACER = registerBlock(new PlacerBlock());
    public static final FrameBlock BLOCK_FRAME = registerBlock(new FrameBlock(), EnableOrNot.ALWAYS_ON);
    public static final BlockDummy BLOCK_DUMMY = new BlockDummy();
    public static final ReplacerDummyBlock BLOCK_DUMMY_REPLACER = new ReplacerDummyBlock();
    public static final CreativeGeneratorBlock BLOCK_CREATIVE_GENERATOR = registerBlock(new CreativeGeneratorBlock());

    public static final ItemChecker ITEM_CHECKER = registerItem(new ItemChecker(), EnableOrNot.ALWAYS_ON);
    public static final YSetterItem ITEM_Y_SETTER = registerItem(new YSetterItem(), EnableOrNot.ALWAYS_ON);
    public static final PumpModuleItem ITEM_PUMP_MODULE = registerItem(new PumpModuleItem(), EnableOrNot.CONFIG_ON);
    public static final BedrockModuleItem ITEM_BEDROCK_MODULE = registerItem(new BedrockModuleItem(), EnableOrNot.CONFIG_OFF);
    public static final EnergyModuleItem ITEM_FUEL_MODULE_NORMAL = registerItem(new EnergyModuleItem(5, "fuel_module_normal"), EnableOrNot.CONFIG_ON);
    public static final ExpModuleItem ITEM_EXP_MODULE = registerItem(new ExpModuleItem(), EnableOrNot.CONFIG_ON);
    public static final ReplacerModuleItem ITEM_REPLACER_MODULE = registerItem(new ReplacerModuleItem(), EnableOrNot.CONFIG_OFF);

    public static final BlockEntityType<TileQuarry> QUARRY_TYPE = registerEntityType(TileQuarry::new, BLOCK_QUARRY, EnableOrNot.CONFIG_ON);
    public static final BlockEntityType<TileMarker> MARKER_TYPE = registerEntityType(TileMarker::new, BLOCK_MARKER, EnableOrNot.ALWAYS_ON);
    public static final BlockEntityType<TileFlexMarker> FLEX_MARKER_TYPE = registerEntityType(TileFlexMarker::new, BLOCK_FLEX_MARKER, EnableOrNot.ALWAYS_ON);
    public static final BlockEntityType<Tile16Marker> MARKER_16_TYPE = registerEntityType(Tile16Marker::new, BLOCK_16_MARKER, EnableOrNot.ALWAYS_ON);
    public static final BlockEntityType<CreativeGeneratorTile> CREATIVE_GENERATOR_TYPE = registerEntityType(CreativeGeneratorTile::new, BLOCK_CREATIVE_GENERATOR, EnableOrNot.ALWAYS_ON);
    public static final BlockEntityType<TileAdvPump> ADV_PUMP_TYPE = registerEntityType(TileAdvPump::new, BLOCK_ADV_PUMP, EnableOrNot.CONFIG_ON);
    public static final BlockEntityType<TileWorkbench> WORKBENCH_TYPE = registerEntityType(TileWorkbench::new, BLOCK_WORKBENCH, EnableOrNot.CONFIG_ON);
    public static final BlockEntityType<MiningWellTile> MINING_WELL_TYPE = registerEntityType(MiningWellTile::new, BLOCK_MINING_WELL, EnableOrNot.CONFIG_ON);
    public static final BlockEntityType<ExpPumpTile> EXP_PUMP_TYPE = registerEntityType(ExpPumpTile::new, BLOCK_EXP_PUMP, EnableOrNot.CONFIG_ON);
    public static final BlockEntityType<PlacerTile> PLACER_TYPE = registerEntityType(PlacerTile::new, BLOCK_PLACER, EnableOrNot.CONFIG_ON);

    public static final MenuType<ContainerMarker> FLEX_MARKER_MENU_TYPE = registerMenuType((windowId, inv, data) ->
        new ContainerMarker(windowId, inv.player, data.readBlockPos(), Holder.FLEX_MARKER_MENU_TYPE), BlockExMarker.GUI_FLEX_ID);
    public static final MenuType<ContainerMarker> MARKER_16_MENU_TYPE = registerMenuType((windowId, inv, data) ->
        new ContainerMarker(windowId, inv.player, data.readBlockPos(), Holder.MARKER_16_MENU_TYPE), BlockExMarker.GUI_16_ID);
    public static final MenuType<YSetterContainer> Y_SETTER_MENU_TYPE = registerMenuType((windowId, inv, data) ->
        new YSetterContainer(windowId, inv.player, data.readBlockPos()), YSetterContainer.GUI_ID);
    public static final MenuType<ContainerWorkbench> WORKBENCH_MENU_TYPE = registerMenuType((windowId, inv, data) ->
        new ContainerWorkbench(windowId, inv.player, data.readBlockPos()), BlockWorkbench.GUI_ID);
    public static final MenuType<ContainerMover> MOVER_MENU_TYPE = registerMenuType((windowId, inv, data) ->
        new ContainerMover(windowId, inv.player, data.readBlockPos()), BlockMover.GUI_ID);
    public static final MenuType<ContainerQuarryModule> MODULE_MENU_TYPE = registerMenuType((windowId, inv, data) ->
        new ContainerQuarryModule(windowId, inv.player, data.readBlockPos()), ContainerQuarryModule.GUI_ID);
    public static final MenuType<PlacerContainer> PLACER_MENU_TYPE = registerMenuType((windowId, inv, data) ->
        new PlacerContainer(windowId, inv.player, data.readBlockPos()), PlacerContainer.GUI_ID);

    public static final LootItemFunctionType ENCHANTED_LOOT_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE,
        new ResourceLocation(QuarryPlus.modID, EnchantedLootFunction.NAME), new LootItemFunctionType(EnchantedLootFunction.SERIALIZER));
    public static final LootItemFunctionType QUARRY_LOOT_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE,
        new ResourceLocation(QuarryPlus.modID, QuarryLootFunction.NAME), new LootItemFunctionType(QuarryLootFunction.SERIALIZER));
    public static final LootItemFunctionType MODULE_LOOT_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE,
        new ResourceLocation(QuarryPlus.modID, ModuleLootFunction.NAME), new LootItemFunctionType(ModuleLootFunction.SERIALIZER));

    public record EntryConditionHolder(ResourceLocation location, EnableOrNot condition) {
        boolean configurable() {
            return condition.configurable();
        }

        String path() {
            return location.getPath();
        }
    }

    public enum EnableOrNot {
        CONFIG_ON, CONFIG_OFF, ALWAYS_ON;

        public boolean configurable() {
            return this == CONFIG_ON || this == CONFIG_OFF;
        }

        public boolean on() {
            return this == CONFIG_ON || this == ALWAYS_ON;
        }
    }
}
