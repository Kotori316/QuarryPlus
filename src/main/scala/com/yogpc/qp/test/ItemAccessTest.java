package com.yogpc.qp.test;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.workbench.WorkbenchRecipes;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.junit.jupiter.api.Test;
import scala.jdk.javaapi.CollectionConverters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ItemAccessTest {
    @Test
    void exist() {
        Item item = Holder.itemStatusChecker();
        assertEquals(new ResourceLocation(QuarryPlus.modID, QuarryPlus.Names.statuschecker), item.getRegistryName());
    }

    @Test
    void myItemsInForgeRegistry() {
        IForgeRegistry<Block> blocks = ForgeRegistries.BLOCKS;
        assertTrue(CollectionConverters.asJava(Holder.blocks())
            .stream().allMatch(blocks::containsValue), "All blocks are in forge registry.");
        IForgeRegistry<Item> items = ForgeRegistries.ITEMS;
        assertTrue(CollectionConverters.asJava(Holder.items()).stream().allMatch(items::containsValue), "All items are in registry.");
    }

    @Test
    void accessToClass() {
        assertDoesNotThrow(() -> new ItemStack(Items.HEART_OF_THE_SEA));
        WorkbenchRecipes recipes = WorkbenchRecipes.dummyRecipe();
        assertAll(
            () -> assertTrue(recipes.getRecipeOutput().isEmpty()),
            () -> assertFalse(recipes.hasContent())
        );
    }

    @Test
    void accessToEnchantment() {
        assertAll(
            () -> assertNotNull(Enchantments.SILK_TOUCH),
            () -> assertNotNull(Enchantments.SHARPNESS.getRegistryName()),
            () -> assertTrue(ForgeRegistries.ENCHANTMENTS.containsValue(Enchantments.FORTUNE))
        );
    }

    @Test
    void CANT_AccessToCapability() {
        assertAll(
            () -> assertNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY),
            () -> assertNull(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY),
            () -> assertNull(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY),
            () -> assertNull(CapabilityEnergy.ENERGY)
        );
    }
}
