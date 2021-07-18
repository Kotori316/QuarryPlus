package com.yogpc.qp.mixin;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.enchantment.EnchantmentTarget$12")
// EnchantmentTarget.DIGGER
public class MixinEnchantmentTargetDigger {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    public void addQuarry(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item == QuarryPlus.ModObjects.BLOCK_QUARRY.blockItem) {
            cir.setReturnValue(true);
        }
    }
}
