package com.yogpc.qp.fabric;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.*;
import com.yogpc.qp.config.ConfigHolder;
import com.yogpc.qp.config.EnableMap;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.fabric.machine.advquarry.AdvQuarryBlockFabric;
import com.yogpc.qp.fabric.machine.advquarry.AdvQuarryEntityFabric;
import com.yogpc.qp.fabric.machine.misc.CheckerItemFabric;
import com.yogpc.qp.fabric.machine.misc.YSetterItemFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryBlockFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryEntityFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryMenuFabric;
import com.yogpc.qp.fabric.packet.PacketHandler;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.MachineLootFunction;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpItem;
import com.yogpc.qp.machine.advquarry.AdvQuarryBlock;
import com.yogpc.qp.machine.advquarry.AdvQuarryContainer;
import com.yogpc.qp.machine.exp.ExpModuleItem;
import com.yogpc.qp.machine.marker.*;
import com.yogpc.qp.machine.misc.*;
import com.yogpc.qp.machine.module.*;
import com.yogpc.qp.machine.mover.MoverBlock;
import com.yogpc.qp.machine.mover.MoverContainer;
import com.yogpc.qp.machine.mover.MoverEntity;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.storage.DebugStorageBlock;
import com.yogpc.qp.machine.storage.DebugStorageContainer;
import com.yogpc.qp.machine.storage.DebugStorageEntity;
import com.yogpc.qp.recipe.InstallBedrockModuleRecipe;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.apache.logging.log4j.util.Lazy;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessFabric implements PlatformAccess, ServerLifecycleEvents.ServerStopped {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsFabric::new);
    private final Lazy<PacketHandler> packetHandlerLazy = Lazy.lazy(PacketHandler::new);
    private final Lazy<TransferFabric> transferLazy = Lazy.lazy(TransferFabric::new);
    private final ConfigHolder configLazy = new ConfigHolder(this::modified);

    public static final class RegisterObjectsFabric implements RegisterObjects {
        public static final QuarryBlockFabric QUARRY_BLOCK = new QuarryBlockFabric();
        public static final BlockEntityType<QuarryEntityFabric> QUARRY_ENTITY_TYPE = BlockEntityType.Builder.of(QuarryEntityFabric::new, QUARRY_BLOCK).build(DSL.emptyPartType());
        public static final MenuType<QuarryMenuFabric> QUARRY_MENU = new ExtendedScreenHandlerType<>(QuarryMenuFabric::new, BlockPos.STREAM_CODEC);
        public static final AdvQuarryBlockFabric ADV_QUARRY_BLOCK = new AdvQuarryBlockFabric();
        public static final BlockEntityType<AdvQuarryEntityFabric> ADV_QUARRY_ENTITY_TYPE = BlockEntityType.Builder.of(AdvQuarryEntityFabric::new, ADV_QUARRY_BLOCK).build(DSL.emptyPartType());
        public static final MenuType<AdvQuarryContainer> ADV_QUARRY_MENU = new ExtendedScreenHandlerType<>(AdvQuarryContainer::new, BlockPos.STREAM_CODEC);
        public static final FrameBlock FRAME_BLOCK = new FrameBlock();
        public static final SoftBlock SOFT_BLOCK = new SoftBlock();
        public static final GeneratorBlock GENERATOR_BLOCK = new GeneratorBlock();
        public static final BlockEntityType<GeneratorEntity> GENERATOR_ENTITY_TYPE = BlockEntityType.Builder.of(GeneratorEntity::new, GENERATOR_BLOCK).build(DSL.emptyPartType());
        public static final CheckerItemFabric CHECKER_ITEM = new CheckerItemFabric();
        public static final NormalMarkerBlock MARKER_BLOCK = new NormalMarkerBlock();
        public static final BlockEntityType<NormalMarkerEntity> MARKER_ENTITY_TYPE = BlockEntityType.Builder.of(NormalMarkerEntity::new, MARKER_BLOCK).build(DSL.emptyPartType());
        public static final YSetterItemFabric Y_SET_ITEM = new YSetterItemFabric();
        public static final MenuType<YSetterContainer> Y_SET_MENU = new ExtendedScreenHandlerType<>(YSetterContainer::new, BlockPos.STREAM_CODEC);
        public static final MoverBlock MOVER_BLOCK = new MoverBlock();
        public static final BlockEntityType<MoverEntity> MOVER_ENTITY_TYPE = BlockEntityType.Builder.of(MoverEntity::new, MOVER_BLOCK).build(DSL.emptyPartType());
        public static final MenuType<MoverContainer> MOVER_MENU = new ExtendedScreenHandlerType<>(MoverContainer::new, BlockPos.STREAM_CODEC);
        public static final PumpModuleItem PUMP_MODULE_ITEM = new PumpModuleItem();
        public static final MenuType<ModuleContainer> MODULE_MENU = new ExtendedScreenHandlerType<>(ModuleContainer::new, BlockPos.STREAM_CODEC);
        public static final BedrockModuleItem BEDROCK_MODULE_ITEM = new BedrockModuleItem();
        public static final ExpModuleItem EXP_MODULE_ITEM = new ExpModuleItem();
        public static final FlexibleMarkerBlock FLEXIBLE_MARKER_BLOCK = new FlexibleMarkerBlock();
        public static final BlockEntityType<FlexibleMarkerEntity> FLEXIBLE_MARKER_ENTITY_TYPE = BlockEntityType.Builder.of(FlexibleMarkerEntity::new, FLEXIBLE_MARKER_BLOCK).build(DSL.emptyPartType());
        public static final ChunkMarkerBlock CHUNK_MARKER_BLOCK = new ChunkMarkerBlock();
        public static final BlockEntityType<ChunkMarkerEntity> CHUNK_MARKER_ENTITY_TYPE = BlockEntityType.Builder.of(ChunkMarkerEntity::new, CHUNK_MARKER_BLOCK).build(DSL.emptyPartType());
        public static final MenuType<MarkerContainer> FLEXIBLE_MARKER_MENU = new ExtendedScreenHandlerType<>(MarkerContainer::createFlexibleMarkerContainer, BlockPos.STREAM_CODEC);
        public static final MenuType<MarkerContainer> CHUNK_MARKER_MENU = new ExtendedScreenHandlerType<>(MarkerContainer::createChunkMarkerContainer, BlockPos.STREAM_CODEC);
        public static final RepeatTickModuleItem REPEAT_TICK_MODULE_ITEM = new RepeatTickModuleItem();
        public static final DebugStorageBlock DEBUG_STORAGE_BLOCK = new DebugStorageBlock();
        public static final BlockEntityType<DebugStorageEntity> DEBUG_STORAGE_TYPE = BlockEntityType.Builder.of(DebugStorageEntity::new, DEBUG_STORAGE_BLOCK).build(DSL.emptyPartType());
        public static final MenuType<DebugStorageContainer> DEBUG_STORAGE_MENU = new ExtendedScreenHandlerType<>(DebugStorageContainer::new, BlockPos.STREAM_CODEC);
        public static final FilterModuleItem FILTER_MODULE_ITEM = new FilterModuleItem();
        public static final MenuType<FilterModuleContainer> FILTER_MODULE_MENU = new ExtendedScreenHandlerType<>((i, inventory, pos) -> new FilterModuleContainer(i, inventory, inventory.getSelected()), BlockPos.STREAM_CODEC);

        public static final LootItemFunctionType<MachineLootFunction> MACHINE_LOOT_FUNCTION = new LootItemFunctionType<>(MachineLootFunction.SERIALIZER);

        private static final List<InCreativeTabs> TAB_ITEMS = new ArrayList<>();
        public static final CreativeModeTab TAB = QuarryPlus.buildCreativeModeTab(FabricItemGroup.builder()).build();
        private static final Map<Class<? extends QpBlock>, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashMap<>();
        private static final Map<String, EnableMap.EnableOrNot> ENABLE_MAP = new HashMap<>();

        public static BlockEntityType<?>[] entityTypes() {
            return BLOCK_ENTITY_TYPES.values().toArray(new BlockEntityType[0]);
        }

        static void registerAll() {
            // Machine
            registerEntityBlock(QUARRY_BLOCK, QUARRY_ENTITY_TYPE, EnableMap.EnableOrNot.CONFIG_ON);
            registerEntityBlock(ADV_QUARRY_BLOCK, ADV_QUARRY_ENTITY_TYPE, EnableMap.EnableOrNot.CONFIG_ON);
            registerEntityBlock(GENERATOR_BLOCK, GENERATOR_ENTITY_TYPE, EnableMap.EnableOrNot.ALWAYS_ON);
            registerEntityBlock(MOVER_BLOCK, MOVER_ENTITY_TYPE, EnableMap.EnableOrNot.CONFIG_ON);
            // Marker
            registerEntityBlock(MARKER_BLOCK, MARKER_ENTITY_TYPE, EnableMap.EnableOrNot.ALWAYS_ON);
            registerEntityBlock(FLEXIBLE_MARKER_BLOCK, FLEXIBLE_MARKER_ENTITY_TYPE, EnableMap.EnableOrNot.CONFIG_ON);
            registerEntityBlock(CHUNK_MARKER_BLOCK, CHUNK_MARKER_ENTITY_TYPE, EnableMap.EnableOrNot.CONFIG_ON);
            // Module
            registerItem(PUMP_MODULE_ITEM, EnableMap.EnableOrNot.ALWAYS_OFF);
            registerItem(BEDROCK_MODULE_ITEM, EnableMap.EnableOrNot.CONFIG_OFF);
            registerItem(EXP_MODULE_ITEM, EnableMap.EnableOrNot.ALWAYS_OFF);
            registerItem(REPEAT_TICK_MODULE_ITEM, EnableMap.EnableOrNot.ALWAYS_OFF);
            registerItem(FILTER_MODULE_ITEM, EnableMap.EnableOrNot.ALWAYS_OFF);
            // Misc
            registerItem(CHECKER_ITEM, EnableMap.EnableOrNot.ALWAYS_ON);
            registerItem(Y_SET_ITEM, EnableMap.EnableOrNot.ALWAYS_ON);
            registerBlockItem(FRAME_BLOCK);
            registerBlockItem(SOFT_BLOCK, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, SoftBlock.NAME), softBlock -> softBlock.blockItem);
            registerEntityBlock(DEBUG_STORAGE_BLOCK, DEBUG_STORAGE_TYPE, EnableMap.EnableOrNot.ALWAYS_ON);
            Registry.register(BuiltInRegistries.MENU, QuarryMenuFabric.GUI_ID, QUARRY_MENU);
            Registry.register(BuiltInRegistries.MENU, YSetterContainer.GUI_ID, Y_SET_MENU);
            Registry.register(BuiltInRegistries.MENU, MoverContainer.GUI_ID, MOVER_MENU);
            Registry.register(BuiltInRegistries.MENU, ModuleContainer.GUI_ID, MODULE_MENU);
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, MarkerContainer.FLEXIBLE_NAME), FLEXIBLE_MARKER_MENU);
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, MarkerContainer.CHUNK_NAME), CHUNK_MARKER_MENU);
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, DebugStorageContainer.NAME), DEBUG_STORAGE_MENU);
            Registry.register(BuiltInRegistries.MENU, AdvQuarryContainer.GUI_ID, ADV_QUARRY_MENU);
            Registry.register(BuiltInRegistries.MENU, FilterModuleContainer.GUI_ID, FILTER_MODULE_MENU);
            Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, MachineLootFunction.NAME), MACHINE_LOOT_FUNCTION);
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, QuarryPlus.modID), TAB);
            for (Map.Entry<ResourceLocation, DataComponentType<?>> entry : QuarryDataComponents.ALL.entrySet()) {
                Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, entry.getKey(), entry.getValue());
            }
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, InstallBedrockModuleRecipe.NAME), InstallBedrockModuleRecipe.SERIALIZER);
        }

        private static void registerEntityBlock(QpBlock block, BlockEntityType<?> entityType, EnableMap.EnableOrNot enable) {
            if (!entityType.isValid(block.defaultBlockState())) {
                throw new IllegalArgumentException("Invalid block entity type (%s) for %s".formatted(entityType, block.getClass().getSimpleName()));
            }
            registerBlockItem(block);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, block.name, entityType);
            BLOCK_ENTITY_TYPES.put(block.getClass(), entityType);
            ENABLE_MAP.put(block.name.getPath(), enable);
        }

        private static void registerBlockItem(QpBlock block) {
            registerBlockItem(block, block.name, b -> b.blockItem);
        }

        private static <T extends Block & InCreativeTabs> void registerBlockItem(T block, ResourceLocation name, Function<T, ? extends BlockItem> itemGetter) {
            Registry.register(BuiltInRegistries.BLOCK, name, block);
            registerItem(itemGetter.apply(block), name);
            TAB_ITEMS.add(block);
        }

        private static void registerItem(QpItem item, EnableMap.EnableOrNot enable) {
            registerItem(item, item.name);
            ENABLE_MAP.put(item.name.getPath(), enable);
        }

        private static void registerItem(Item item, ResourceLocation name) {
            Registry.register(BuiltInRegistries.ITEM, name, item);
            if (item instanceof InCreativeTabs c) {
                TAB_ITEMS.add(c);
            }
            if (item instanceof BlockItem blockItem) {
                blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
            }
        }

        @Override
        public Supplier<? extends QuarryBlock> quarryBlock() {
            return Lazy.value(QUARRY_BLOCK);
        }

        @Override
        public Supplier<? extends FrameBlock> frameBlock() {
            return Lazy.value(FRAME_BLOCK);
        }

        @Override
        public Supplier<? extends GeneratorBlock> generatorBlock() {
            return Lazy.value(GENERATOR_BLOCK);
        }

        @Override
        public Supplier<? extends NormalMarkerBlock> markerBlock() {
            return Lazy.value(MARKER_BLOCK);
        }

        @Override
        public Supplier<? extends MoverBlock> moverBlock() {
            return Lazy.value(MOVER_BLOCK);
        }

        @Override
        public Supplier<? extends FlexibleMarkerBlock> flexibleMarkerBlock() {
            return Lazy.value(FLEXIBLE_MARKER_BLOCK);
        }

        @Override
        public Supplier<? extends ChunkMarkerBlock> chunkMarkerBlock() {
            return Lazy.value(CHUNK_MARKER_BLOCK);
        }

        @Override
        public Supplier<? extends DebugStorageBlock> debugStorageBlock() {
            return Lazy.value(DEBUG_STORAGE_BLOCK);
        }

        @Override
        public Supplier<? extends AdvQuarryBlock> advQuarryBlock() {
            return Lazy.value(ADV_QUARRY_BLOCK);
        }

        @Override
        public Supplier<? extends SoftBlock> softBlock() {
            return Lazy.value(SOFT_BLOCK);
        }

        @Override
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            var t = BLOCK_ENTITY_TYPES.get(block.getClass());
            if (t == null) {
                QuarryPlus.LOGGER.warn("Unknown block type: {}", block.name);
                return Optional.empty();
            }
            return Optional.of(t);
        }

        @Override
        public Map<String, EnableMap.EnableOrNot> defaultEnableSetting() {
            return ENABLE_MAP;
        }

        @Override
        public Supplier<? extends BedrockModuleItem> bedrockModuleItem() {
            return Lazy.value(BEDROCK_MODULE_ITEM);
        }

        @Override
        public Stream<Supplier<? extends InCreativeTabs>> allItems() {
            return TAB_ITEMS.stream().map(t -> () -> t);
        }

        @Override
        public Supplier<MenuType<? extends YSetterContainer>> ySetterContainer() {
            return Lazy.value(Y_SET_MENU);
        }

        @Override
        public Supplier<MenuType<? extends MoverContainer>> moverContainer() {
            return Lazy.value(MOVER_MENU);
        }

        @Override
        public Supplier<MenuType<? extends ModuleContainer>> moduleContainer() {
            return Lazy.value(MODULE_MENU);
        }

        @Override
        public Supplier<MenuType<? extends MarkerContainer>> flexibleMarkerContainer() {
            return Lazy.value(FLEXIBLE_MARKER_MENU);
        }

        @Override
        public Supplier<MenuType<? extends MarkerContainer>> chunkMarkerContainer() {
            return Lazy.value(CHUNK_MARKER_MENU);
        }

        @Override
        public Supplier<MenuType<? extends DebugStorageContainer>> debugStorageContainer() {
            return Lazy.value(DEBUG_STORAGE_MENU);
        }

        @Override
        public Supplier<MenuType<? extends AdvQuarryContainer>> advQuarryContainer() {
            return Lazy.value(ADV_QUARRY_MENU);
        }

        @Override
        public Supplier<MenuType<? extends FilterModuleContainer>> filterModuleContainer() {
            return Lazy.value(FILTER_MODULE_MENU);
        }

        @Override
        public Supplier<LootItemFunctionType<? extends MachineLootFunction>> machineLootFunction() {
            return Lazy.value(MACHINE_LOOT_FUNCTION);
        }
    }

    @Override
    public String platformName() {
        return "Fabric";
    }

    @Override
    public RegisterObjects registerObjects() {
        return itemsLazy.get();
    }

    @Override
    public Packet packetHandler() {
        return packetHandlerLazy.get();
    }

    @Override
    public Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("%s.toml".formatted(QuarryPlus.modID));
    }

    @Override
    public boolean isInDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Supplier<? extends QuarryConfig> getConfig() {
        return configLazy;
    }

    @Override
    public Transfer transfer() {
        return transferLazy.get();
    }

    @Override
    public FluidStackLike getFluidInItem(ItemStack stack) {
        var storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage == null) {
            return FluidStackLike.EMPTY;
        }
        var extracted = StorageUtil.findExtractableContent(storage, null);
        if (extracted == null) {
            return FluidStackLike.EMPTY;
        }
        return new FluidStackLike(extracted.resource().getFluid(), extracted.amount(), extracted.resource().getComponents());
    }

    @Override
    public Component getFluidName(FluidStackLike stack) {
        return FluidVariantAttributes.getName(FluidVariant.of(stack.fluid(), stack.patch()));
    }

    @Override
    public <T extends AbstractContainerMenu> void openGui(ServerPlayer player, GeneralScreenHandler<T> handler) {
        player.openMenu(new ExtendedGeneralScreenHandler<>(handler));
    }

    @Override
    public void onServerStopped(MinecraftServer server) {
        configLazy.reset();
    }

    private QuarryConfig modified() {
        return QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment);
    }
}
