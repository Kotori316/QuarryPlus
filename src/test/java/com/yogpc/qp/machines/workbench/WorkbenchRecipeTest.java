package com.yogpc.qp.machines.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.yogpc.qp.QuarryPlusTest.id;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class WorkbenchRecipeTest {
    CompareRecipe stone = new CompareRecipe(id("test_stone"), new ItemStack(Items.STONE), 10);
    CompareRecipe bedrock = new CompareRecipe(id("test_bedrock"), new ItemStack(Items.BEDROCK), 10);
    CompareRecipe diamond1 = new CompareRecipe(id("test_diamond1"), new ItemStack(Items.DIAMOND, 1), 5);
    CompareRecipe diamond0 = new CompareRecipe(id("test_diamond"), new ItemStack(Items.DIAMOND, 2), 5);

    @Test
    @SuppressWarnings("EqualsWithItself")
        // This is test for comparing itself.
    void compareSame() {
        assertEquals(0, WorkbenchRecipe.COMPARATOR.compare(stone, stone));
        assertEquals(0, WorkbenchRecipe.COMPARATOR.compare(bedrock, bedrock));
    }

    @Test
    void compareStoneBedrock() {
        assertTrue(WorkbenchRecipe.COMPARATOR.compare(stone, bedrock) < 0);
    }

    @Test
    void compareWithEnergy() {
        CompareRecipe stone2 = new CompareRecipe(id("test_stone"), new ItemStack(Items.STONE), 5);
        assertTrue(WorkbenchRecipe.COMPARATOR.compare(stone, stone2) > 0, "expect stone2 < stone");
    }

    @Test
    void compareSameEnergy() {
        assertTrue(WorkbenchRecipe.COMPARATOR.compare(stone, bedrock) < 0);
        CompareRecipe glass = new CompareRecipe(id("test_glass"), new ItemStack(Items.GLASS), 10);
        assertTrue(WorkbenchRecipe.COMPARATOR.compare(bedrock, glass) < 0);
    }

    @Test
    void compareSameItem0() {
        assertAll(
            () -> assertNotEquals(diamond0, diamond1),
            () -> assertNotEquals(0, WorkbenchRecipe.COMPARATOR.compare(diamond0, diamond1)),
            () -> assertTrue(WorkbenchRecipe.COMPARATOR.compare(diamond0, diamond1) < 0),
            () -> assertTrue(WorkbenchRecipe.COMPARATOR.compare(diamond1, diamond0) > 0)
        );
    }

    @Test
    void sort() {
        Random random = new Random(654);
        List<WorkbenchRecipe> expected = List.of(diamond0, diamond1, stone, bedrock);
        List<WorkbenchRecipe> randomized = new ArrayList<>(expected);
        Collections.shuffle(randomized, random);

        assertNotEquals(expected, randomized);
        randomized.sort(WorkbenchRecipe.COMPARATOR);
        assertIterableEquals(expected, randomized);
    }

    @ParameterizedTest
    @MethodSource("itemEnergy")
    void compareSameItem(Item item, long energy) {
        var r1 = new CompareRecipe(id("test_r1"), new ItemStack(item, 5), energy);
        var r2 = new CompareRecipe(id("test_r2"), new ItemStack(item, 10), energy);
        var r3 = new CompareRecipe(id("test_r3"), new ItemStack(item, 1), energy);

        assertAll(
            () -> assertNotEquals(0, WorkbenchRecipe.COMPARATOR.compare(r1, r2)),
            () -> assertNotEquals(0, WorkbenchRecipe.COMPARATOR.compare(r2, r3)),
            () -> assertTrue(WorkbenchRecipe.COMPARATOR.compare(r1, r2) < 0),
            () -> assertTrue(WorkbenchRecipe.COMPARATOR.compare(r2, r1) > 0),
            () -> assertTrue(WorkbenchRecipe.COMPARATOR.compare(r2, r3) < 0),
            () -> assertTrue(WorkbenchRecipe.COMPARATOR.compare(r3, r2) > 0)
        );
    }

    static Stream<Object[]> itemEnergy() {
        return Stream.of(Items.APPLE, Items.COBBLESTONE, Items.PACKED_ICE, Items.GOLDEN_PICKAXE, Items.ELYTRA)
            .flatMap(i -> LongStream.of(1, 54, 10, 54221, 600 * PowerTile.ONE_FE).boxed().map(l ->
                new Object[]{i, l}
            ));
    }

    private static class CompareRecipe extends WorkbenchRecipe {
        public CompareRecipe(ResourceLocation location, ItemStack output, long energy) {
            super(location, output, energy, true);
        }

        @Override
        public List<IngredientList> inputs() {
            return List.of();
        }

        @Override
        protected String getSubTypeName() {
            return "compare";
        }

        @Override
        protected ItemStack getOutput(List<ItemStack> inventory) {
            return getResultItem().copy();
        }
    }
}
