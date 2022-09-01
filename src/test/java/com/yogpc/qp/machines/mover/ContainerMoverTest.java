package com.yogpc.qp.machines.mover;

import java.util.List;
import java.util.function.Predicate;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(QuarryPlusTest.class)
class ContainerMoverTest {

    @Test
    void moveEnchantment() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);

        ContainerMover.moveEnchantment(Enchantments.BLOCK_EFFICIENCY, from, to, e -> true, () -> {
        });
        assertAll(
            () -> assertEquals(4, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from)),
            () -> assertEquals(1, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, to))
        );
        ContainerMover.moveEnchantment(Enchantments.BLOCK_EFFICIENCY, from, to, e -> true, () -> {
        });
        assertAll(
            () -> assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from)),
            () -> assertEquals(2, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, to))
        );
    }

    @Test
    void noMoveLevel() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        to.enchant(Enchantments.BLOCK_EFFICIENCY, 5);

        ContainerMover.moveEnchantment(Enchantments.BLOCK_EFFICIENCY, from, to, e -> true, () -> fail("Should not be reached."));
        assertEquals(5, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from));
        assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, from));
        assertEquals(5, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, to));
    }

    @Test
    void noMoveCompatible() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        from.enchant(Enchantments.BLOCK_FORTUNE, 3);
        to.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        to.enchant(Enchantments.SILK_TOUCH, 1);

        ContainerMover.moveEnchantment(Enchantments.BLOCK_FORTUNE, from, to, e -> true, () -> fail("Should not be reached."));
        assertEquals(5, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from));
        assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, from));
        assertEquals(5, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, to));

        assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, from));
        assertEquals(0, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, to));
        assertEquals(1, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, to));
    }

    @Test
    void noMoveAcceptable() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);

        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        from.enchant(Enchantments.BLOCK_FORTUNE, 3);
        Predicate<Enchantment> predicate = Predicate.isEqual(Enchantments.BLOCK_EFFICIENCY);

        ContainerMover.moveEnchantment(Enchantments.UNBREAKING, from, to, predicate, () -> fail("Should not be reached."));
        assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, from));
        assertEquals(0, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, to));

        ContainerMover.moveEnchantment(Enchantments.BLOCK_EFFICIENCY, from, to, predicate, () -> {
        });
        assertEquals(4, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from));
        assertEquals(1, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, to));
    }

    @Test
    void downLevel1() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);

        ContainerMover.downLevel(Enchantments.BLOCK_EFFICIENCY, from);
        assertEquals(4, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from));
        assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, from));
    }

    @Test
    void downLevel2() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 1);

        ContainerMover.downLevel(Enchantments.BLOCK_EFFICIENCY, from);
        assertTrue(from.getEnchantmentTags().isEmpty());
    }

    @Test
    void downLevel3() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 1);
        from.enchant(Enchantments.UNBREAKING, 3);

        ContainerMover.downLevel(Enchantments.BLOCK_EFFICIENCY, from);
        assertFalse(from.getEnchantmentTags().isEmpty());
        assertEquals(0, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from));
        assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, from));
    }

    @Test
    void upLevel1() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        ContainerMover.upLevel(Enchantments.BLOCK_EFFICIENCY, from);
        assertEquals(1, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from));
    }

    @Test
    void upLevel2() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 1);
        ContainerMover.upLevel(Enchantments.BLOCK_EFFICIENCY, from);
        assertEquals(2, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, from));
    }

    @Test
    void getMovable1() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);

        var movable = ContainerMover.getMovable(from, to, e -> true);
        assertEquals(List.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING), movable);
    }

    @Test
    void getMovableOrder() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.UNBREAKING, 3);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);

        var movable = ContainerMover.getMovable(from, to, e -> true);
        assertEquals(List.of(Enchantments.UNBREAKING, Enchantments.BLOCK_EFFICIENCY), movable);
    }

    @Test
    void getMovableLimited() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        to.enchant(Enchantments.BLOCK_EFFICIENCY, 5);

        var movable = ContainerMover.getMovable(from, to, e -> true);
        assertEquals(List.of(Enchantments.UNBREAKING), movable, "Limited to unbreaking");
    }

    @Test
    void getMovableNonLimited() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        to.enchant(Enchantments.BLOCK_EFFICIENCY, 4);

        var movable = ContainerMover.getMovable(from, to, e -> true);
        assertEquals(List.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING), movable);
    }

    @Test
    void getMovable3() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        from.enchant(Enchantments.BLOCK_FORTUNE, 3);

        var movable = ContainerMover.getMovable(from, to, e -> true);
        assertEquals(List.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE), movable);
    }

    @Test
    void getMovableFortune() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        from.enchant(Enchantments.BLOCK_FORTUNE, 3);
        to.enchant(Enchantments.SILK_TOUCH, 1);

        var movable = ContainerMover.getMovable(from, to, e -> true);
        assertEquals(List.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING), movable);
    }

    @Test
    void getMovableSilktouch() {
        var from = new ItemStack(Items.DIAMOND_PICKAXE);
        var to = new ItemStack(Items.NETHERITE_PICKAXE);
        from.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
        from.enchant(Enchantments.UNBREAKING, 3);
        from.enchant(Enchantments.SILK_TOUCH, 1);
        to.enchant(Enchantments.BLOCK_FORTUNE, 1);

        var movable = ContainerMover.getMovable(from, to, e -> true);
        assertEquals(List.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING), movable);
    }
}
