package com.yogpc.qp.gametest;

import java.util.Map;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.testutil.GameTestUtil;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
public final class DropTest {
    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void existence(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).above();

        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, Holder.BLOCK_QUARRY))
            .thenExecuteAfter(1, () -> helper.assertBlockPresent(Holder.BLOCK_QUARRY, pos))
            .thenExecuteAfter(1, () -> {
                var tile = helper.getBlockEntity(pos);
                assertTrue(tile instanceof TileQuarry);
            })
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void dropOfQuarryPlus1(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).above();

        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, Holder.BLOCK_QUARRY))
            .thenExecuteAfter(1, () -> {
                var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                    helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
                assertEquals(1, drops.size(), "Drop was " + drops);
                var stack = drops.get(0);
                assertAll(
                    () -> assertEquals(Holder.BLOCK_QUARRY.blockItem, stack.getItem()),
                    () -> assertEquals(1, stack.getCount()),
                    () -> assertFalse(stack.hasTag(), "Stack: %s, %s".formatted(stack, stack.getTag()))
                );
            })
            .thenSucceed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void dropOfQuarryPlus2(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).above();
        var enchantments = Map.of(
            Enchantments.BLOCK_EFFICIENCY, 3
        );

        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, Holder.BLOCK_QUARRY))
            .thenExecuteAfter(1, () -> {
                if (helper.getBlockEntity(pos) instanceof TileQuarry quarry) {
                    quarry.setEnchantments(enchantments);
                } else {
                    fail("Tile at %s is not Quarry but %s.".formatted(pos, helper.getBlockEntity(pos)));
                }
            })
            .thenExecuteAfter(1, () -> {
                var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                    helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
                assertEquals(1, drops.size(), "Drop was " + drops);
                var stack = drops.get(0);
                var tag = stack.getTag();
                assertAll(
                    () -> assertEquals(Holder.BLOCK_QUARRY.blockItem, stack.getItem()),
                    () -> assertEquals(1, stack.getCount()),
                    () -> assertNotNull(tag, "Stack: %s, %s".formatted(stack, stack.getTag())),
                    () -> assertEquals(enchantments, EnchantmentHelper.getEnchantments(stack))
                );
            })
            .thenSucceed();
    }
}
