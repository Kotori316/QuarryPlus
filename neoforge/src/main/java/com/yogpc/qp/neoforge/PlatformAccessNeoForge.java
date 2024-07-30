package com.yogpc.qp.neoforge;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.*;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.marker.NormalMarkerBlock;
import com.yogpc.qp.machine.marker.NormalMarkerEntity;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import com.yogpc.qp.machine.misc.GeneratorEntity;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.neoforge.machine.misc.CheckerItemNeoForge;
import com.yogpc.qp.neoforge.machine.quarry.QuarryBlockNeoForge;
import com.yogpc.qp.neoforge.machine.quarry.QuarryEntityNeoForge;
import com.yogpc.qp.neoforge.packet.PacketHandler;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.*;
import org.apache.logging.log4j.util.Lazy;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessNeoForge implements PlatformAccess {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsNeoForge::new);
    private final Lazy<PacketHandler> packetHandlerLazy = Lazy.lazy(PacketHandler::new);
    private final Lazy<TransferNeoForge> transferLazy = Lazy.lazy(TransferNeoForge::new);

    public static class RegisterObjectsNeoForge implements PlatformAccess.RegisterObjects {
        private static final DeferredRegister.Blocks BLOCK_REGISTER = DeferredRegister.createBlocks(QuarryPlus.modID);
        private static final DeferredRegister.Items ITEM_REGISTER = DeferredRegister.createItems(QuarryPlus.modID);
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, QuarryPlus.modID);
        private static final DeferredRegister<IngredientType<?>> INGREDIENT_REGISTER = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, QuarryPlus.modID);
        private static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuarryPlus.modID);
        private static final DeferredRegister<LootItemFunctionType<?>> LOOT_TYPE_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, QuarryPlus.modID);
        static final List<DeferredRegister<?>> REGISTER_LIST = List.of(
            BLOCK_REGISTER, ITEM_REGISTER, BLOCK_ENTITY_REGISTER, RECIPE_REGISTER, INGREDIENT_REGISTER, CREATIVE_TAB_REGISTER, LOOT_TYPE_REGISTER, DATA_COMPONENT_TYPE_REGISTER
        );
        private static final List<Supplier<? extends InCreativeTabs>> TAB_ITEMS = new ArrayList<>();

        public static final DeferredBlock<QuarryBlockNeoForge> BLOCK_QUARRY = registerBlock(QuarryBlockNeoForge.NAME, QuarryBlockNeoForge::new);
        public static final DeferredBlock<FrameBlock> BLOCK_FRAME = registerBlock(FrameBlock.NAME, FrameBlock::new);
        public static final DeferredBlock<GeneratorBlock> BLOCK_GENERATOR = registerBlock(GeneratorBlock.NAME, GeneratorBlock::new);
        public static final DeferredBlock<NormalMarkerBlock> BLOCK_MARKER = registerBlock(NormalMarkerBlock.NAME, NormalMarkerBlock::new);

        public static final DeferredItem<CheckerItemNeoForge> ITEM_CHECKER = registerItem(CheckerItemNeoForge.NAME, CheckerItemNeoForge::new);

        private static final Map<Class<? extends QpBlock>, Supplier<BlockEntityType<?>>> BLOCK_ENTITY_TYPES = new HashMap<>();
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuarryEntityNeoForge>> QUARRY_ENTITY_TYPE = registerBlockEntity(QuarryBlockNeoForge.NAME, BLOCK_QUARRY, QuarryEntityNeoForge::new);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorEntity>> GENERATOR_ENTITY_TYPE = registerBlockEntity(GeneratorBlock.NAME, BLOCK_GENERATOR, GeneratorEntity::new);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NormalMarkerEntity>> MARKER_ENTITY_TYPE = registerBlockEntity(NormalMarkerBlock.NAME, BLOCK_MARKER, NormalMarkerEntity::new);

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_TAB_REGISTER.register(QuarryPlus.modID, () -> QuarryPlus.buildCreativeModeTab(CreativeModeTab.builder()).build());

        private static <T extends QpBlock> DeferredBlock<T> registerBlock(String name, Supplier<T> supplier) {
            var block = BLOCK_REGISTER.register(name, supplier);
            ITEM_REGISTER.register(name, () -> block.get().blockItem);
            TAB_ITEMS.add(block);
            return block;
        }

        private static <T extends Item & InCreativeTabs> DeferredItem<T> registerItem(String name, Supplier<T> supplier) {
            var item = ITEM_REGISTER.register(name, supplier);
            TAB_ITEMS.add(item);
            return item;
        }

        @SuppressWarnings("unchecked")
        @SafeVarargs
        private static <T extends QpBlock, U extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<U>> registerBlockEntity(String name, DeferredBlock<T> block, BlockEntityType.BlockEntitySupplier<U> factory, T... dummy) {
            var entityType = BLOCK_ENTITY_REGISTER.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(DSL.emptyPartType()));
            BLOCK_ENTITY_TYPES.put((Class<? extends QpBlock>) dummy.getClass().componentType(), (Supplier<BlockEntityType<?>>) (Object) entityType);
            return entityType;
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
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            var t = BLOCK_ENTITY_TYPES.get(block.getClass());
            if (t == null) {
                QuarryPlus.LOGGER.warn("Unknown block type: {}", block.name);
                return Optional.empty();
            }
            return Optional.of(t.get());
        }

        @Override
        public Stream<Supplier<? extends InCreativeTabs>> allItems() {
            return TAB_ITEMS.stream();
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
    public QuarryConfig quarryConfig() {
        return new QuarryConfigNeoForge();
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
}
