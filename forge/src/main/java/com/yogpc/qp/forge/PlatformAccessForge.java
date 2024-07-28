package com.yogpc.qp.forge;

import com.mojang.datafixers.DSL;
import com.yogpc.qp.*;
import com.yogpc.qp.forge.machine.misc.CheckerItemForge;
import com.yogpc.qp.forge.machine.quarry.QuarryBlockForge;
import com.yogpc.qp.forge.machine.quarry.QuarryEntityForge;
import com.yogpc.qp.forge.packet.PacketHandler;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import com.yogpc.qp.machine.misc.GeneratorEntity;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.crafting.ingredients.IIngredientSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessForge implements PlatformAccess {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsForge::new);
    private final Lazy<PacketHandler> packetHandlerLazy = Lazy.lazy(PacketHandler::new);
    private final Lazy<TransferForge> transferLazy = Lazy.lazy(TransferForge::new);

    public static class RegisterObjectsForge implements PlatformAccess.RegisterObjects {
        private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, QuarryPlus.modID);
        private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, QuarryPlus.modID);
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, QuarryPlus.modID);
        private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, QuarryPlus.modID);
        private static final DeferredRegister<IIngredientSerializer<?>> INGREDIENT_REGISTER = DeferredRegister.create(ForgeRegistries.INGREDIENT_SERIALIZERS, QuarryPlus.modID);
        private static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuarryPlus.modID);
        private static final DeferredRegister<LootItemFunctionType<?>> LOOT_TYPE_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, QuarryPlus.modID);
        static final List<DeferredRegister<?>> REGISTER_LIST = List.of(
            BLOCK_REGISTER, ITEM_REGISTER, BLOCK_ENTITY_REGISTER, RECIPE_REGISTER, INGREDIENT_REGISTER, CREATIVE_TAB_REGISTER, LOOT_TYPE_REGISTER, DATA_COMPONENT_TYPE_REGISTER
        );
        private static final List<Supplier<? extends InCreativeTabs>> TAB_ITEMS = new ArrayList<>();

        public static final RegistryObject<QuarryBlockForge> BLOCK_QUARRY = registerBlock(QuarryBlockForge.NAME, QuarryBlockForge::new);
        public static final RegistryObject<FrameBlock> BLOCK_FRAME = registerBlock(FrameBlock.NAME, FrameBlock::new);
        public static final RegistryObject<GeneratorBlock> BLOCK_GENERATOR = registerBlock(GeneratorBlock.NAME, GeneratorBlock::new);

        public static final RegistryObject<CheckerItemForge> ITEM_CHECKER = registerItem(CheckerItemForge.NAME, CheckerItemForge::new);

        public static final RegistryObject<BlockEntityType<QuarryEntityForge>> QUARRY_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register(QuarryBlockForge.NAME, () ->
            BlockEntityType.Builder.of(QuarryEntityForge::new, BLOCK_QUARRY.get()).build(DSL.emptyPartType()));
        public static final RegistryObject<BlockEntityType<GeneratorEntity>> GENERATOR_ENTITY_TYPE = BLOCK_ENTITY_REGISTER.register(GeneratorBlock.NAME, () ->
            BlockEntityType.Builder.of(GeneratorEntity::new, BLOCK_GENERATOR.get()).build(DSL.emptyPartType()));

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
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            return switch (block) {
                case QuarryBlockForge ignored -> Optional.of(QUARRY_ENTITY_TYPE.get());
                case GeneratorBlock ignored -> Optional.of(GENERATOR_ENTITY_TYPE.get());
                case null, default -> {
                    QuarryPlus.LOGGER.warn("Unknown block entity type: {}", block != null ? block.name : "null");
                    yield Optional.empty();
                }
            };
        }

        @Override
        public Stream<Supplier<? extends InCreativeTabs>> allItems() {
            return TAB_ITEMS.stream();
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
    public QuarryConfig quarryConfig() {
        return new QuarryConfigForge();
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

}