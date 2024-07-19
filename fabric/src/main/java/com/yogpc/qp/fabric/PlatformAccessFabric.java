package com.yogpc.qp.fabric;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.fabric.machine.quarry.QuarryBlockFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryEntityFabric;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessFabric implements PlatformAccess {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsFabric::new);

    public static final class RegisterObjectsFabric implements RegisterObjects {
        public static final QuarryBlockFabric QUARRY_BLOCK = new QuarryBlockFabric();
        public static final BlockEntityType<QuarryEntityFabric> QUARRY_ENTITY_TYPE = BlockEntityType.Builder.of(QuarryEntityFabric::new, QUARRY_BLOCK).build(DSL.emptyPartType());
        public static final FrameBlock FRAME_BLOCK = new FrameBlock();

        private static final List<InCreativeTabs> TAB_ITEMS = new ArrayList<>();
        public static final CreativeModeTab TAB = QuarryPlus.buildCreativeModeTab(FabricItemGroup.builder()).build();

        static void registerAll() {
            registerEntityBlock(QUARRY_BLOCK, QUARRY_ENTITY_TYPE);
            registerBlockItem(FRAME_BLOCK);
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, QuarryPlus.modID), TAB);
        }

        private static void registerEntityBlock(QpBlock block, BlockEntityType<?> entityType) {
            registerBlockItem(block);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, block.name, entityType);
        }

        private static void registerBlockItem(QpBlock block) {
            Registry.register(BuiltInRegistries.BLOCK, block.name, block);
            Registry.register(BuiltInRegistries.ITEM, block.name, block.blockItem);
            TAB_ITEMS.add(block);
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
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            return switch (block) {
                case QuarryBlockFabric ignored -> Optional.of(QUARRY_ENTITY_TYPE);
                case null, default -> Optional.empty();
            };
        }

        @Override
        public Stream<Supplier<? extends InCreativeTabs>> allItems() {
            return TAB_ITEMS.stream().map(t -> () -> t);
        }
    }

    @Override
    public RegisterObjects registerObjects() {
        return itemsLazy.get();
    }
}
