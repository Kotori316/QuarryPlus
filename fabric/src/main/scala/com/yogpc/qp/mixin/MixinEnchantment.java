package com.yogpc.qp.mixin;

import com.yogpc.qp.machines.EnchantableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public final class MixinEnchantment {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    public void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof EnchantableItem) {
            cir.setReturnValue(stack.getCount() == 1);
        }
    }
}
