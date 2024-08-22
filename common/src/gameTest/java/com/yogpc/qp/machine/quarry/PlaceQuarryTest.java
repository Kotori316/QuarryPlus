package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.machine.module.ModuleInventoryHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaceQuarryTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get()))
            .thenExecuteAfter(1, () ->
                assertInstanceOf(QuarryBlock.class, helper.getBlockState(base).getBlock())
            )
            .thenExecuteAfter(1, () ->
                assertInstanceOf(QuarryEntity.class, helper.getBlockEntity(base))
            )
            .thenSucceed();
    }

    public static void checkDropNormal(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get()))
            .thenExecuteAfter(1, () -> {
                var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
                assertFalse(drop.isEmpty());
                var quarryStack = drop.getFirst();
                assertInstanceOf(QuarryItem.class, quarryStack.getItem());
                assertFalse(quarryStack.isEnchanted());
            })
            .thenSucceed();
    }

    public static void checkDropEnchanted(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get()))
            .thenExecute(() -> {
                var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 3);
                helper.<QuarryEntity>getBlockEntity(base).setEnchantments(mutable.toImmutable());
            })
            .thenExecuteAfter(1, () -> {
                var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
                assertFalse(drop.isEmpty());
                var quarryStack = drop.getFirst();
                assertInstanceOf(QuarryItem.class, quarryStack.getItem());
                assertTrue(quarryStack.isEnchanted());
                assertEquals(1, quarryStack.getEnchantments().size());
                assertEquals(3, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
            })
            .thenSucceed();
    }

    public static void checkDropEnchanted2(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get()))
            .thenExecute(() -> {
                var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 5);
                mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
                helper.<QuarryEntity>getBlockEntity(base).setEnchantments(mutable.toImmutable());
            })
            .thenExecuteAfter(1, () -> {
                var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
                var quarryStack = drop.getFirst();
                assertInstanceOf(QuarryItem.class, quarryStack.getItem());
                assertTrue(quarryStack.isEnchanted());
                assertEquals(2, quarryStack.getEnchantments().size());
                assertEquals(5, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
                assertEquals(3, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
            })
            .thenSucceed();
    }

    public static void saveEnchantment(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(base);

        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 5);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
        quarry.setEnchantments(mutable.toImmutable());

        var saved = quarry.saveWithFullMetadata(helper.getLevel().registryAccess());
        quarry.loadWithComponents(saved, helper.getLevel().registryAccess());

        var enchantments = quarry.getEnchantments();
        assertEquals(5, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(3, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        assertEquals(0, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.FORTUNE)));

        helper.succeed();
    }

    public static void accessModuleInventory(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(base);
        var moduleInv = ModuleInventoryHolder.getFromObject(quarry);
        assertTrue(moduleInv.isPresent());
        assertTrue(moduleInv.get().getModules().isEmpty());

        helper.succeed();
    }
}
