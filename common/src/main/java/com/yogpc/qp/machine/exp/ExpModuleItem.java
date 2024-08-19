package com.yogpc.qp.machine.exp;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.machine.QpItem;
import com.yogpc.qp.machine.module.QuarryModule;
import com.yogpc.qp.machine.module.QuarryModuleProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ExpModuleItem extends QpItem implements QuarryModuleProvider.Item {
    public static final String NAME = "exp_module";

    public ExpModuleItem() {
        super(new Properties().stacksTo(1).component(QuarryDataComponents.HOLDING_EXP_COMPONENT, 0), NAME);
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        return new ExpItemModule(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            int exp = stack.getOrDefault(QuarryDataComponents.HOLDING_EXP_COMPONENT, 0);
            if (exp > 0) {
                player.displayClientMessage(Component.literal("Give %d exp point".formatted(exp)), true);
                player.giveExperiencePoints(exp);
                stack.set(QuarryDataComponents.HOLDING_EXP_COMPONENT, 0);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        int exp = stack.getOrDefault(QuarryDataComponents.HOLDING_EXP_COMPONENT, 0);
        if (exp > 0) {
            tooltipComponents.add(
                Component.empty()
                    .append(Component.literal("Exp: "))
                    .append(Component.literal(String.valueOf(exp)).withStyle(ChatFormatting.AQUA))
            );
        }
    }

    private record ExpItemModule(ItemStack stack) implements ExpModule {

        @Override
        public void addExp(int amount) {
            if (amount == 0) return;

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
