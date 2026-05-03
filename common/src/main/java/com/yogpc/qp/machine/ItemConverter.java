package com.yogpc.qp.machine;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public record ItemConverter(List<Conversion> conversions) {

    public static ItemConverter defaultInstance() {
        List<Conversion> conversions;
        if (PlatformAccess.config().convertDeepslateOres()) {
            conversions = List.of(new DeepslateOreConversion());
        } else {
            conversions = List.of();
        }
        return new ItemConverter(conversions);
    }

    public interface Conversion {
        /**
         * @param stack you can assume the stack returned {@code true} in {@link Conversion#shouldApply(ItemStackTemplate)}
         * @return the converted stacks. Returning an empty stream will void the stacks
         */
        Stream<ItemStackTemplate> convert(ItemStackTemplate stack);

        default Stream<ItemStack> convert(ItemStack stack) {
            if (stack.isEmpty()) {
                return Stream.of(stack);
            }
            return this.convert(ItemStackTemplate.fromNonEmptyStack(stack)).map(ItemStackTemplate::create);
        }

        /**
         * @return whether to apply this conversion
         */
        boolean shouldApply(ItemStackTemplate stack);

        default boolean shouldApply(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            return this.shouldApply(ItemStackTemplate.fromNonEmptyStack(stack));
        }
    }

    @VisibleForTesting
    public Stream<ItemStackTemplate> convert(ItemStackTemplate stack) {
        return conversions.stream()
            .filter(conversion -> conversion.shouldApply(stack))
            .findAny()
            .map(f -> f.convert(stack))
            .orElseGet(() -> Stream.of(stack));
    }

    public Stream<ItemStack> convert(ItemStack stack) {
        if (stack.isEmpty()) {
            return Stream.empty();
        }
        return convert(ItemStackTemplate.fromNonEmptyStack(stack)).map(ItemStackTemplate::create);
    }

    public ItemConverter concat(List<Conversion> others) {
        var list = new ArrayList<>(this.conversions);
        list.addAll(others);
        return new ItemConverter(list);
    }

    public static class DeepslateOreConversion implements Conversion {
        @Override
        public Stream<ItemStackTemplate> convert(ItemStackTemplate stack) {
            var id = BuiltInRegistries.ITEM.getKey(stack.item().value());
            var newId = id.withPath(s -> s.replace("deepslate_", "").replace("_deepslate", ""));
            return BuiltInRegistries.ITEM.get(newId)
                .map(h -> new ItemStackTemplate(h, stack.count(), stack.components()))
                .map(Stream::of)
                .orElseGet(() -> Stream.of(stack));
        }

        @Override
        public boolean shouldApply(ItemStackTemplate stack) {
            var id = BuiltInRegistries.ITEM.getKey(stack.item().value());
            return id.getPath().contains("deepslate") && id.getPath().contains("ore");
        }
    }

    public static class ChunkDestroyerConversion implements Conversion {

        @Override
        public Stream<ItemStackTemplate> convert(ItemStackTemplate stack) {
            // Convert to empty if the condition matches
            return Stream.empty();
        }

        @Override
        public boolean shouldApply(ItemStackTemplate stack) {
            // Check item tag
            if (
                stack.is(ItemTags.DIRT)
                    || stack.is(ItemTags.GRASS_BLOCKS)
                    || stack.is(Items.COBBLESTONE)
                    || stack.is(Items.SANDSTONE)
                    || stack.is(Items.RED_SANDSTONE)
            ) {
                return true;
            }
            if (stack.item().value() instanceof BlockItem blockItem) {
                var state = blockItem.getBlock().defaultBlockState();
                return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER);
            }
            return false;
        }
    }

    public record ToEmptyConverter(Collection<MachineStorage.ItemKey> itemKeys) implements Conversion {

        @Override
        public Stream<ItemStackTemplate> convert(ItemStackTemplate stack) {
            return Stream.empty();
        }

        @Override
        public boolean shouldApply(ItemStackTemplate stack) {
            var key = MachineStorage.ItemKey.of(stack);
            return itemKeys.contains(key);
        }
    }
}
