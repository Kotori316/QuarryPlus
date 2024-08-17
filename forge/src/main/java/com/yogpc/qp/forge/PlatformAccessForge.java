package com.yogpc.qp.forge;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.ConfigHolder;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.forge.machine.marker.NormalMarkerEntityForge;
import com.yogpc.qp.forge.machine.misc.CheckerItemForge;
import com.yogpc.qp.forge.machine.misc.YSetterItemForge;
import com.yogpc.qp.forge.machine.quarry.QuarryBlockForge;
import com.yogpc.qp.forge.machine.quarry.QuarryEntityForge;
import com.yogpc.qp.forge.packet.PacketHandler;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.MachineLootFunction;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.marker.NormalMarkerBlock;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import com.yogpc.qp.machine.misc.GeneratorEntity;
import com.yogpc.qp.machine.misc.YSetterContainer;
import com.yogpc.qp.machine.module.BedrockModuleItem;
import com.yogpc.qp.machine.module.ModuleContainer;
import com.yogpc.qp.machine.module.PumpModuleItem;
import com.yogpc.qp.machine.mover.MoverBlock;
import com.yogpc.qp.machine.mover.MoverContainer;
import com.yogpc.qp.machine.mover.MoverEntity;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.crafting.ingredients.IIngredientSerializer;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.util.Lazy;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessForge implements PlatformAccess {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsForge::new);
    private final Lazy<PacketHandler> packetHandlerLazy = Lazy.lazy(PacketHandler::new);
    private final Lazy<TransferForge> transferLazy = Lazy.lazy(TransferForge::new);
    private final ConfigHolder configLazy = new ConfigHolder(() ->
        QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment)
    );

    public static class RegisterObjectsForge implements PlatformAccess.RegisterObjects {
        private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, QuarryPlus.modID);
        private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, QuarryPlus.modID);
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, QuarryPlus.modID);
        private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, QuarryPlus.modID);
        private static final DeferredRegister<IIngredientSerializer<?>> INGREDIENT_REGISTER = DeferredRegister.create(ForgeRegistries.INGREDIENT_SERIALIZERS, QuarryPlus.modID);
        private static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuarryPlus.modID);
        private static final DeferredRegister<LootItemFunctionType<?>> LOOT_TYPE_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(Registries.MENU, QuarryPlus.modID);
        static final List<DeferredRegister<?>> REGISTER_LIST = List.of(
            BLOCK_REGISTER, ITEM_REGISTER, BLOCK_ENTITY_REGISTER, RECIPE_REGISTER, INGREDIENT_REGISTER, CREATIVE_TAB_REGISTER, LOOT_TYPE_REGISTER, DATA_COMPONENT_TYPE_REGISTER, MENU_TYPE_REGISTER
        );
        private static final List<Supplier<? extends InCreativeTabs>> TAB_ITEMS = new ArrayList<>();

        public static final RegistryObject<QuarryBlockForge> BLOCK_QUARRY = registerBlock(QuarryBlockForge.NAME, QuarryBlockForge::new);
        public static final RegistryObject<FrameBlock> BLOCK_FRAME = registerBlock(FrameBlock.NAME, FrameBlock::new);
        public static final RegistryObject<GeneratorBlock> BLOCK_GENERATOR = registerBlock(GeneratorBlock.NAME, GeneratorBlock::new);
        public static final RegistryObject<NormalMarkerBlock> BLOCK_MARKER = registerBlock(NormalMarkerBlock.NAME, NormalMarkerBlock::new);
        public static final RegistryObject<MoverBlock> BLOCK_MOVER = registerBlock(MoverBlock.NAME, MoverBlock::new);

        public static final RegistryObject<CheckerItemForge> ITEM_CHECKER = registerItem(CheckerItemForge.NAME, CheckerItemForge::new);
        public static final RegistryObject<YSetterItemForge> ITEM_Y_SET = registerItem(YSetterItemForge.NAME, YSetterItemForge::new);
        public static final RegistryObject<PumpModuleItem> ITEM_PUMP_MODULE = registerItem(PumpModuleItem.NAME, PumpModuleItem::new);
        public static final RegistryObject<BedrockModuleItem> ITEM_BEDROCK_MODULE = registerItem(BedrockModuleItem.NAME, BedrockModuleItem::new);

        private static final Map<Class<? extends QpBlock>, Supplier<BlockEntityType<?>>> BLOCK_ENTITY_TYPES = new HashMap<>();
        public static final RegistryObject<BlockEntityType<QuarryEntityForge>> QUARRY_ENTITY_TYPE = registerBlockEntity(QuarryBlockForge.NAME, BLOCK_QUARRY, QuarryEntityForge::new);
        public static final RegistryObject<BlockEntityType<GeneratorEntity>> GENERATOR_ENTITY_TYPE = registerBlockEntity(GeneratorBlock.NAME, BLOCK_GENERATOR, GeneratorEntity::new);
        public static final RegistryObject<BlockEntityType<NormalMarkerEntityForge>> MARKER_ENTITY_TYPE = registerBlockEntity(NormalMarkerBlock.NAME, BLOCK_MARKER, NormalMarkerEntityForge::new);
        public static final RegistryObject<BlockEntityType<MoverEntity>> MOVER_ENTITY_TYPE = registerBlockEntity(MoverBlock.NAME, BLOCK_MOVER, MoverEntity::new);

        public static final RegistryObject<MenuType<? extends YSetterContainer>> Y_SET_MENU_TYPE = registerMenu("gui_y_setter", YSetterContainer::new);
        public static final RegistryObject<MenuType<? extends MoverContainer>> MOVER_MENU_TYPE = registerMenu("gui_mover", MoverContainer::new);
        public static final RegistryObject<MenuType<? extends ModuleContainer>> MODULE_MENU_TYPE = registerMenu("gui_quarry_module", ModuleContainer::new);

        public static final RegistryObject<LootItemFunctionType<? extends MachineLootFunction>> MACHINE_LOOT_FUNCTION = LOOT_TYPE_REGISTER.register(MachineLootFunction.NAME, () -> new LootItemFunctionType<>(MachineLootFunction.SERIALIZER));

        public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_TAB_REGISTER.register(QuarryPlus.modID, () -> QuarryPlus.buildCreativeModeTab(CreativeModeTab.builder()).build());

        private static <T extends QpBlock> RegistryObject<T> registerBlock(String name, Supplier<T> supplier) {
            var block = BLOCK_REGISTER.register(name, supplier);
            ITEM_REGISTER.register(name, () -> block.get().blockItem);
            TAB_ITEMS.add(block);
            return block;
        }

        private static <T extends Item & InCreativeTabs> RegistryObject<T> registerItem(String name, Supplier<T> supplier) {
            var item = ITEM_REGISTER.register(name, supplier);
            TAB_ITEMS.add(item);
            return item;
        }

        @SuppressWarnings("unchecked")
        @SafeVarargs
        private static <T extends QpBlock, U extends BlockEntity> RegistryObject<BlockEntityType<U>> registerBlockEntity(String name, RegistryObject<T> block, BlockEntityType.BlockEntitySupplier<U> factory, T... dummy) {
            var entityType = BLOCK_ENTITY_REGISTER.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(DSL.emptyPartType()));
            BLOCK_ENTITY_TYPES.put((Class<? extends QpBlock>) dummy.getClass().componentType(), (Supplier<BlockEntityType<?>>) (Object) entityType);
            return entityType;
        }

        private static <T extends AbstractContainerMenu> RegistryObject<MenuType<? extends T>> registerMenu(String name, GeneralScreenHandler.ContainerFactory<T> factory) {
            return MENU_TYPE_REGISTER.register(name, () ->
                IForgeMenuType.create((i, inventory, friendlyByteBuf) -> factory.create(i, inventory, friendlyByteBuf.readBlockPos()))
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
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            var t = BLOCK_ENTITY_TYPES.get(block.getClass());
            if (t == null) {
                QuarryPlus.LOGGER.warn("Unknown block type: {}", block.name);
                return Optional.empty();
            }
            return Optional.of(t.get());
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
        public Supplier<LootItemFunctionType<? extends MachineLootFunction>> machineLootFunction() {
            return MACHINE_LOOT_FUNCTION;
        }
    }

    @Override
    public String platformName() {
        return "Forge";
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
            return new FluidStackLike(bucketItem.getFluid(), MachineStorage.ONE_BUCKET, DataComponentPatch.EMPTY);
        }
        return FluidStackLike.EMPTY;
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
