package com.yogpc.qp.gametest.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import com.yogpc.qp.machine.quarry.QuarryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaceQuarryTest {
    static final BlockPos base = BlockPos.ZERO.above();

    static Holder<Enchantment> getEnchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        var reg = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        return reg.getHolderOrThrow(key);
    }

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
                mutable.set(getEnchantment(helper, Enchantments.EFFICIENCY), 3);
                helper.<QuarryEntity>getBlockEntity(base).setEnchantments(mutable.toImmutable());
            })
            .thenExecuteAfter(1, () -> {
                var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
                assertFalse(drop.isEmpty());
                var quarryStack = drop.getFirst();
                assertInstanceOf(QuarryItem.class, quarryStack.getItem());
                assertTrue(quarryStack.isEnchanted());
                assertEquals(1, quarryStack.getEnchantments().size());
                assertEquals(3, quarryStack.getEnchantments().getLevel(getEnchantment(helper, Enchantments.EFFICIENCY)));
            })
            .thenSucceed();
    }

    public static void checkDropEnchanted2(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get()))
            .thenExecute(() -> {
                var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                mutable.set(getEnchantment(helper, Enchantments.EFFICIENCY), 5);
                mutable.set(getEnchantment(helper, Enchantments.UNBREAKING), 3);
                helper.<QuarryEntity>getBlockEntity(base).setEnchantments(mutable.toImmutable());
            })
            .thenExecuteAfter(1, () -> {
                var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
                var quarryStack = drop.getFirst();
                assertInstanceOf(QuarryItem.class, quarryStack.getItem());
                assertTrue(quarryStack.isEnchanted());
                assertEquals(2, quarryStack.getEnchantments().size());
                assertEquals(5, quarryStack.getEnchantments().getLevel(getEnchantment(helper, Enchantments.EFFICIENCY)));
                assertEquals(3, quarryStack.getEnchantments().getLevel(getEnchantment(helper, Enchantments.UNBREAKING)));
            })
            .thenSucceed();
    }
}
