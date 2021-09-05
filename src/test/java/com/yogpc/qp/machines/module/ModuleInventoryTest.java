package com.yogpc.qp.machines.module;

import java.util.Set;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleInventoryTest extends QuarryPlusTest {

    @Test
    void getModules1() {
        var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_PUMP_MODULE)));
        assertEquals(Set.of(QuarryModule.Constant.PUMP), Set.copyOf(modules));
    }

    @Test
    void getModules2() {
        var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_BEDROCK_MODULE)));
        assertEquals(Set.of(QuarryModule.Constant.BEDROCK), Set.copyOf(modules));
    }

    @Test
    void getModules3() {
        var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_BEDROCK_MODULE), new ItemStack(Holder.ITEM_PUMP_MODULE)));
        assertEquals(Set.of(QuarryModule.Constant.BEDROCK, QuarryModule.Constant.PUMP), Set.copyOf(modules));
    }

    @Test
    void getModules4() {
        var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_PUMP_MODULE), new ItemStack(Holder.ITEM_PUMP_MODULE)));
        assertEquals(Set.of(QuarryModule.Constant.PUMP), Set.copyOf(modules));
    }

}
