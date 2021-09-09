package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ExpModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "exp_module";
    public static final String KEY_AMOUNT = "amount";

    public ExpModuleItem() {
        super(new Properties().tab(Holder.TAB).stacksTo(1));
        setRegistryName(QuarryPlus.modID, NAME);
    }

    @Override
    public QuarryModule getModule(@Nonnull ItemStack stack) {
        return new ExpItemModule(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        Stream.ofNullable(stack.getTag())
            .mapToInt(t -> t.getInt(KEY_AMOUNT))
            .filter(i -> i >= 0)
            .mapToObj(i -> "Exp: " + i)
            .map(TextComponent::new)
            .forEach(list::add);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getTag() != null) {
            int exp = stack.getTag().getInt(KEY_AMOUNT);
            if (!level.isClientSide) {
                player.giveExperiencePoints(exp);
            }
            stack.removeTagKey(KEY_AMOUNT);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    static class ExpItemModule extends ExpModule {
        private final ItemStack stack;

        ExpItemModule(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void addExp(int amount) {
            if (amount == 0) return;
            var tag = stack.getOrCreateTag();
            var pre = tag.getInt(KEY_AMOUNT);
            tag.putInt(KEY_AMOUNT, pre + amount);
        }

        @Override
        public int getExp() {
            if (!stack.hasTag()) {
                return 0;
            } else {
                assert stack.getTag() != null; // Stack does have tag!
                return stack.getTag().getInt(KEY_AMOUNT);
            }
        }
    }
}
