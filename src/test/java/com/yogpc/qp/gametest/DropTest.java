package com.yogpc.qp.gametest;

import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.advpump.TileAdvPump;
import com.yogpc.qp.machines.advpump.TileAdvPumpSetter;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.mini_quarry.MiniQuarryTile;
import com.yogpc.qp.machines.miningwell.MiningWellTile;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@GameTestHolder(QuarryPlus.modID)
@GameTestDontPrefix
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

    private static List<Map<Enchantment, Integer>> enchantmentList() {
        return List.of(
            Map.of(Enchantments.BLOCK_EFFICIENCY, 3),
            Map.of(Enchantments.BLOCK_EFFICIENCY, 2, Enchantments.UNBREAKING, 1),
            Map.of(Enchantments.BLOCK_EFFICIENCY, 5, Enchantments.UNBREAKING, 3),
            Map.of(Enchantments.UNBREAKING, 2),
            Map.of()
        );
    }

    private static String getPostFix(Map<Enchantment, Integer> map) {
        if (map.isEmpty()) return "none";
        return map.entrySet().stream().map(e -> "%s%d".formatted(
                Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(e.getKey())).getPath().charAt(0),
                e.getValue()
            ))
            .collect(Collectors.joining());
    }

    @GameTestGenerator
    public List<TestFunction> dropOfQuarryPlus() {
        return enchantmentList().stream().map(enchantments ->
            GameTestUtil.create(QuarryPlus.modID, "defaultBatch", "quarry_drop_" + getPostFix(enchantments),
                g -> dropOfMachine(g, enchantments, Holder.BLOCK_QUARRY))
        ).toList();
    }

    @GameTestGenerator
    public List<TestFunction> dropOfMiniQuarry() {
        return enchantmentList().stream().map(enchantments ->
            GameTestUtil.create(QuarryPlus.modID, "defaultBatch", "mini_quarry_drop_" + getPostFix(enchantments),
                g -> dropOfMachine(g, enchantments, Holder.BLOCK_MINI_QUARRY))
        ).toList();
    }

    @GameTestGenerator
    public List<TestFunction> dropOfAdvQuarry() {
        return enchantmentList().stream().map(enchantments ->
            GameTestUtil.create(QuarryPlus.modID, "defaultBatch", "adv_quarry_drop_" + getPostFix(enchantments),
                g -> dropOfMachine(g, enchantments, Holder.BLOCK_ADV_QUARRY))
        ).toList();
    }

    @GameTestGenerator
    public List<TestFunction> dropOfAdvPump() {
        return enchantmentList().stream().map(enchantments ->
            GameTestUtil.create(QuarryPlus.modID, "defaultBatch", "adv_pump_drop_" + getPostFix(enchantments),
                g -> dropOfMachine(g, enchantments, Holder.BLOCK_ADV_PUMP))
        ).toList();
    }

    private void dropOfMachine(GameTestHelper helper, Map<Enchantment, Integer> enchantments, QPBlock block) {
        var pos = GameTestUtil.getBasePos(helper).above();

        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, block))
            .thenExecuteAfter(1, () -> {
                var entity = helper.getBlockEntity(pos);
                if (entity instanceof TileQuarry quarry) {
                    quarry.setEnchantments(enchantments);
                } else if (entity instanceof MiniQuarryTile miniQuarry) {
                    miniQuarry.setEnchantments(EnchantmentLevel.fromMap(enchantments));
                } else if (entity instanceof TileAdvQuarry advQuarry) {
                    advQuarry.setEnchantments(EnchantmentLevel.fromMap(enchantments));
                } else if (entity instanceof TileAdvPump advPump) {
                    TileAdvPumpSetter.setEnchantment(advPump, enchantments);
                } else {
                    fail("Tile at %s is not Quarry but %s.".formatted(pos, entity));
                }
            })
            .thenExecuteAfter(1, () -> {
                var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
                    helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
                assertEquals(1, drops.size(), "Drop was " + drops);
                var stack = drops.get(0);
                var tag = stack.getTag();
                assertAll(
                    () -> assertEquals(block.blockItem, stack.getItem()),
                    () -> assertEquals(1, stack.getCount()),
                    () -> {
                        if (enchantments.isEmpty()) assertNull(tag, "Stack: %s, %s".formatted(stack, stack.getTag()));
                        else assertNotNull(tag, "Stack: %s, %s".formatted(stack, enchantments));
                    },
                    () -> assertEquals(enchantments, EnchantmentHelper.getEnchantments(stack))
                );
            })
            .thenSucceed();
    }

    @GameTestGenerator
    public List<TestFunction> dropOfMiningWell() {
        return IntStream.range(0, 6).mapToObj(i -> GameTestUtil.create(QuarryPlus.modID, "defaultBatch", "mining_well_drop_" + i,
            g -> dropOfMiningWell(g, i))).toList();
    }

    private void dropOfMiningWell(GameTestHelper helper, int efficiency) {
        var pos = GameTestUtil.getBasePos(helper).above();
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, Holder.BLOCK_MINING_WELL))
            .thenExecuteAfter(1, () -> ((MiningWellTile) Objects.requireNonNull(helper.getBlockEntity(pos))).setEfficiencyLevel(efficiency))
            .thenExecuteAfter(1, () -> {
                var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), helper.getBlockEntity(pos));
                assertEquals(1, drops.size(), "Drop was " + drops);
                var stack = drops.get(0);
                assertEquals(efficiency, stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY));
            })
            .thenSucceed();
    }
}
