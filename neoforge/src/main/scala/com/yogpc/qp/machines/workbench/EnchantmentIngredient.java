package com.yogpc.qp.machines.workbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EnchantmentIngredient extends Ingredient {
    public static final String NAME = "enchantment_ingredient";
    private final ItemStack stack;
    private final List<EnchantmentInstance> enchantments;
    private final CompoundTag withoutEnchantment;
    private final boolean checkDamage;
    private final boolean checkOtherTags;

    public EnchantmentIngredient(ItemStack stack, List<EnchantmentInstance> enchantments, boolean checkDamage, boolean checkOtherTags) {
        super(Stream.of(new Ingredient.ItemValue(addEnchantments(stack, enchantments))), () -> TYPE);
        this.stack = stack;
        this.enchantments = enchantments;
        this.withoutEnchantment = getTagWithoutEnchantment(stack, checkDamage);
        this.checkDamage = checkDamage;
        this.checkOtherTags = checkOtherTags;
    }

    @Override
    public boolean isSimple() {
        return false;
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
        if (this.checkDamage) {
            int expectedDamage = this.stack.getDamageValue();
            int actualDamage = stack.getDamageValue();
            if (expectedDamage != actualDamage) return false;
        }
        if (this.checkOtherTags) {
            CompoundTag nbt = getTagWithoutEnchantment(stack, checkDamage);
            return Objects.equals(this.withoutEnchantment, nbt);
        } else {
            return true;
        }
    }

    private ItemStack stackForSerialization() {
        var stack = this.stack.copy();
        stack.setTag(this.withoutEnchantment);
        if (!checkDamage) {
            // setTag added damage tag even the given tag is null.
            stack.removeTagKey("Damage");
        }
        return stack;
    }

    private static ItemStack addEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
        ItemStack toEnchantment = stack.copy();
        enchantments.forEach(d -> toEnchantment.enchant(d.enchantment, d.level));
        return toEnchantment;
    }

    @Nullable
    private static CompoundTag getTagWithoutEnchantment(ItemStack stack, boolean checkDamage) {
        return Optional.ofNullable(stack.getTag()).map(CompoundTag::copy).map(c -> {
            c.remove(ItemStack.TAG_ENCH);
            c.remove(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
            if (!checkDamage) c.remove(ItemStack.TAG_DAMAGE);
            return c;
        }).filter(Predicate.not(CompoundTag::isEmpty)).orElse(null);
    }

    private static final Codec<EnchantmentInstance> ENCHANTMENT_INSTANCE_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BuiltInRegistries.ENCHANTMENT.byNameCodec().fieldOf("id").forGetter(i -> i.enchantment),
            Codec.INT.fieldOf("level").forGetter(i -> i.level)
        ).apply(instance, EnchantmentInstance::new)
    );
    public static final Codec<EnchantmentIngredient> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(i -> i.stackForSerialization()),
            ENCHANTMENT_INSTANCE_CODEC.listOf().optionalFieldOf("enchantments", List.of()).forGetter(i -> i.enchantments),
            Codec.BOOL.optionalFieldOf("checkDamage").xmap(o -> o.orElse(false), Optional::of).forGetter(i -> i.checkDamage),
            Codec.BOOL.optionalFieldOf("checkOtherTags").xmap(o -> o.orElse(false), Optional::of).forGetter(i -> i.checkOtherTags)
        ).apply(instance, EnchantmentIngredient::new));

    public static final IngredientType<EnchantmentIngredient> TYPE = new IngredientType<>(CODEC);
}
