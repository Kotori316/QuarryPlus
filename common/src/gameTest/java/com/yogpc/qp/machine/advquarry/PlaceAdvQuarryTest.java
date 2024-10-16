package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaceAdvQuarryTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().advQuarryBlock().get());
        assertInstanceOf(AdvQuarryBlock.class, helper.getBlockState(base).getBlock());
        assertInstanceOf(AdvQuarryEntity.class, helper.getBlockEntity(base));
        helper.succeed();
    }

    public static void placeNoEnchantment(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().advQuarryBlock().get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, base.below(), Direction.UP);
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().advQuarryBlock().get(), base);

        AdvQuarryEntity advQuarryEntity = helper.getBlockEntity(base);
        assertTrue(advQuarryEntity.getEnchantments().isEmpty());
        assertEquals(PowerEntity.ONE_FE * PlatformAccess.config().powerMap().advQuarry().maxEnergy(), advQuarryEntity.getMaxEnergy());
        helper.succeed();
    }


    public static void placeEfficiency(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().advQuarryBlock().get());
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, base.below(), Direction.UP);

        AdvQuarryEntity advQuarryEntity = helper.getBlockEntity(base);
        assertFalse(advQuarryEntity.getEnchantments().isEmpty());
        assertEquals(3 * PowerEntity.ONE_FE * PlatformAccess.config().powerMap().advQuarry().maxEnergy(), advQuarryEntity.getMaxEnergy());
        helper.succeed();
    }
}
