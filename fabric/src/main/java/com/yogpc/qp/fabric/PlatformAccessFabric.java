package com.yogpc.qp.fabric;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.*;
import com.yogpc.qp.fabric.machine.misc.CheckerItemFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryBlockFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryEntityFabric;
import com.yogpc.qp.fabric.packet.PacketHandler;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.marker.NormalMarkerBlock;
import com.yogpc.qp.machine.marker.NormalMarkerEntity;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import com.yogpc.qp.machine.misc.GeneratorEntity;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.util.Lazy;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessFabric implements PlatformAccess {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsFabric::new);
    private final Lazy<PacketHandler> packetHandlerLazy = Lazy.lazy(PacketHandler::new);
    private final Lazy<TransferFabric> transferLazy = Lazy.lazy(TransferFabric::new);

    public static final class RegisterObjectsFabric implements RegisterObjects {
        public static final QuarryBlockFabric QUARRY_BLOCK = new QuarryBlockFabric();
        public static final BlockEntityType<QuarryEntityFabric> QUARRY_ENTITY_TYPE = BlockEntityType.Builder.of(QuarryEntityFabric::new, QUARRY_BLOCK).build(DSL.emptyPartType());
        public static final FrameBlock FRAME_BLOCK = new FrameBlock();
        public static final GeneratorBlock GENERATOR_BLOCK = new GeneratorBlock();
        public static final BlockEntityType<GeneratorEntity> GENERATOR_ENTITY_TYPE = BlockEntityType.Builder.of(GeneratorEntity::new, GENERATOR_BLOCK).build(DSL.emptyPartType());
        public static final CheckerItemFabric CHECKER_ITEM = new CheckerItemFabric();
        public static final NormalMarkerBlock MARKER_BLOCK = new NormalMarkerBlock();
        public static final BlockEntityType<NormalMarkerEntity> MARKER_ENTITY_TYPE = BlockEntityType.Builder.of(NormalMarkerEntity::new, MARKER_BLOCK).build(DSL.emptyPartType());

        private static final List<InCreativeTabs> TAB_ITEMS = new ArrayList<>();
        public static final CreativeModeTab TAB = QuarryPlus.buildCreativeModeTab(FabricItemGroup.builder()).build();
        private static final Map<Class<? extends QpBlock>, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashMap<>();

        static void registerAll() {
            registerEntityBlock(QUARRY_BLOCK, QUARRY_ENTITY_TYPE);
            registerBlockItem(FRAME_BLOCK);
            registerEntityBlock(GENERATOR_BLOCK, GENERATOR_ENTITY_TYPE);
            registerItem(CHECKER_ITEM, CHECKER_ITEM.name);
            registerEntityBlock(MARKER_BLOCK, MARKER_ENTITY_TYPE);
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, QuarryPlus.modID), TAB);
        }

        private static void registerEntityBlock(QpBlock block, BlockEntityType<?> entityType) {
            if (!entityType.isValid(block.defaultBlockState())) {
                throw new IllegalArgumentException("Invalid block entity type (%s) for %s".formatted(entityType, block.getClass().getSimpleName()));
            }
            registerBlockItem(block);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, block.name, entityType);
            BLOCK_ENTITY_TYPES.put(block.getClass(), entityType);
        }

        private static void registerBlockItem(QpBlock block) {
            Registry.register(BuiltInRegistries.BLOCK, block.name, block);
            registerItem(block.blockItem, block.name);
            TAB_ITEMS.add(block);
        }

        private static void registerItem(Item item, ResourceLocation name) {
            Registry.register(BuiltInRegistries.ITEM, name, item);
            if (item instanceof InCreativeTabs c) {
                TAB_ITEMS.add(c);
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
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            var t = BLOCK_ENTITY_TYPES.get(block.getClass());
            if (t == null) {
                QuarryPlus.LOGGER.warn("Unknown block type: {}", block.name);
                return Optional.empty();
            }
            return Optional.of(t);
        }

        @Override
        public Stream<Supplier<? extends InCreativeTabs>> allItems() {
            return TAB_ITEMS.stream().map(t -> () -> t);
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
    public QuarryConfig quarryConfig() {
        return new QuarryConfigFabric();
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
}
