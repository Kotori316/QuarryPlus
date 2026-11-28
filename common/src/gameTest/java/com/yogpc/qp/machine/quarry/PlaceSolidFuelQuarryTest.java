package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaceSolidFuelQuarryTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        assertInstanceOf(SolidFuelQuarryBlock.class, helper.getBlockState(base).getBlock());
        assertInstanceOf(SolidFuelQuarryEntity.class, helper.getBlockEntity(base, BlockEntity.class));
        helper.succeed();
    }

    public static void checkDrop(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base, BlockEntity.class));
        assertFalse(drop.isEmpty());
        assertInstanceOf(QuarryItem.class, drop.getFirst().getItem());
        assertFalse(drop.getFirst().isEnchanted());
        helper.succeed();
    }

    public static void enchantmentIsIgnored(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, base.below(), Direction.UP);
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get(), base);

        SolidFuelQuarryEntity quarry = helper.getBlockEntity(base, SolidFuelQuarryEntity.class);
        assertTrue(quarry.getEnchantments().isEmpty());
        assertEquals(PlatformAccess.config().powerMap().solidFuelQuarry().maxEnergy() * PowerEntity.ONE_FE, quarry.getMaxEnergy());

        quarry.setEnchantments(new ItemEnchantments.Mutable(ItemEnchantments.EMPTY).toImmutable());
        helper.succeed();
    }

    public static void doesNotAcceptEnergy(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        SolidFuelQuarryEntity quarry = helper.getBlockEntity(base, SolidFuelQuarryEntity.class);
        assertEquals(0, quarry.addEnergy(PowerEntity.ONE_FE * 100, false));
        assertEquals(0, quarry.getEnergy());
        helper.succeed();
    }

    public static void generateEnergyFromFuel(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        SolidFuelQuarryEntity quarry = helper.getBlockEntity(base, SolidFuelQuarryEntity.class);
        quarry.fuelContainer.setItem(0, new ItemStack(Items.COAL, 2));
        assertEquals(0, quarry.getFuelCount());

        SolidFuelQuarryEntity.tickFuel(helper.getLevel(), quarry.getBlockPos(), quarry.getBlockState(), quarry);
        assertEquals(1, quarry.fuelContainer.getItem(0).getCount());
        var generationSpeed = PlatformAccess.config().powerMap().solidFuelQuarry().generationSpeed();
        assertEquals((long) (generationSpeed * PowerEntity.ONE_FE), quarry.getEnergy());
        // The total energy of a fuel is independent of the speed. Coal burns 1600 ticks. (1 tick is already used.)
        assertEquals(1600 * SolidFuelQuarryEntity.TOTAL_FE_PER_BURN_TICK, (quarry.getFuelCount() + 1) * generationSpeed, 1e-6);
        helper.succeed();
    }

    public static void onlyFuelCanBePlaced(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        SolidFuelQuarryEntity quarry = helper.getBlockEntity(base, SolidFuelQuarryEntity.class);
        assertTrue(quarry.fuelContainer.canPlaceItem(0, new ItemStack(Items.COAL)));
        assertFalse(quarry.fuelContainer.canPlaceItem(0, new ItemStack(Items.DIAMOND)));
        helper.succeed();
    }

    public static void dropFuelWhenRemoved(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        SolidFuelQuarryEntity quarry = helper.getBlockEntity(base, SolidFuelQuarryEntity.class);
        quarry.fuelContainer.setItem(0, new ItemStack(Items.COAL, 5));

        helper.setBlock(base, Blocks.AIR);

        var dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(base)).inflate(2));
        var coalCount = dropped.stream().map(ItemEntity::getItem).filter(i -> i.is(Items.COAL)).mapToInt(ItemStack::getCount).sum();
        assertEquals(5, coalCount);
        helper.succeed();
    }

    public static void fuelWithRemainingItemSingle(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        SolidFuelQuarryEntity quarry = helper.getBlockEntity(base, SolidFuelQuarryEntity.class);
        quarry.fuelContainer.setItem(0, new ItemStack(Items.LAVA_BUCKET));

        SolidFuelQuarryEntity.tickFuel(helper.getLevel(), quarry.getBlockPos(), quarry.getBlockState(), quarry);
        assertTrue(quarry.getFuelCount() > 0);
        // The remaining item is put into the fuel slot, not into the storage.
        assertTrue(quarry.fuelContainer.getItem(0).is(Items.BUCKET), "slot: " + quarry.fuelContainer.getItem(0));
        assertEquals(1, quarry.fuelContainer.getItem(0).getCount());
        assertEquals(0, quarry.storage.getItemCount(Items.BUCKET, DataComponentPatch.EMPTY), "Storage: " + quarry.storage);
        helper.succeed();
    }

    public static void fuelWithRemainingItemStacked(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().solidFuelQuarryBlock().get());
        SolidFuelQuarryEntity quarry = helper.getBlockEntity(base, SolidFuelQuarryEntity.class);
        // Container.setItem limits the stack size to the max size of the item (1 for Lava bucket), so change the count after setting.
        var stack = new ItemStack(Items.LAVA_BUCKET);
        quarry.fuelContainer.setItem(0, stack);
        stack.setCount(4);

        SolidFuelQuarryEntity.tickFuel(helper.getLevel(), quarry.getBlockPos(), quarry.getBlockState(), quarry);
        assertTrue(quarry.getFuelCount() > 0, "fuelCount: " + quarry.getFuelCount());
        assertTrue(quarry.fuelContainer.getItem(0).is(Items.LAVA_BUCKET), "slot: " + quarry.fuelContainer.getItem(0));
        assertEquals(3, quarry.fuelContainer.getItem(0).getCount());
        // The slot is still used by the fuel, so the remaining item is in the storage (last resort), not dropped.
        assertEquals(1, quarry.storage.getItemCount(Items.BUCKET, DataComponentPatch.EMPTY), "Storage: " + quarry.storage);
        var around = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(base)).inflate(2));
        assertTrue(around.isEmpty(), "Dropped items: " + around);
        helper.succeed();
    }
}
