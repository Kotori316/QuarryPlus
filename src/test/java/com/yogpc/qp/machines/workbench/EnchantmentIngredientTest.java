package com.yogpc.qp.machines.workbench;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentIngredientTest extends QuarryPlusTest {
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    ItemStack diamondPickaxe, ironPickaxe;

    @BeforeEach
    void setup() {
        var diamond_pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        diamond_pickaxe.removeTagKey("Damage");
        this.diamondPickaxe = diamond_pickaxe;
        var iron = new ItemStack(Items.IRON_PICKAXE);
        iron.removeTagKey("Damage");
        this.ironPickaxe = iron;
    }

    @Test
    void instance() {
        var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
        assertTrue(ei.toJson().isJsonObject());
        assertNotEquals(0, ei.toJson().size());
    }

    @Nested
    class FromJson {
        @Test
        @DisplayName("Diamond Pickaxe with Silktouch")
        void fromJson1() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:diamond_pickaxe",
                  "count": 1,
                  "checkDamage":false,
                  "enchantments": [
                    {
                      "id": "minecraft:silk_touch",
                      "level": 1
                    }
                  ]
                }
                """, JsonObject.class);
            var loaded = EnchantmentIngredient.Serializer.INSTANCE.parse(json);
            var expect = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            assertEquals(json, loaded.toJson());
            assertEquals(expect.toJson(), loaded.toJson());
        }

        @Test
        @DisplayName("Diamond Pickaxe with Efficiency IV")
        void fromJson2() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:diamond_pickaxe",
                  "count": 1,
                  "checkDamage":false,
                  "enchantments": [
                    {
                      "id": "minecraft:efficiency",
                      "level": 4
                    }
                  ]
                }
                """, JsonObject.class);
            var loaded = EnchantmentIngredient.Serializer.INSTANCE.parse(json);
            var expect = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 4)), false);
            assertEquals(json, loaded.toJson());
            assertEquals(expect.toJson(), loaded.toJson());
        }

        @Test
        @DisplayName("Diamond Pickaxe with Efficiency III and Silktouch")
        void fromJson3() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:diamond_pickaxe",
                  "count": 1,
                  "checkDamage":false,
                  "enchantments": [
                    {
                      "id": "minecraft:efficiency",
                      "level": 4
                    },
                    {
                      "id": "minecraft:silk_touch",
                      "level": 1
                    }
                  ]
                }
                """, JsonObject.class);
            var loaded = EnchantmentIngredient.Serializer.INSTANCE.parse(json);
            var expect = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 4),
                new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            assertEquals(json, loaded.toJson());
            assertEquals(expect.toJson(), loaded.toJson());
        }

        @Test
        @DisplayName("Diamond Pickaxe with Silktouch and Efficiency III(order must be kept)")
        void fromJson4() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:diamond_pickaxe",
                  "count": 1,
                  "checkDamage":false,
                  "enchantments": [
                    {
                      "id": "minecraft:silk_touch",
                      "level": 1
                    },
                    {
                      "id": "minecraft:efficiency",
                      "level": 4
                    }
                  ]
                }
                """, JsonObject.class);
            var loaded = EnchantmentIngredient.Serializer.INSTANCE.parse(json);
            var expect = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1),
                new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 4)), false);
            assertEquals(json, loaded.toJson());
            assertEquals(expect.toJson(), loaded.toJson());
        }

        @Test
        @DisplayName("Stone with Silktouch")
        void fromJson5() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:stone",
                  "count": 1,
                  "checkDamage":false,
                  "enchantments": [
                    {
                      "id": "minecraft:silk_touch",
                      "level": 1
                    }
                  ]
                }
                """, JsonObject.class);
            var loaded = EnchantmentIngredient.Serializer.INSTANCE.parse(json);
            var expect = new EnchantmentIngredient(new ItemStack(Items.STONE), List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            assertEquals(json, loaded.toJson());
            assertEquals(expect.toJson(), loaded.toJson());
        }

        @Test
        @DisplayName("Enchantment Book with Silktouch")
        void fromJson6() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:enchanted_book",
                  "count": 1,
                  "checkDamage":false,
                  "enchantments": [
                    {
                      "id": "minecraft:silk_touch",
                      "level": 1
                    }
                  ]
                }
                """, JsonObject.class);
            var loaded = EnchantmentIngredient.Serializer.INSTANCE.parse(json);
            var expect = new EnchantmentIngredient(new ItemStack(Items.ENCHANTED_BOOK), List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            assertEquals(json, loaded.toJson());
            assertEquals(expect.toJson(), loaded.toJson());
        }
    }

    @Nested
    class Match {
        @Test
        void noMatchEmpty() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            assertFalse(ei.test(ItemStack.EMPTY));
        }

        @Test
        void noMatchDifferentItem1() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            var stack1 = new ItemStack(Items.IRON_PICKAXE);
            stack1.enchant(Enchantments.SILK_TOUCH, 1);
            assertFalse(ei.test(stack1));
            ironPickaxe.enchant(Enchantments.SILK_TOUCH, 1);
            assertFalse(ei.test(ironPickaxe));
        }

        @Test
        void noMatchDifferentItem2() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            var stack1 = new ItemStack(Items.IRON_PICKAXE);
            assertFalse(ei.test(stack1));
            assertFalse(ei.test(ironPickaxe));
        }

        @Test
        void noMatchNoEnchantment() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            assertFalse(ei.test(diamondPickaxe));
        }

        @Test
        @DisplayName("Silktouch for Silktouch")
        void match1() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);

            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            assertTrue(ei.test(stack));
        }

        @Test
        @DisplayName("Silktouch for Fortune")
        void noMatch1() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);

            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.enchant(Enchantments.BLOCK_FORTUNE, 1);
            assertFalse(ei.test(stack));
        }

        @Test
        @DisplayName("Silktouch for Silktouch and Efficiency V")
        void match2() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);

            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
            assertTrue(ei.test(stack));
        }

        @Test
        @DisplayName("Efficiency III for Silktouch and Efficiency V")
        void match3() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 3)), false);

            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
            assertTrue(ei.test(stack));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3})
        @DisplayName("Enchanted Book for Fortune III")
        void match4(int i) {
            var e1 = new EnchantmentIngredient(new ItemStack(Items.ENCHANTED_BOOK), List.of(new EnchantmentInstance(Enchantments.BLOCK_FORTUNE, 1)), false);
            var stack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.BLOCK_FORTUNE, i));
            assertTrue(e1.test(stack));
        }
    }
}
