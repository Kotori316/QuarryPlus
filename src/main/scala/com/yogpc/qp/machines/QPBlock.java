package com.yogpc.qp.machines;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class QPBlock extends Block {
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    private final ResourceLocation registryName;
    public final QPBlockItem blockItem;

    public QPBlock(Properties properties, String name, Function<QPBlock, QPBlockItem> itemFunction) {
        super(properties);
        registryName = new ResourceLocation(QuarryPlus.modID, name);
        blockItem = itemFunction.apply(this);
    }

    public QPBlock(Properties properties, String name) {
        this(properties, name, b -> new QPBlockItem(b, new Item.Properties()));
    }

    public ResourceLocation getRegistryName() {
        return registryName;
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
    public void setPlacedBy(Level level, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(level, pPos, pState, pPlacer, pStack);
        if (!level.isClientSide && this.disallowedDim().contains(level.dimension().location())) {
            if (pPlacer instanceof Player player) {
                player.displayClientMessage(Component.literal("This block doesn't work in this dimension by server config"), false);
            }
        }
    }

    /**
     * Machines must not work in these dimensions.
     */
    public Set<ResourceLocation> disallowedDim() {
        return QuarryPlus.serverConfig.machineWork.unworkableDimensions.get().stream()
            .map(ResourceLocation::tryParse)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * Helper method copied from {@link net.minecraft.world.level.block.BaseEntityBlock}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> type1, BlockEntityType<E> exceptedType, CombinedBlockEntityTicker<? super E> ticker) {
        return exceptedType == type1 ? (BlockEntityTicker<A>) ticker : null;
    }

    public static class QPBlockItem extends BlockItem {

        public QPBlockItem(QPBlock block, Item.Properties properties) {
            super(block, properties);
        }

        /**
         * Implemented for unit test. Default implementation just returns "air".
         * This override return actual block name.
         */
        @Override
        public String toString() {
            return this.getRegistryName().getPath();
        }

        public ResourceLocation getRegistryName() {
            return ((QPBlock) getBlock()).getRegistryName();
        }

        @Override
        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
            super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
            if (QuarryPlus.config != null && !QuarryPlus.config.enableMap.enabled(getRegistryName())) {
                tooltips.add(Component.translatable("quarryplus.tooltip.item_disable_message"));
            }
        }

        public List<ItemStack> creativeTabItem() {
            return List.of(new ItemStack(this));
        }
    }
}
