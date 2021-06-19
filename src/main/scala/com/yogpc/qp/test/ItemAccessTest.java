package com.yogpc.qp.test;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.workbench.WorkbenchRecipes;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

    public static Stream<Block> myBlocksInForgeRegistry() {
        return CollectionConverters.asJava(Holder.blocks()).stream();
    }

    @ParameterizedTest
    @MethodSource
    void myBlocksInForgeRegistry(Block block) {
        IForgeRegistry<Block> blocks = ForgeRegistries.BLOCKS;
        assertTrue(blocks.containsValue(block), "Block is in forge registry.");
    }

    public static Stream<Item> myItemsInForgeRegistry() {
        return CollectionConverters.asJava(Holder.items()).stream();
    }

    @ParameterizedTest
    @MethodSource
    void myItemsInForgeRegistry(Item item) {
        IForgeRegistry<Item> items = ForgeRegistries.ITEMS;
        assertTrue(items.containsValue(item), "Item is in registry.");
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

    public static Stream<Enchantment> accessToEnchantment() {
        return Stream.of(
            Enchantments.SILK_TOUCH,
            Enchantments.SHARPNESS,
            Enchantments.FORTUNE,
            Enchantments.EFFICIENCY
        );
    }

    @ParameterizedTest
    @MethodSource
    void accessToEnchantment(Enchantment enchantment) {
        assertAll(
            () -> assertNotNull(enchantment),
            () -> assertNotNull(enchantment.getRegistryName()),
            () -> assertTrue(ForgeRegistries.ENCHANTMENTS.containsValue(enchantment))
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

    @Test
    void dummy() {
        assertTrue(accessToEnchantment().count() > 0);
        assertTrue(myBlocksInForgeRegistry().count() > 0);
        assertTrue(myItemsInForgeRegistry().count() > 0);
    }
}
