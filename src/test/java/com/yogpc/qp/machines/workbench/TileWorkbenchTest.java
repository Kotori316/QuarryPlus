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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class TileWorkbenchTest {
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

    @Nested
    class ExtractionTest {
        @Test
        void cantExtractItems() {
            var defaultValue = QuarryPlus.config.common.allowWorkbenchExtraction.get();
            try {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(false);
                var tile = tile();
                tile.setItem(0, new ItemStack(Items.APPLE, 64));

                var handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new);
                var extracted = handler.extractItem(0, 1, true);
                assertTrue(extracted.isEmpty());
            } finally {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(defaultValue);
            }
        }

        @Test
        void canExtractItems() {
            var defaultValue = QuarryPlus.config.common.allowWorkbenchExtraction.get();
            try {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(true);
                var tile = tile();
                tile.setItem(0, new ItemStack(Items.APPLE, 64));

                var handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new);
                var extracted = handler.extractItem(0, 1, false);
                assertTrue(ItemStack.matches(extracted, new ItemStack(Items.APPLE, 1)),
                    "Extracted: %s".formatted(extracted));

                var tileItem = tile.getItem(0);
                assertTrue(ItemStack.matches(tileItem, new ItemStack(Items.APPLE, 63)),
                    "Item in the tile: %s".formatted(tileItem));
            } finally {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(defaultValue);
            }
        }

        @Test
        void canExtractItemsSimulate() {
            var defaultValue = QuarryPlus.config.common.allowWorkbenchExtraction.get();
            try {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(true);
                var tile = tile();
                tile.setItem(0, new ItemStack(Items.APPLE, 64));

                var handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new);
                var extracted = handler.extractItem(0, 1, true);
                assertTrue(ItemStack.matches(extracted, new ItemStack(Items.APPLE, 1)),
                    "Extracted: %s".formatted(extracted));

                var tileItem = tile.getItem(0);
                assertTrue(ItemStack.matches(tileItem, new ItemStack(Items.APPLE, 64)),
                    "Item in the tile: %s".formatted(tileItem));
            } finally {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(defaultValue);
            }
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            256,128
            64,32
            1,1
            64,64
            512,1
            2000,32
            """)
        void canExtractMoreItems(int initial, int extractCount) {
            var defaultValue = QuarryPlus.config.common.allowWorkbenchExtraction.get();
            try {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(true);
                var tile = tile();
                tile.setItem(0, new ItemStack(Items.APPLE, initial));

                var handler = tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new);
                var extracted = handler.extractItem(0, extractCount, false);
                assertTrue(ItemStack.matches(extracted, new ItemStack(Items.APPLE, extractCount)),
                    "Extracted: %s".formatted(extracted));

                var tileItem = tile.getItem(0);
                if(initial - extractCount <= 0){
                    assertTrue(tileItem.isEmpty(),
                        "Item in the tile: %s".formatted(tileItem));
                }else{
                    assertTrue(ItemStack.matches(tileItem, new ItemStack(Items.APPLE, initial - extractCount)),
                        "Item in the tile: %s".formatted(tileItem));
                }

            } finally {
                QuarryPlus.config.common.allowWorkbenchExtraction.set(defaultValue);
            }
        }
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
        assertEquals(5L, tile.getMaxEnergy(), "The default energy was changed to 0.");
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
