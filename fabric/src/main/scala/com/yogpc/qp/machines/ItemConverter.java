package com.yogpc.qp.machines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;

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
        return conversionMap()
            .stream()
            .filter(e -> e.getKey().test(key))
            .findFirst()
            .map(Map.Entry::getValue)
            .map(f -> f.apply(key))
            // Null check is done in this context.
            .map(k -> k.toStack(before.getCount()))
            .orElse(before);
    }

    public ItemConverter combined(ItemConverter other) {
        var newList = new ArrayList<>(this.conversionMap());
        newList.addAll(other.conversionMap());
        return new ItemConverter(newList);
    }

    public static ItemConverter defaultConverter() {
        if (QuarryPlus.config.common.convertDeepslateOres) {
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
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                return new ItemKey(BuiltInRegistries.ITEM.get(id), i.nbt());
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
        if (QuarryPlus.config.common.removesCommonMaterialAdvQuarry) {
            Function<ItemKey, ItemKey> function = itemKey -> new ItemKey(ItemStack.EMPTY);
            return new ItemConverter(Stream.of(
                    itemPredicate(Items.STONE),
                    itemPredicate(Items.GRANITE),
                    itemPredicate(Items.DIORITE),
                    itemPredicate(Items.ANDESITE),
                    tagPredicate(ItemTags.STONE_TOOL_MATERIALS),
                    itemPredicate(Items.DIRT),
                    itemPredicate(Items.GRASS_BLOCK),
                    itemPredicate(Items.NETHERRACK),
                    itemPredicate(Items.DEEPSLATE),
                    itemPredicate(Items.TUFF),
                    itemPredicate(Items.BLACKSTONE),
                    itemPredicate(Items.SANDSTONE)
                ).map(p -> Map.entry(p, function))
                .toList());
        } else {
            return new ItemConverter(List.of());
        }
    }

    static Predicate<ItemKey> tagPredicate(TagKey<Item> tag) {
        return itemKey -> itemKey.toStack(1).is(tag);
    }

    static Predicate<ItemKey> itemPredicate(Item item) {
        return itemKey -> itemKey.item() == item;
    }
}
