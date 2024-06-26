package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EnchantmentIngredient extends AbstractIngredient {
    public static final String NAME = "enchantment_ingredient";
    private final ItemStack stack;
    private final List<EnchantmentInstance> enchantments;
    @Nullable
    private final CompoundTag withoutEnchantment;
    private final boolean checkDamage;

    public EnchantmentIngredient(ItemStack stack, List<EnchantmentInstance> enchantments, boolean checkDamage) {
        super(Stream.of(new Ingredient.ItemValue(addEnchantments(stack, enchantments))));
        this.stack = stack;
        this.enchantments = enchantments;
        this.withoutEnchantment = getTagWithoutEnchantment(stack, checkDamage);
        this.checkDamage = checkDamage;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        Serializer.INSTANCE.write(object, this);
        return object;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (this.stack.getItem() != stack.getItem()) {
            return false;
        }
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (!this.enchantments.stream().allMatch(d -> enchantments.getOrDefault(d.enchantment, 0) >= d.level)) {
            return false;
        }
        if (this.withoutEnchantment == null) {
            // Not to check tags as we didn't expect other tags
            return true;
        }
        CompoundTag nbt = getTagWithoutEnchantment(stack, checkDamage);
        return Objects.equals(this.withoutEnchantment, nbt);
    }

    private static ItemStack addEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
        ItemStack toEnchantment = stack.copy();
        enchantments.forEach(d -> toEnchantment.enchant(d.enchantment, d.level));
        return toEnchantment;
    }

    @Nullable
    private static CompoundTag getTagWithoutEnchantment(ItemStack stack, boolean checkDamage) {
        return Optional.ofNullable(stack.getShareTag()).map(CompoundTag::copy).map(c -> {
            c.remove(ItemStack.TAG_ENCH);
            c.remove(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
            if (!checkDamage) c.remove(ItemStack.TAG_DAMAGE);
            return c;
        }).filter(Predicate.not(CompoundTag::isEmpty)).orElse(null);
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<EnchantmentIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public EnchantmentIngredient parse(FriendlyByteBuf buffer) {
            ItemStack stack = buffer.readItem();
            int size = buffer.readVarInt();
            List<EnchantmentInstance> data = IntStream.range(0, size)
                .mapToObj(operand -> {
                    Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(buffer.readResourceLocation());
                    int level = buffer.readInt();
                    return new EnchantmentInstance(Objects.requireNonNull(enchantment), level);
                }).collect(Collectors.toList());
            boolean checkDamage = buffer.readBoolean();
            return new EnchantmentIngredient(stack, data, checkDamage);
        }

        @Override
        public EnchantmentIngredient parse(JsonObject json) {
            ItemStack stack = CraftingHelper.getItemStack(json, true);
            boolean checkDamage = GsonHelper.getAsBoolean(json, "checkDamage", false);
            List<EnchantmentInstance> data;
            if (json.has("enchantments")) {
                JsonArray enchantmentArray = json.getAsJsonArray("enchantments");
                data = StreamSupport.stream(enchantmentArray.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(o -> {
                        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(GsonHelper.getAsString(o, "id")));
                        int level = GsonHelper.getAsInt(o, "level", 1);
                        return new EnchantmentInstance(Objects.requireNonNull(enchantment), level);
                    }).collect(Collectors.toList());
            } else {
                data = Collections.emptyList();
            }
            return new EnchantmentIngredient(stack, data, checkDamage);
        }

        @Override
        public void write(FriendlyByteBuf buffer, EnchantmentIngredient ingredient) {
            buffer.writeItemStack(ingredient.stack, false);
            buffer.writeVarInt(ingredient.enchantments.size());
            for (EnchantmentInstance data : ingredient.enchantments) {
                buffer.writeResourceLocation(Objects.requireNonNull(data.enchantment.getRegistryName()));
                buffer.writeInt(data.level);
            }
            buffer.writeBoolean(ingredient.checkDamage);
        }

        @SuppressWarnings("ConstantConditions")
        public void write(JsonObject json, EnchantmentIngredient ingredient) {
            json.addProperty("type", CraftingHelper.getID(Serializer.INSTANCE).toString());
            json.addProperty("item", ingredient.stack.getItem().getRegistryName().toString());
            json.addProperty("count", ingredient.stack.getCount());
            json.addProperty("checkDamage", ingredient.checkDamage);
            if (ingredient.withoutEnchantment != null) {
                JsonElement element = Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, ingredient.withoutEnchantment);
                json.add("nbt", element);
            }
            JsonArray enchantmentArray = ingredient.enchantments.stream().reduce(new JsonArray(), (jsonElements, enchantmentData) -> {
                JsonObject object = new JsonObject();
                object.addProperty("id", enchantmentData.enchantment.getRegistryName().toString());
                object.addProperty("level", enchantmentData.level);
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
