package com.yogpc.qp.machines.workbench;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(QuarryPlusTest.class)
class EnchantmentIngredientTest {
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
    class FromJsonTest {
        @Test
        @DisplayName("Diamond Pickaxe with Silktouch")
        void fromJson1() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:diamond_pickaxe",
                  "count": 1,
                  "checkDamage": false,
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
                  "checkDamage": false,
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
                  "checkDamage": false,
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
                  "checkDamage": false,
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
                  "checkDamage": false,
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
                  "checkDamage": false,
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

        @Test
        @DisplayName("checkDamage: true")
        void fromJson7() {
            // language=json
            var json = GSON.fromJson("""
                {
                  "type": "quarryplus:enchantment_ingredient",
                  "item": "minecraft:diamond_pickaxe",
                  "count": 1,
                  "nbt": {
                    "Damage": 0
                  },
                  "checkDamage": true,
                  "enchantments": [
                    {
                      "id": "minecraft:silk_touch",
                      "level": 1
                    }
                  ]
                }
                                """, JsonObject.class);
            var loaded = EnchantmentIngredient.Serializer.INSTANCE.parse(json);
            var expect = new EnchantmentIngredient(new ItemStack(Items.DIAMOND_PICKAXE), List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), true);
            assertEquals(json, loaded.toJson());
            assertEquals(expect.toJson(), loaded.toJson());
        }
    }

    @Nested
    class MatchTest {
        @Test
        void noMatchEmpty1() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);
            assertFalse(ei.test(ItemStack.EMPTY));
        }

        @Test
        void noMatchEmpty2() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(), false);
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
        @DisplayName("Match if ingredient has no enchantment.")
        void matchNoEnchantment() {
            var ei = new EnchantmentIngredient(diamondPickaxe, List.of(), false);
            assertAll(
                () -> assertTrue(ei.test(diamondPickaxe)),
                () -> assertTrue(ei.test(new ItemStack(Items.DIAMOND_PICKAXE))),
                () -> assertFalse(ei.test(new ItemStack(Items.IRON_PICKAXE))),
                () -> {
                    var s = new ItemStack(Items.DIAMOND_PICKAXE);
                    s.enchant(Enchantments.SILK_TOUCH, 1);
                    assertTrue(ei.test(s));
                }, () -> {
                    var s = new ItemStack(Items.DIAMOND_PICKAXE);
                    s.enchant(Enchantments.SILK_TOUCH, 1);
                    s.enchant(Enchantments.BLOCK_EFFICIENCY, 1);
                    assertTrue(ei.test(s));
                }
            );
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

        @Test
        void match5() {
            var stack = new ItemStack(Items.APPLE);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            stack.getOrCreateTag().putString("stringKey", "stringValue");
            var i = new EnchantmentIngredient(stack, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);

            var s2 = new ItemStack(Items.APPLE);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            assertAll(
                () -> assertTrue(i.test(stack)),
                () -> assertFalse(i.test(new ItemStack(Items.APPLE))),
                () -> assertFalse(i.test(s2))
            );
        }

        @Test
        void match6() {
            var stack = new ItemStack(Items.APPLE);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            var i = new EnchantmentIngredient(stack, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false);

            var s2 = stack.copy();
            s2.getOrCreateTag().putString("stringKey", "stringValue");
            assertAll(
                () -> assertTrue(i.test(stack)),
                () -> assertFalse(i.test(new ItemStack(Items.APPLE))),
                () -> assertTrue(i.test(s2))
            );
        }

        @Nested
        class ManyEnchantmentTest {
            EnchantmentIngredient e1 = new EnchantmentIngredient(new ItemStack(Items.DIAMOND_PICKAXE), List.of(
                new EnchantmentInstance(Enchantments.BLOCK_EFFICIENCY, 1),
                new EnchantmentInstance(Enchantments.UNBREAKING, 2)
            ), false);

            static Stream<Object[]> e1Level() {
                return IntStream.rangeClosed(1, 5).boxed().flatMap(e ->
                    IntStream.rangeClosed(2, 3).mapToObj(u -> new Object[]{e, u}));
            }

            @ParameterizedTest
            @MethodSource("e1Level")
            void matchMany(int efficiencyLevel, int unbreakingLevel) {
                var stack = new ItemStack(Items.DIAMOND_PICKAXE);
                stack.enchant(Enchantments.BLOCK_EFFICIENCY, efficiencyLevel);
                stack.enchant(Enchantments.UNBREAKING, unbreakingLevel);
                assertTrue(e1.test(stack));
            }

            @ParameterizedTest
            @MethodSource("e1Level")
            void matchManyWithFortune(int efficiencyLevel, int unbreakingLevel) {
                var stack = new ItemStack(Items.DIAMOND_PICKAXE);
                stack.enchant(Enchantments.BLOCK_EFFICIENCY, efficiencyLevel);
                stack.enchant(Enchantments.UNBREAKING, unbreakingLevel);
                stack.enchant(Enchantments.BLOCK_FORTUNE, 2);
                assertTrue(e1.test(stack));
            }

            @Test
            @DisplayName("E=1, U=1")
            void notMatch1() {
                var efficiencyLevel = 1;
                var unbreakingLevel = 1;
                var stack = new ItemStack(Items.DIAMOND_PICKAXE);
                stack.enchant(Enchantments.BLOCK_EFFICIENCY, efficiencyLevel);
                stack.enchant(Enchantments.UNBREAKING, unbreakingLevel);
                assertFalse(e1.test(stack));
            }

            @ParameterizedTest
            @ValueSource(ints = {1, 2, 3, 4, 5})
            void onlyEfficiency(int efficiencyLevel) {
                var stack = new ItemStack(Items.DIAMOND_PICKAXE);
                stack.enchant(Enchantments.BLOCK_EFFICIENCY, efficiencyLevel);
                assertFalse(e1.test(stack));
            }

            @ParameterizedTest
            @ValueSource(ints = {1, 2, 3, 4, 5})
            void onlyUnbreaking(int unbreakingLevel) {
                var stack = new ItemStack(Items.DIAMOND_PICKAXE);
                stack.enchant(Enchantments.UNBREAKING, unbreakingLevel);
                assertFalse(e1.test(stack));
            }
        }
    }

    @Nested
    class CheckDamageTest {
        EnchantmentIngredient checkDamage = new EnchantmentIngredient(getDamaged(20), List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)),
            true);

        static ItemStack getDamaged(int damage) {
            var stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.setDamageValue(damage);
            return stack;
        }

        @Test
        void sameValue() {
            var stack = getDamaged(20);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            assertTrue(checkDamage.test(stack));
        }

        @Test
        void low() {
            var stack = getDamaged(19);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            assertFalse(checkDamage.test(stack));
        }

        @Test
        void high() {
            var stack = getDamaged(21);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            assertFalse(checkDamage.test(stack));
        }
    }

    @Nested
    class NoDamageTest {
        @Test
        @DisplayName("No check of damage. No damage tag. Diamond")
        void noCheckNoDamage1() {
            var ingredient = new EnchantmentIngredient(diamondPickaxe, List.of(), false);
            assertTrue(ingredient.test(new ItemStack(Items.DIAMOND_PICKAXE)), "Diamond pickaxe should match.");
            assertTrue(ingredient.test(diamondPickaxe), "Diamond pickaxe without damage should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(15)), "Diamond pickaxe with damage(15) should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(1)), "Diamond pickaxe with damage(1) should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(0)), "Diamond pickaxe with damage(0) should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(100)), "Diamond pickaxe with damage(100) should match.");
        }

        @Test
        @DisplayName("No check of damage. Damage tag. Diamond")
        void noCheckNoDamage2() {
            var ingredient = new EnchantmentIngredient(new ItemStack(Items.DIAMOND_PICKAXE), List.of(), false);
            assertTrue(ingredient.test(new ItemStack(Items.DIAMOND_PICKAXE)), "Diamond pickaxe should match.");
            assertTrue(ingredient.test(diamondPickaxe), "Diamond pickaxe without damage should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(15)), "Diamond pickaxe with damage(15) should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(1)), "Diamond pickaxe with damage(1) should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(0)), "Diamond pickaxe with damage(0) should match.");
            assertTrue(ingredient.test(CheckDamageTest.getDamaged(100)), "Diamond pickaxe with damage(100) should match.");
        }

        @Test
        @DisplayName("No check of damage. Damage tag. invalid item")
        void noCheckNoDamage3() {
            var ingredient = new EnchantmentIngredient(new ItemStack(Items.IRON_PICKAXE), List.of(), false);
            assertFalse(ingredient.test(new ItemStack(Items.DIAMOND_PICKAXE)), "Diamond pickaxe shouldn't match.");
            assertFalse(ingredient.test(diamondPickaxe), "Diamond pickaxe without damage shouldn't match.");
            assertFalse(ingredient.test(CheckDamageTest.getDamaged(15)), "Diamond pickaxe with damage(15) shouldn't match.");
            assertFalse(ingredient.test(CheckDamageTest.getDamaged(1)), "Diamond pickaxe with damage(1) shouldn't match.");
            assertFalse(ingredient.test(CheckDamageTest.getDamaged(0)), "Diamond pickaxe with damage(0) shouldn't match.");
            assertFalse(ingredient.test(CheckDamageTest.getDamaged(100)), "Diamond pickaxe with damage(100) shouldn't match.");
        }
    }
}
