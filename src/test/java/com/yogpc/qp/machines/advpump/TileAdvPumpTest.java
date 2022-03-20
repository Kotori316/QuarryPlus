package com.yogpc.qp.machines.advpump;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.EnchantmentLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class TileAdvPumpTest {
    @Test
    void dummy() {
        assertTrue(enchantedCapacity().findAny().isPresent());
    }

    @Test
    void initialCapacity() {
        var tile = new TileAdvPump(BlockPos.ZERO, Holder.BLOCK_ADV_PUMP.defaultBlockState());
        var e = new EnchantmentEfficiency(List.of());
        assertEquals(e.energyCapacity, tile.getMaxEnergy());
    }

    @ParameterizedTest
    @MethodSource
    void enchantedCapacity(EnchantmentEfficiency enchantments) {
        var tile = new TileAdvPump(BlockPos.ZERO, Holder.BLOCK_ADV_PUMP.defaultBlockState());
        tile.setEnchantment(enchantments);
        assertEquals(enchantments.energyCapacity, tile.getMaxEnergy());
    }

    static Stream<EnchantmentEfficiency> enchantedCapacity() {
        return IntStream.rangeClosed(0, 10)
            .mapToObj(l -> new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, l))
            .map(List::of)
            .map(EnchantmentEfficiency::new);
    }
}