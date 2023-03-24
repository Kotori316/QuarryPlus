package com.yogpc.qp;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class AccessItemTest extends QuarryPlusTest {
    @Test
    void testItems() {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(Items.DIAMOND);
        Assertions.assertEquals("minecraft:diamond", id.toString());
    }
}
