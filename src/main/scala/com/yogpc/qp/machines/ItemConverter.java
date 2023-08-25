package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.yogpc.qp.utils.MapStreamSyntax.byKey;

public record ItemConverter(
    List<Map.Entry<Predicate<ItemKey>, Function<ItemKey, ItemKey>>> conversionMap) {

    public ItemConverter(Map<ItemKey, ItemKey> map) {
        this(map.entrySet().stream().map(e -> {
            Predicate<ItemKey> predicate = Predicate.isEqual(e.getKey());
            Function<ItemKey, ItemKey> converter = i -> e.getValue();
            return Map.entry(predicate, converter);
        }).toList());
    }

    public ItemStack map(ItemStack before) {
        if (conversionMap().isEmpty()) return before;
        var key = new ItemKey(before);
        var pair = mapToKey(key, before.getCount());
        return pair.getKey().toStack(pair.getValue());
    }

    public Map.Entry<ItemKey, Integer> mapToKey(ItemStack before) {
        return mapToKey(new ItemKey(before), before.getCount());
    }

    public Map.Entry<ItemKey, Integer> mapToKey(ItemKey before, int count) {
        var key = conversionMap().stream()
            .filter(byKey(p -> p.test(before)))
            .findFirst()
            .map(Map.Entry::getValue)
            .map(f -> convert(f, before))
            .orElse(before);
        return Map.entry(key, count);
    }

    private static ItemKey convert(Function<ItemKey, ItemKey> func, ItemKey key) {
        var converted = func.apply(key);
        if (!(func instanceof LogFunction l && l.noLog()))
            TraceQuarryWork.convertItem(key, converted);
        return converted;
    }

    public ItemConverter combined(@Nullable ItemConverter other) {
        if (other == null || other.conversionMap.isEmpty()) return this;
        if (this.conversionMap.isEmpty()) return other;
        var newList = new ArrayList<>(this.conversionMap());
        newList.addAll(other.conversionMap());
        return new ItemConverter(newList);
    }

    public static ItemConverter defaultConverter() {
        if (QuarryPlus.config.common.convertDeepslateOres.get()) {
            return deepslateConverter();
        } else {
            return new ItemConverter(List.of());
        }
    }

    public static ItemConverter deepslateConverter() {
        Predicate<ItemKey> predicate = i -> {
            var path = i.getId().getPath();
            return path.contains("deepslate") && path.contains("ore");
        };
        Function<ItemKey, ItemKey> function = i -> {
            var newPath = i.getId().getPath().replace("deepslate_", "").replace("_deepslate", "");
            var id = new ResourceLocation(i.getId().getNamespace(), newPath);
            if (ForgeRegistries.ITEMS.containsKey(id)) {
                return new ItemKey(ForgeRegistries.ITEMS.getValue(id), i.nbt());
            } else {
                return null;
            }
        };
        return new ItemConverter(List.of(Pair.of(predicate, function)));
    }

    /**
     * This method will return an ItemConverter instance which removes these items.
     * <ul>
     *     <li>Stone</li>
     *     <li>Cobblestone</li>
     *     <li>Dirt</li>
     *     <li>Grass Block</li>
     *     <li>Netherrack</li>
     *     <li>Sandstone</li>
     *     <li>Deepslate</li>
     *     <li>Blackstone</li>
     * </ul>
     *
     * @return ItemConverter instance for {@link com.yogpc.qp.machines.advquarry.TileAdvQuarry Chunk Destroyer}.
     */
    @SuppressWarnings("SpellCheckingInspection") // For javadoc
    public static ItemConverter advQuarryConverter() {
        if (!QuarryPlus.config.common.removeCommonMaterialsByCD.get()) return new ItemConverter(List.of());
        Function<ItemKey, ItemKey> function = new NoLogFunction(itemKey -> ItemKey.EMPTY_KEY);
        return new ItemConverter(Stream.of(
                tagPredicate(Tags.Items.STONE),
                tagPredicate(Tags.Items.COBBLESTONE),
                itemPredicate(Items.DIRT),
                itemPredicate(Items.GRASS_BLOCK),
                blockTagPredicate(BlockTags.BASE_STONE_OVERWORLD),
                blockTagPredicate(BlockTags.BASE_STONE_NETHER),
                tagPredicate(Tags.Items.SANDSTONE)
            ).map(p -> Map.entry(p, function))
            .toList());
    }

    public static ItemConverter voidConverter(List<ItemKey> voidedItems) {
        return new ItemConverter(voidedItems.stream().map(k -> Map.entry(Predicate.<ItemKey>isEqual(k), OneLogFunction.createEmpty())).toList());
    }

    static Predicate<ItemKey> tagPredicate(TagKey<Item> tag) {
        return itemKey -> itemKey.toStack(1).is(tag);
    }

    static Predicate<ItemKey> blockTagPredicate(TagKey<Block> tag) {
        return itemKey ->
            (itemKey.item() instanceof BlockItem blockItem)
                && blockItem.getBlock().defaultBlockState().is(tag);
    }

    static Predicate<ItemKey> itemPredicate(Item item) {
        return itemKey -> itemKey.item() == item;
    }

    private interface LogFunction extends Function<ItemKey, ItemKey> {
        boolean noLog();
    }

    private record NoLogFunction(Function<ItemKey, ItemKey> function) implements LogFunction {

        @Override
        public ItemKey apply(ItemKey key) {
            return this.function.apply(key);
        }

        @Override
        public boolean noLog() {
            return true;
        }
    }

    private static class OneLogFunction implements LogFunction {
        private final Function<ItemKey, ItemKey> function;
        private boolean logged = false;

        private OneLogFunction(Function<ItemKey, ItemKey> function) {
            this.function = function;
        }

        static Function<ItemKey, ItemKey> create(Function<ItemKey, ItemKey> function) {
            return new OneLogFunction(function);
        }

        static Function<ItemKey, ItemKey> createEmpty() {
            return create(key -> ItemKey.EMPTY_KEY);
        }

        @Override
        public boolean noLog() {
            if (this.logged) return true;
            this.logged = true;
            return false;
        }

        @Override
        public ItemKey apply(ItemKey itemKey) {
            return this.function.apply(itemKey);
        }
    }
}
