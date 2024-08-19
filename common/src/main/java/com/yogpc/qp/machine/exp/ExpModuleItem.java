package com.yogpc.qp.machine.exp;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.machine.QpItem;
import com.yogpc.qp.machine.module.QuarryModule;
import com.yogpc.qp.machine.module.QuarryModuleProvider;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ExpModuleItem extends QpItem implements QuarryModuleProvider.Item {
    public static final String NAME = "exp_module";

    public ExpModuleItem() {
        super(new Properties().stacksTo(1).component(QuarryDataComponents.HOLDING_EXP_COMPONENT, 0), NAME);
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        return new ExpItemModule(stack);
    }

    private record ExpItemModule(ItemStack stack) implements ExpModule {

        @Override
        public void addExp(int amount) {
            stack.update(QuarryDataComponents.HOLDING_EXP_COMPONENT, 0, amount, Integer::sum);
        }

        @Override
        public int getExp() {
            return stack.getOrDefault(QuarryDataComponents.HOLDING_EXP_COMPONENT, 0);
        }

        @Override
        public String toString() {
            return "ExpItemModule{exp=" + getExp() + "}";
        }
    }
}
