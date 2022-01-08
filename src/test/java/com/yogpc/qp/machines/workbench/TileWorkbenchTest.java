package com.yogpc.qp.machines.workbench;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileWorkbenchTest extends QuarryPlusTest {
    static TileWorkbench tile() {
        return new TileWorkbench(BlockPos.ZERO, Holder.BLOCK_WORKBENCH.defaultBlockState());
    }

    @Test
    void createInstance() {
        assertDoesNotThrow(TileWorkbenchTest::tile);
    }

    @Test
    void initialCapacity() {
        var tile = tile();
        assertEquals(5L, tile.getMaxEnergy() / PowerTile.ONE_FE);
    }

    WorkbenchRecipe r1 = RecipeFinderTest.create("stone", 100L, new ItemStack(Items.STONE, 64), new ItemStack(Items.COBBLESTONE, 64));
    WorkbenchRecipe r2 = RecipeFinderTest.create("dirt1", 200L, new ItemStack(Items.STONE), new ItemStack(Items.DIRT, 4));
    WorkbenchRecipe r3 = RecipeFinderTest.create("iron1", 300L, new ItemStack(Items.STONE, 64), new ItemStack(Items.IRON_INGOT));
    WorkbenchRecipe r4 = RecipeFinderTest.create("wood1", 400L, new ItemStack(Items.OAK_LOG, 32), new ItemStack(Items.IRON_INGOT));
    WorkbenchRecipe r5 = RecipeFinderTest.create("dirt2", 500L, new ItemStack(Items.COBBLESTONE, 48), new ItemStack(Items.DIRT, 24));
    WorkbenchRecipe r6 = RecipeFinderTest.create("iron2", 600L, new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.IRON_INGOT, 16));
    WorkbenchRecipe r7 = new IngredientRecipe(new ResourceLocation(QuarryPlus.modID, "test_100"), new ItemStack(Items.DIAMOND, 4), 100 * PowerTile.ONE_FE,
        false, RecipeFinderTest.createList(new ItemStack(Items.STONE, 64), new ItemStack(Items.IRON_INGOT, 64)));

    @BeforeEach
    void setup() {
        WorkbenchRecipe.recipeFinder = SimpleRecipeFinder.create(List.of(r1, r2, r3, r4, r5, r6, r7));
    }

    @AfterEach
    void postTest() {
        WorkbenchRecipe.recipeFinder = new DefaultFinder();
    }

    @Test
    void setRecipe2() {
        var tile = tile();
        tile.setItem(0, new ItemStack(Items.DIRT, 16));
        tile.updateRecipeList();
        assertEquals(5L, tile.getMaxEnergy() / PowerTile.ONE_FE); // Not updated yet.
        tile.setCurrentRecipe(new ResourceLocation(QuarryPlus.modID, "test_dirt1"));
        assertEquals(200L, tile.getMaxEnergy());
    }

    @Test
    void setRecipe5_fail() {
        var tile = tile();
        tile.setItem(0, new ItemStack(Items.DIRT, 16));
        tile.updateRecipeList();
        assertEquals(5L, tile.getMaxEnergy() / PowerTile.ONE_FE); // Not updated yet.
        tile.setCurrentRecipe(new ResourceLocation(QuarryPlus.modID, "test_dirt2"));
        assertEquals(0L, tile.getMaxEnergy());
        assertFalse(tile.getRecipe().hasContent());
    }

    @Test
    void setRecipe5() {
        var tile = tile();
        tile.setItem(0, new ItemStack(Items.DIRT, 24));
        tile.updateRecipeList();
        assertEquals(5L, tile.getMaxEnergy() / PowerTile.ONE_FE); // Not updated yet.
        tile.setCurrentRecipe(new ResourceLocation(QuarryPlus.modID, "test_dirt2"));
        assertEquals(500L, tile.getMaxEnergy());
        assertTrue(tile.getRecipe().hasContent());
    }

    @Test
    void setRecipe7() {
        var tile = tile();
        tile.setItem(0, new ItemStack(Items.DIRT, 24));
        tile.setItem(1, new ItemStack(Items.STONE, 64));
        tile.setItem(5, new ItemStack(Items.IRON_INGOT, 64));
        tile.updateRecipeList();
        assertEquals(5L, tile.getMaxEnergy() / PowerTile.ONE_FE); // Not updated yet.
        tile.setCurrentRecipe(new ResourceLocation(QuarryPlus.modID, "test_100"));
        assertEquals(100 * PowerTile.ONE_FE, tile.getMaxEnergy());
        assertTrue(tile.getRecipe().hasContent());
    }
}