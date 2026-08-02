package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaceAdvPumpTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().advPumpBlock().get());
        assertInstanceOf(AdvPumpBlock.class, helper.getBlockState(base).getBlock());
        assertInstanceOf(AdvPumpEntity.class, helper.getBlockEntity(base));
        helper.succeed();
    }

    public static void placeNoEnchantment(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().advPumpBlock().get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, base.below(), Direction.UP);
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().advPumpBlock().get(), base);

        AdvPumpEntity pump = helper.getBlockEntity(base);
        assertTrue(pump.getEnchantments().isEmpty());
        assertEquals(PowerEntity.ONE_FE * PlatformAccess.config().powerMap().advPump().maxEnergy(), pump.getMaxEnergy());
        helper.succeed();
    }

    public static void placeEfficiency(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().advPumpBlock().get());
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, base.below(), Direction.UP);

        AdvPumpEntity pump = helper.getBlockEntity(base);
        assertFalse(pump.getEnchantments().isEmpty());
        assertEquals(4 * PowerEntity.ONE_FE * PlatformAccess.config().powerMap().advPump().maxEnergy(), pump.getMaxEnergy(),
            "maxEnergy scales as base * 2^efficiency (efficiency=2 -> x4), unlike AdvQuarry's linear (1+efficiency)");
        helper.succeed();
    }

    public static void placeFortune(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().advPumpBlock().get());
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.FORTUNE), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, base.below(), Direction.UP);

        AdvPumpEntity pump = helper.getBlockEntity(base);
        assertEquals((int) (PlatformAccess.config().powerMap().advPump().range() * 3), pump.range(helper.getLevel()),
            "rangeLevel = max(fortune, 0) = 2 -> range = base * 3");
        helper.succeed();
    }

    public static void placeSilkTouch(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().advPumpBlock().get());
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.SILK_TOUCH), 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, base.below(), Direction.UP);

        AdvPumpEntity pump = helper.getBlockEntity(base);
        assertEquals((int) (PlatformAccess.config().powerMap().advPump().range() * 4), pump.range(helper.getLevel()),
            "Silk Touch always maps to rangeLevel 3, regardless of level, and doesn't stack with Fortune");
        helper.succeed();
    }

    public static void drainWaterSource(GameTestHelper helper) {
        var waterRelative = base.below();
        helper.setBlock(waterRelative, Blocks.WATER);
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().advPumpBlock().get());

        var absolutePos = helper.absolutePos(base);
        var absoluteWaterPos = helper.absolutePos(waterRelative);
        AdvPumpEntity pump = helper.getBlockEntity(base);
        pump.addEnergy(PowerEntity.ONE_FE * 1_000_000L, false);

        pump.drainOnce(helper.getLevel(), absolutePos, helper.getLevel().getBlockState(absolutePos));

        assertTrue(helper.getLevel().getFluidState(absoluteWaterPos).isEmpty());
        assertEquals(MachineStorage.ONE_BUCKET, pump.storage.getFluidCount(Fluids.WATER),
            "One source block drained -> exactly one bucket's worth stored, not more, not less");
        assertTrue(helper.getLevel().getBlockState(absoluteWaterPos).is(PlatformAccess.getAccess().registerObjects().softBlock().get()),
            "The drained source is replaced with the SoftBlock placeholder, not left as air");
        helper.succeed();
    }

    public static void drainWaterSourceWithDeleteFluid(GameTestHelper helper) {
        var waterRelative = base.below();
        helper.setBlock(waterRelative, Blocks.WATER);
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().advPumpBlock().get());

        var absolutePos = helper.absolutePos(base);
        var absoluteWaterPos = helper.absolutePos(waterRelative);
        AdvPumpEntity pump = helper.getBlockEntity(base);
        pump.addEnergy(PowerEntity.ONE_FE * 1_000_000L, false);
        pump.deleteFluid = true;

        pump.drainOnce(helper.getLevel(), absolutePos, helper.getLevel().getBlockState(absolutePos));

        assertTrue(helper.getLevel().getFluidState(absoluteWaterPos).isEmpty());
        assertEquals(0, pump.storage.getFluidCount(Fluids.WATER),
            "With deleteFluid on, the water disappears from the world but is discarded rather than stored");
        helper.succeed();
    }

    public static void drainLavaSource(GameTestHelper helper) {
        var lavaRelative = base.below();
        helper.setBlock(lavaRelative, Blocks.LAVA);
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().advPumpBlock().get());

        var absolutePos = helper.absolutePos(base);
        var absoluteLavaPos = helper.absolutePos(lavaRelative);
        AdvPumpEntity pump = helper.getBlockEntity(base);
        pump.addEnergy(PowerEntity.ONE_FE * 1_000_000L, false);

        pump.drainOnce(helper.getLevel(), absolutePos, helper.getLevel().getBlockState(absolutePos));

        assertTrue(helper.getLevel().getFluidState(absoluteLavaPos).isEmpty());
        assertEquals(MachineStorage.ONE_BUCKET, pump.storage.getFluidCount(Fluids.LAVA));
        assertTrue(helper.getLevel().getBlockState(absoluteLavaPos).isAir(),
            "Unlike water, a drained lava source is left as plain air, not the SoftBlock placeholder");
        assertFalse(helper.getLevel().getBlockState(absoluteLavaPos).is(PlatformAccess.getAccess().registerObjects().softBlock().get()));
        helper.succeed();
    }
}
