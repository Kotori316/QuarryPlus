package com.yogpc.qp.machine.mover;

import com.google.common.base.CaseFormat;
import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class MoverEntityTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static Stream<TestFunction> tests(String batchName, String structureName) {
        return Stream.of(
            canPlaceEnchanted(batchName, structureName),
            canNotPlaceEnchanted(batchName, structureName),
            GameTestFunctions.getTestFunctionStream(batchName, structureName, List.of(MoverEntityTest.class), 100)
        ).flatMap(Function.identity());
    }

    static void instance(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        assertNotNull(helper.getBlockEntity(base, MoverEntity.class));
        helper.succeed();
    }

    static void initial(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        var mover = helper.getBlockEntity(base, MoverEntity.class);
        assertTrue(mover.inventory.isEmpty());
        assertTrue(mover.movableEnchantments.isEmpty());
        helper.succeed();
    }

    private static Stream<TestFunction> canPlaceEnchanted(String batchName, String structureName) {
        record ItemCase(Item item, String name) {
        }
        return Stream.of(
            new ItemCase(Items.DIAMOND_PICKAXE, "diamond_pickaxe"),
            new ItemCase(Items.NETHERITE_PICKAXE, "netherite_pickaxe"),
            new ItemCase(Items.BOW, "bow"),
            new ItemCase(Items.DIAMOND_AXE, "diamond_axe"),
            new ItemCase(Items.NETHERITE_AXE, "netherite_axe"),
            new ItemCase(Items.DIAMOND_HOE, "diamond_hoe"),
            new ItemCase(Items.DIAMOND_SWORD, "diamond_sword"),
            new ItemCase(Items.DIAMOND_SHOVEL, "diamond_shovel")
        ).map(c -> TestFunction.createWithStructure(QuarryPlus.modID, batchName,
            CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "canPlaceEnchanted_%s".formatted(c.name())),
            structureName,
            GameTestFunctions.wrapper(g -> {
                g.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
                var mover = g.getBlockEntity(base, MoverEntity.class);
                var enchantment = GameTestFunctions.getEnchantment(g, Enchantments.UNBREAKING);
                var stack = new ItemStack(c.item());
                stack.enchant(enchantment, 1);
                assertTrue(mover.inventory.canPlaceItem(0, stack));
                g.succeed();
            })
        ));
    }

    private static Stream<TestFunction> canNotPlaceEnchanted(String batchName, String structureName) {
        record ItemCase(Item item, String name) {
        }
        return Stream.of(
            new ItemCase(Items.WOODEN_PICKAXE, "wooden_pickaxe"),
            new ItemCase(Items.STONE_PICKAXE, "stone_pickaxe"),
            new ItemCase(Items.IRON_PICKAXE, "iron_pickaxe"),
            new ItemCase(Items.GOLDEN_PICKAXE, "golden_pickaxe"),
            new ItemCase(Items.DIAMOND, "diamond")
        ).map(c -> TestFunction.createWithStructure(QuarryPlus.modID, batchName,
            CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "canNotPlaceEnchanted_%s".formatted(c.name())),
            structureName,
            GameTestFunctions.wrapper(g -> {
                g.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
                var mover = g.getBlockEntity(base, MoverEntity.class);
                var enchantment = GameTestFunctions.getEnchantment(g, Enchantments.UNBREAKING);
                var stack = new ItemStack(c.item());
                stack.enchant(enchantment, 1);
                assertFalse(mover.inventory.canPlaceItem(0, stack));
                g.succeed();
            })
        ));
    }
}
