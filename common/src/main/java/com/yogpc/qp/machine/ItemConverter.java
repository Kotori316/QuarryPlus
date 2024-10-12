package com.yogpc.qp.machine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public record ItemConverter(List<Conversion> conversions) {

    public interface Conversion {
        /**
         * @param stack you can assume the stack returned {@code true} in {@link Conversion#shouldApply(ItemStack)}
         * @return the converted stacks. Returning empty stream will void the stacks
         */
        Stream<ItemStack> convert(ItemStack stack);

        /**
         * @return whether to apply this conversion
         */
        boolean shouldApply(ItemStack stack);
    }

    public Stream<ItemStack> convert(ItemStack stack) {
        return conversions.stream()
            .filter(conversion -> conversion.shouldApply(stack))
            .findAny()
            .map(f -> f.convert(stack))
            .orElseGet(() -> Stream.of(stack));
    }

    public ItemConverter concat(List<Conversion> others) {
        var list = new ArrayList<>(this.conversions);
        list.addAll(others);
        return new ItemConverter(list);
    }

    public static class DeepslateOreConversion implements Conversion {
        @Override
        public Stream<ItemStack> convert(ItemStack stack) {
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            var newId = id.withPath(s -> s.replace("deepslate_", "").replace("_deepslate", ""));
            return BuiltInRegistries.ITEM.getHolder(newId)
                .map(h -> new ItemStack(h, stack.getCount(), stack.getComponentsPatch()))
                .stream();
        }

        @Override
        public boolean shouldApply(ItemStack stack) {
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return id.getPath().contains("deepslate") && id.getPath().contains("ore");
        }
    }

    public static class ChunkDestroyerConversion implements Conversion {

        @Override
        public Stream<ItemStack> convert(ItemStack stack) {
            // Convert to empty if the condition matches
            return Stream.empty();
        }

        @Override
        public boolean shouldApply(ItemStack stack) {
            // Check item tag
            if (stack.is(ItemTags.DIRT) || stack.is(Items.COBBLESTONE)) {
                return true;
            }
            if (stack.getItem() instanceof BlockItem blockItem) {
                var state = blockItem.getBlock().defaultBlockState();
                return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER);
            }
            return false;
        }
    }

    public record ToEmptyConverter(Set<MachineStorage.ItemKey> itemKeys) implements Conversion {

        @Override
        public Stream<ItemStack> convert(ItemStack stack) {
            return Stream.empty();
        }

        @Override
        public boolean shouldApply(ItemStack stack) {
            var key = MachineStorage.ItemKey.of(stack);
            return itemKeys.contains(key);
        }
    }
}
