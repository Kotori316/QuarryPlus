package com.yogpc.qp.neoforge;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.*;
import com.yogpc.qp.config.ConfigHolder;
import com.yogpc.qp.config.EnableMap;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.MachineLootFunction;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.advquarry.AdvQuarryBlock;
import com.yogpc.qp.machine.exp.ExpModuleItem;
import com.yogpc.qp.machine.marker.*;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import com.yogpc.qp.machine.misc.GeneratorEntity;
import com.yogpc.qp.machine.misc.YSetterContainer;
import com.yogpc.qp.machine.module.BedrockModuleItem;
import com.yogpc.qp.machine.module.ModuleContainer;
import com.yogpc.qp.machine.module.PumpModuleItem;
import com.yogpc.qp.machine.module.RepeatTickModuleItem;
import com.yogpc.qp.machine.mover.MoverBlock;
import com.yogpc.qp.machine.mover.MoverContainer;
import com.yogpc.qp.machine.mover.MoverEntity;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.storage.DebugStorageBlock;
import com.yogpc.qp.machine.storage.DebugStorageContainer;
import com.yogpc.qp.machine.storage.DebugStorageEntity;
import com.yogpc.qp.neoforge.machine.advquarry.AdvQuarryEntityNeoForge;
import com.yogpc.qp.neoforge.machine.misc.CheckerItemNeoForge;
import com.yogpc.qp.neoforge.machine.misc.YSetterItemNeoForge;
import com.yogpc.qp.neoforge.machine.quarry.QuarryBlockNeoForge;
import com.yogpc.qp.neoforge.machine.quarry.QuarryEntityNeoForge;
import com.yogpc.qp.neoforge.packet.PacketHandler;
import com.yogpc.qp.recipe.InstallBedrockModuleRecipe;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.*;
import org.apache.logging.log4j.util.Lazy;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessNeoForge implements PlatformAccess {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsNeoForge::new);
    private final Lazy<PacketHandler> packetHandlerLazy = Lazy.lazy(PacketHandler::new);
    private final Lazy<TransferNeoForge> transferLazy = Lazy.lazy(TransferNeoForge::new);
    private final ConfigHolder configLazy = new ConfigHolder(() ->
        QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment)
    );

    public static class RegisterObjectsNeoForge implements PlatformAccess.RegisterObjects {
        private static final DeferredRegister.Blocks BLOCK_REGISTER = DeferredRegister.createBlocks(QuarryPlus.modID);
        private static final DeferredRegister.Items ITEM_REGISTER = DeferredRegister.createItems(QuarryPlus.modID);
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, QuarryPlus.modID);
        private static final DeferredRegister<IngredientType<?>> INGREDIENT_REGISTER = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, QuarryPlus.modID);
        private static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuarryPlus.modID);
        private static final DeferredRegister<LootItemFunctionType<?>> LOOT_TYPE_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(Registries.MENU, QuarryPlus.modID);
        static final List<DeferredRegister<?>> REGISTER_LIST = List.of(
            BLOCK_REGISTER, ITEM_REGISTER, BLOCK_ENTITY_REGISTER, RECIPE_REGISTER, INGREDIENT_REGISTER, CREATIVE_TAB_REGISTER, LOOT_TYPE_REGISTER, DATA_COMPONENT_TYPE_REGISTER, MENU_TYPE_REGISTER
        );
        private static final List<Supplier<? extends InCreativeTabs>> TAB_ITEMS = new ArrayList<>();
        private static final Map<String, EnableMap.EnableOrNot> ENABLE_MAP = new HashMap<>();

        // Machine
        public static final DeferredBlock<QuarryBlockNeoForge> BLOCK_QUARRY = registerBlock(QuarryBlockNeoForge.NAME, QuarryBlockNeoForge::new);
        public static final DeferredBlock<AdvQuarryBlock> BLOCK_ADV_QUARRY = registerBlock(AdvQuarryBlock.NAME, AdvQuarryBlock::new);
        public static final DeferredBlock<GeneratorBlock> BLOCK_GENERATOR = registerBlock(GeneratorBlock.NAME, GeneratorBlock::new);
        public static final DeferredBlock<MoverBlock> BLOCK_MOVER = registerBlock(MoverBlock.NAME, MoverBlock::new);
        // Marker
        public static final DeferredBlock<NormalMarkerBlock> BLOCK_MARKER = registerBlock(NormalMarkerBlock.NAME, NormalMarkerBlock::new);
        public static final DeferredBlock<FlexibleMarkerBlock> BLOCK_FLEXIBLE_MARKER = registerBlock(FlexibleMarkerBlock.NAME, FlexibleMarkerBlock::new);
        public static final DeferredBlock<ChunkMarkerBlock> BLOCK_CHUNK_MARKER = registerBlock(ChunkMarkerBlock.NAME, ChunkMarkerBlock::new);
        // Module
        public static final DeferredItem<PumpModuleItem> ITEM_PUMP_MODULE = registerItem(PumpModuleItem.NAME, PumpModuleItem::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final DeferredItem<BedrockModuleItem> ITEM_BEDROCK_MODULE = registerItem(BedrockModuleItem.NAME, BedrockModuleItem::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final DeferredItem<ExpModuleItem> ITEM_EXP_MODULE = registerItem(ExpModuleItem.NAME, ExpModuleItem::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final DeferredItem<RepeatTickModuleItem> ITEM_REPEAT_MODULE = registerItem(RepeatTickModuleItem.NAME, RepeatTickModuleItem::new, EnableMap.EnableOrNot.CONFIG_OFF);
        // Misc
        public static final DeferredItem<CheckerItemNeoForge> ITEM_CHECKER = registerItem(CheckerItemNeoForge.NAME, CheckerItemNeoForge::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final DeferredItem<YSetterItemNeoForge> ITEM_Y_SET = registerItem(YSetterItemNeoForge.NAME, YSetterItemNeoForge::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final DeferredBlock<FrameBlock> BLOCK_FRAME = registerBlock(FrameBlock.NAME, FrameBlock::new);
        public static final DeferredBlock<DebugStorageBlock> BLOCK_DEBUG_STORAGE = registerBlock(DebugStorageBlock.NAME, DebugStorageBlock::new);

        private static final Map<Class<? extends QpBlock>, Supplier<BlockEntityType<?>>> BLOCK_ENTITY_TYPES = new HashMap<>();
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuarryEntityNeoForge>> QUARRY_ENTITY_TYPE = registerBlockEntity(QuarryBlockNeoForge.NAME, BLOCK_QUARRY, QuarryEntityNeoForge::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorEntity>> GENERATOR_ENTITY_TYPE = registerBlockEntity(GeneratorBlock.NAME, BLOCK_GENERATOR, GeneratorEntity::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NormalMarkerEntity>> MARKER_ENTITY_TYPE = registerBlockEntity(NormalMarkerBlock.NAME, BLOCK_MARKER, NormalMarkerEntity::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MoverEntity>> MOVER_ENTITY_TYPE = registerBlockEntity(MoverBlock.NAME, BLOCK_MOVER, MoverEntity::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlexibleMarkerEntity>> FLEXIBLE_MARKER_ENTITY_TYPE = registerBlockEntity(FlexibleMarkerBlock.NAME, BLOCK_FLEXIBLE_MARKER, FlexibleMarkerEntity::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkMarkerEntity>> CHUNK_MARKER_ENTITY_TYPE = registerBlockEntity(ChunkMarkerBlock.NAME, BLOCK_CHUNK_MARKER, ChunkMarkerEntity::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DebugStorageEntity>> DEBUG_STORAGE_TYPE = registerBlockEntity(DebugStorageBlock.NAME, BLOCK_DEBUG_STORAGE, DebugStorageEntity::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvQuarryEntityNeoForge>> ADV_QUARRY_ENTITY_TYPE = registerBlockEntity(AdvQuarryBlock.NAME, BLOCK_ADV_QUARRY, AdvQuarryEntityNeoForge::new, EnableMap.EnableOrNot.CONFIG_ON);

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_TAB_REGISTER.register(QuarryPlus.modID, () -> QuarryPlus.buildCreativeModeTab(CreativeModeTab.builder()).build());
        public static final DeferredHolder<MenuType<?>, MenuType<? extends YSetterContainer>> Y_SET_MENU_TYPE = registerMenu("gui_y_setter", YSetterContainer::new);
        public static final DeferredHolder<MenuType<?>, MenuType<? extends MoverContainer>> MOVER_MENU_TYPE = registerMenu("gui_mover", MoverContainer::new);
        public static final DeferredHolder<MenuType<?>, MenuType<? extends ModuleContainer>> MODULE_MENU_TYPE = registerMenu("gui_quarry_module", ModuleContainer::new);
        public static final DeferredHolder<MenuType<?>, MenuType<? extends MarkerContainer>> FLEXIBLE_MARKER_MENU_TYPE = registerMenu(MarkerContainer.FLEXIBLE_NAME, MarkerContainer::createFlexibleMarkerContainer);
        public static final DeferredHolder<MenuType<?>, MenuType<? extends MarkerContainer>> CHUNK_MARKER_MENU_TYPE = registerMenu(MarkerContainer.CHUNK_NAME, MarkerContainer::createChunkMarkerContainer);
        public static final DeferredHolder<MenuType<?>, MenuType<? extends DebugStorageContainer>> DEBUG_STORAGE_MENU_TYPE = registerMenu(DebugStorageContainer.NAME, DebugStorageContainer::new);
        public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<? extends MachineLootFunction>> MACHINE_LOOT_FUNCTION = LOOT_TYPE_REGISTER.register(MachineLootFunction.NAME, () -> new LootItemFunctionType<>(MachineLootFunction.SERIALIZER));
        public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InstallBedrockModuleRecipe>> INSTALL_BEDROCK_MODULE_RECIPE = RECIPE_REGISTER.register(InstallBedrockModuleRecipe.NAME, () -> InstallBedrockModuleRecipe.SERIALIZER);

        static {
            DATA_COMPONENT_TYPE_REGISTER.register("quarry_remove_bedrock_component", () -> QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT);
            DATA_COMPONENT_TYPE_REGISTER.register("quarry_holding_exp_component", () -> QuarryDataComponents.HOLDING_EXP_COMPONENT);
        }

        private static <T extends QpBlock> DeferredBlock<T> registerBlock(String name, Supplier<T> supplier) {
            var block = BLOCK_REGISTER.register(name, supplier);
            ITEM_REGISTER.register(name, () -> block.get().blockItem);
            TAB_ITEMS.add(block);
            return block;
        }

        private static <T extends Item & InCreativeTabs> DeferredItem<T> registerItem(String name, Supplier<T> supplier, EnableMap.EnableOrNot enableOrNot) {
            var item = ITEM_REGISTER.register(name, supplier);
            TAB_ITEMS.add(item);
            ENABLE_MAP.put(name, enableOrNot);
            return item;
        }

        @SuppressWarnings("unchecked")
        @SafeVarargs
        private static <T extends QpBlock, U extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<U>> registerBlockEntity(String name, DeferredBlock<T> block, BlockEntityType.BlockEntitySupplier<U> factory, EnableMap.EnableOrNot enableOrNot, T... dummy) {
            var entityType = BLOCK_ENTITY_REGISTER.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(DSL.emptyPartType()));
            BLOCK_ENTITY_TYPES.put((Class<? extends QpBlock>) dummy.getClass().componentType(), (Supplier<BlockEntityType<?>>) (Object) entityType);
            ENABLE_MAP.put(name, enableOrNot);
            return entityType;
        }

        private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<? extends T>> registerMenu(String name, GeneralScreenHandler.ContainerFactory<T> factory) {
            return MENU_TYPE_REGISTER.register(name, () ->
                IMenuTypeExtension.create((i, inventory, friendlyByteBuf) -> factory.create(i, inventory, friendlyByteBuf.readBlockPos()))
            );
        }

        @Override
        public Supplier<? extends QuarryBlock> quarryBlock() {
            return BLOCK_QUARRY;
        }

        @Override
        public Supplier<? extends FrameBlock> frameBlock() {
            return BLOCK_FRAME;
        }

        @Override
        public Supplier<? extends GeneratorBlock> generatorBlock() {
            return BLOCK_GENERATOR;
        }

        @Override
        public Supplier<? extends NormalMarkerBlock> markerBlock() {
            return BLOCK_MARKER;
        }

        @Override
        public Supplier<? extends MoverBlock> moverBlock() {
            return BLOCK_MOVER;
        }

        @Override
        public Supplier<? extends FlexibleMarkerBlock> flexibleMarkerBlock() {
            return BLOCK_FLEXIBLE_MARKER;
        }

        @Override
        public Supplier<? extends ChunkMarkerBlock> chunkMarkerBlock() {
            return BLOCK_CHUNK_MARKER;
        }

        @Override
        public Supplier<? extends DebugStorageBlock> debugStorageBlock() {
            return BLOCK_DEBUG_STORAGE;
        }

        @Override
        public Supplier<? extends AdvQuarryBlock> advQuarryBlock() {
            return BLOCK_ADV_QUARRY;
        }

        @Override
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            var t = BLOCK_ENTITY_TYPES.get(block.getClass());
            if (t == null) {
                QuarryPlus.LOGGER.warn("Unknown block type: {}", block.name);
                return Optional.empty();
            }
            return Optional.of(t.get());
        }

        @Override
        public Map<String, EnableMap.EnableOrNot> defaultEnableSetting() {
            return ENABLE_MAP;
        }

        @Override
        public Supplier<? extends BedrockModuleItem> bedrockModuleItem() {
            return ITEM_BEDROCK_MODULE;
        }

        @Override
        public Stream<Supplier<? extends InCreativeTabs>> allItems() {
            return TAB_ITEMS.stream();
        }

        @Override
        public Supplier<MenuType<? extends YSetterContainer>> ySetterContainer() {
            return Y_SET_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends MoverContainer>> moverContainer() {
            return MOVER_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends ModuleContainer>> moduleContainer() {
            return MODULE_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends MarkerContainer>> flexibleMarkerContainer() {
            return FLEXIBLE_MARKER_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends MarkerContainer>> chunkMarkerContainer() {
            return CHUNK_MARKER_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends DebugStorageContainer>> debugStorageContainer() {
            return DEBUG_STORAGE_MENU_TYPE;
        }

        @Override
        public Supplier<LootItemFunctionType<? extends MachineLootFunction>> machineLootFunction() {
            return MACHINE_LOOT_FUNCTION;
        }
    }

    @Override
    public String platformName() {
        return "NeoForge";
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
        return FMLPaths.CONFIGDIR.get().resolve("%s.toml".formatted(QuarryPlus.modID));
    }

    @Override
    public Supplier<? extends QuarryConfig> getConfig() {
        return configLazy;
    }

    @Override
    public boolean isInDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public Transfer transfer() {
        return transferLazy.get();
    }

    @Override
    public FluidStackLike getFluidInItem(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucketItem) {
            return new FluidStackLike(bucketItem.content, MachineStorage.ONE_BUCKET, DataComponentPatch.EMPTY);
        }
        return FluidStackLike.EMPTY;
    }

    @Override
    public Component getFluidName(FluidStackLike stack) {
        var s = new FluidStack(stack.fluid(), Math.clamp(stack.amount(), 0, Integer.MAX_VALUE));
        return s.getHoverName();
    }

    @Override
    public <T extends AbstractContainerMenu> void openGui(ServerPlayer player, GeneralScreenHandler<T> handler) {
        player.openMenu(handler, handler.pos());
    }

    @SubscribeEvent
    public void onWorldUnload(ServerStoppedEvent event) {
        configLazy.reset();
    }
}
