package com.yogpc.qp.machine.mover;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import com.yogpc.qp.machine.QpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MoverEntityTest extends BeforeMC {

    @BeforeEach
    void setUp() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        when(type.isValid(any())).thenReturn(true);
        PlatformAccess.RegisterObjects registerObjects = mock(PlatformAccess.RegisterObjects.class);
        when(registerObjects.getBlockEntityType(any())).thenReturn(Optional.of(type));
        PlatformAccess spied = spy(PlatformAccessDelegate.createVanilla());
        doReturn(registerObjects).when(spied).registerObjects();

        PlatformAccessDelegate delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
        delegate.setAccess(spied);
    }

    private static class DummyBlock extends QpBlock {
        public DummyBlock() {
            super(Properties.of(), "dummy", b -> new BlockItem(b, new Item.Properties()));
        }

        @Override
        protected QpBlock createBlock(Properties properties) {
            return this;
        }
    }

    @Test
    void instance() {
        var instance = assertDoesNotThrow(() -> new MoverEntity(BlockPos.ZERO, new DummyBlock().defaultBlockState()));
        assertNotNull(instance);
    }

    @Test
    void initial() {
        var instance = assertDoesNotThrow(() -> new MoverEntity(BlockPos.ZERO, new DummyBlock().defaultBlockState()));
        assertTrue(instance.inventory.isEmpty());
        assertTrue(instance.movableEnchantments.isEmpty());
    }

    @Test
    void canPlace() {
        var instance = assertDoesNotThrow(() -> new MoverEntity(BlockPos.ZERO, new DummyBlock().defaultBlockState()));
        assertAll(
            () -> assertFalse(instance.inventory.canPlaceItem(0, ItemStack.EMPTY)),
            () -> assertFalse(instance.inventory.canPlaceItem(0, Items.DIAMOND_PICKAXE.getDefaultInstance())),
            () -> assertFalse(instance.inventory.canPlaceItem(0, Items.NETHERITE_PICKAXE.getDefaultInstance()))
        );
    }

    @ParameterizedTest
    @MethodSource
    void canPlaceEnchanted(Item item) {
        var instance = assertDoesNotThrow(() -> new MoverEntity(BlockPos.ZERO, new DummyBlock().defaultBlockState()));

        var stack = item.getDefaultInstance();
        var enchantments = mock(ItemEnchantments.class);
        when(enchantments.isEmpty()).thenReturn(false);
        stack.set(DataComponents.ENCHANTMENTS, enchantments);

        assertTrue(instance.inventory.canPlaceItem(0, stack));
    }

    static Stream<Item> canPlaceEnchanted() {
        return Stream.of(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.BOW, Items.DIAMOND_AXE, Items.NETHERITE_AXE, Items.DIAMOND_HOE, Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL);
    }

    @ParameterizedTest
    @MethodSource
    void canNotPlaceEnchanted(Item item) {
        var instance = assertDoesNotThrow(() -> new MoverEntity(BlockPos.ZERO, new DummyBlock().defaultBlockState()));

        var stack = item.getDefaultInstance();
        var enchantments = mock(ItemEnchantments.class);
        when(enchantments.isEmpty()).thenReturn(false);
        stack.set(DataComponents.ENCHANTMENTS, enchantments);

        assertFalse(instance.inventory.canPlaceItem(0, stack));
    }

    static Stream<Item> canNotPlaceEnchanted() {
        return Stream.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND);
    }
}
