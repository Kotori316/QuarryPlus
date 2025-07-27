package com.yogpc.qp.machines.module;

import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@GameTestHolder(QuarryPlus.modID)
@PrefixGameTestTemplate(value = false)
class FilterModuleItemTest {

    static final String BATCH = "FilterModuleItem";

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void emptyTagRow() {
        var stack = new ItemStack(Holder.ITEM_FILTER_MODULE);
        assertEquals(2, FilterModuleItem.getRowsFromStack(stack));
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void twoTagRow() {
        var stack = new ItemStack(Holder.ITEM_FILTER_MODULE);
        stack.getOrCreateTag().putInt("filter_items_rows", 4);
        assertEquals(4, FilterModuleItem.getRowsFromStack(stack));
    }
}
