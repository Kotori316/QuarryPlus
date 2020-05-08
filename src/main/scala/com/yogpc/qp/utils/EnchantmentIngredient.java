package com.yogpc.qp.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantmentIngredient extends Ingredient {
    private final ItemStack stack;
    private final List<EnchantmentData> enchantments;
    private final CompoundNBT withoutEnchantment;

    public EnchantmentIngredient(ItemStack stack, List<EnchantmentData> enchantments) {
        super(Stream.of(new Ingredient.SingleItemList(addEnchantments(stack, enchantments))));
        this.stack = stack;
        this.enchantments = enchantments;
        this.withoutEnchantment = Optional.ofNullable(stack.getShareTag()).map(CompoundNBT::copy).map(c -> {
            c.remove("Enchantments");
            return c;
        }).filter(c -> !c.isEmpty()).orElse(null);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        Serializer.INSTANCE.write(object, this);
        return object;
    }

    @Override
    public boolean test(@Nullable ItemStack p_test_1_) {
        if (p_test_1_ == null) {
            return false;
        }
        if (stack.getItem() != p_test_1_.getItem() || stack.getCount() != p_test_1_.getCount()) {
            return false;
        }
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(p_test_1_);
        if (!this.enchantments.stream().allMatch(d -> enchantments.getOrDefault(d.enchantment, 0) >= d.enchantmentLevel)) {
            return false;
        }
        CompoundNBT nbt = Optional.ofNullable(p_test_1_.getShareTag()).map(CompoundNBT::copy).map(c -> {
            c.remove("Enchantments");
            return c;
        }).filter(c -> !c.isEmpty()).orElse(null);
        return Objects.equals(this.withoutEnchantment, nbt);
    }

    private static ItemStack addEnchantments(ItemStack stack, List<EnchantmentData> enchantments) {
        ItemStack toEnchantment = stack.copy();
        enchantments.forEach(d -> toEnchantment.addEnchantment(d.enchantment, d.enchantmentLevel));
        return toEnchantment;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<EnchantmentIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public EnchantmentIngredient parse(PacketBuffer buffer) {
            ItemStack stack = buffer.readItemStack();
            int size = buffer.readVarInt();
            List<EnchantmentData> data = IntStream.range(0, size)
                .mapToObj(operand -> {
                    Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(buffer.readResourceLocation());
                    int level = buffer.readInt();
                    return new EnchantmentData(Objects.requireNonNull(enchantment), level);
                }).collect(Collectors.toList());
            return new EnchantmentIngredient(stack, data);
        }

        @Override
        public EnchantmentIngredient parse(JsonObject json) {
            ItemStack stack = CraftingHelper.getItemStack(json, true);
            List<EnchantmentData> data;
            if (json.has("enchantments")) {
                JsonArray enchantmentArray = json.getAsJsonArray("enchantments");
                data = StreamSupport.stream(enchantmentArray.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(o -> {
                        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(JSONUtils.getString(o, "id")));
                        int level = JSONUtils.getInt(o, "level", 1);
                        return new EnchantmentData(Objects.requireNonNull(enchantment), level);
                    }).collect(Collectors.toList());
            } else {
                data = Collections.emptyList();
            }
            return new EnchantmentIngredient(stack, data);
        }

        @Override
        public void write(PacketBuffer buffer, EnchantmentIngredient ingredient) {
            buffer.writeItemStack(ingredient.stack);
            buffer.writeVarInt(ingredient.enchantments.size());
            for (EnchantmentData data : ingredient.enchantments) {
                buffer.writeResourceLocation(Objects.requireNonNull(data.enchantment.getRegistryName()));
                buffer.writeInt(data.enchantmentLevel);
            }
        }

        @SuppressWarnings("ConstantConditions")
        public void write(JsonObject json, EnchantmentIngredient ingredient) {
            json.addProperty("type", CraftingHelper.getID(Serializer.INSTANCE).toString());
            json.addProperty("item", ingredient.stack.getItem().getRegistryName().toString());
            json.addProperty("count", ingredient.stack.getCount());
            if (ingredient.stack.hasTag()) {
                json.add("nbt", JSONUtils.fromJson(ingredient.stack.getTag().toString()));
            }
            JsonArray enchantmentArray = ingredient.enchantments.stream().reduce(new JsonArray(), (jsonElements, enchantmentData) -> {
                JsonObject object = new JsonObject();
                object.addProperty("id", enchantmentData.enchantment.getRegistryName().toString());
                object.addProperty("level", enchantmentData.enchantmentLevel);
                jsonElements.add(object);
                return jsonElements;
            }, (jsonElements, jsonElements2) -> {
                jsonElements.addAll(jsonElements2);
                return jsonElements;
            });
            if (enchantmentArray.size() != 0) {
                json.add("enchantments", enchantmentArray);
            }
        }
    }
}
