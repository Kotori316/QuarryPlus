package com.yogpc.qp.machines;

import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class QPBlock extends Block {
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public final QPBlockItem blockItem;
    private final ResourceLocation internalName;

    public QPBlock(Properties properties, String name, Function<QPBlock, QPBlockItem> itemFunction) {
        super(properties);
        internalName = new ResourceLocation(QuarryPlus.modID, name);
        blockItem = itemFunction.apply(this);
        blockItem.setRegistryName(QuarryPlus.modID, name);
    }

    public QPBlock(Properties properties, String name) {
        this(properties, name, b -> new QPBlockItem(b, new Item.Properties().tab(QuarryPlus.CREATIVE_TAB)));
    }

    /**
     * Implemented for unit test. Default implementation returns Air item as modded items are not registered in Registry.
     *
     * @return the item representing this block.
     */
    @Override
    public final Item asItem() {
        return this.blockItem;
    }

    @Override
    public String toString() {
        return String.valueOf(internalName);
    }

    public ResourceLocation getRegistryName() {
        return internalName;
    }

    @Override
    @org.jetbrains.annotations.Nullable
    @SuppressWarnings("deprecation")
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof ExtendedScreenHandlerFactory e) {
            return e;
        } else if (blockEntity instanceof MenuProvider m) {
            QuarryPlus.LOGGER.warn("BlockEntity {} implements menu provider instead of extended one.", m.getClass());
            return m;
        } else {
            return null;
        }
    }

    /**
     * Helper method copied from {@link net.minecraft.world.level.block.BaseEntityBlock}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> type1, BlockEntityType<E> exceptedType, BlockEntityTicker<? super E> ticker) {
        return exceptedType == type1 ? (BlockEntityTicker<A>) ticker : null;
    }

    public static class QPBlockItem extends BlockItem {
        private ResourceLocation internalName;

        public QPBlockItem(QPBlock block, Item.Properties properties) {
            super(block, properties);
        }

        /**
         * Implemented for unit test. Default implementation just returns "air".
         * This override return actual block name.
         */
        @Override
        public String toString() {
            return internalName.getPath();
        }

        public void setRegistryName(String modId, String name) {
            internalName = new ResourceLocation(modId, name);
        }
    }
}
