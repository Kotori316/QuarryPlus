package com.yogpc.qp.gametest;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.exp.ExpModuleItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ExpModuleItemTest {
    static ExpModuleItem item() {
        return ((ExpModuleItem) BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, ExpModuleItem.NAME)));
    }

    public static void initStack(GameTestHelper helper) {
        var stack = new ItemStack(item());
        var module = item().getModule(stack);
        assertEquals(0, module.getExp());
        assertTrue(stack.has(QuarryDataComponents.HOLDING_EXP_COMPONENT));
        helper.succeed();
    }

    public static void updateExp(GameTestHelper helper) {
        var stack = new ItemStack(item());
        var module = item().getModule(stack);
        assertEquals(0, module.getExp());

        module.addExp(200);
        assertEquals(200, module.getExp());
        module.addExp(300);
        assertEquals(500, module.getExp());
        assertEquals(500, stack.get(QuarryDataComponents.HOLDING_EXP_COMPONENT));

        helper.succeed();
    }

    public static void setExp(GameTestHelper helper) {
        var stack = new ItemStack(item());
        stack.set(QuarryDataComponents.HOLDING_EXP_COMPONENT, 344);
        var module = item().getModule(stack);
        assertEquals(344, module.getExp());
        helper.succeed();
    }

    public static void use(GameTestHelper helper) {
        var stack = new ItemStack(item());
        stack.set(QuarryDataComponents.HOLDING_EXP_COMPONENT, 344);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        assertEquals(344, player.totalExperience);

        var module = item().getModule(stack);
        assertEquals(0, module.getExp());
        helper.succeed();
    }
}
