package com.yogpc.qp.forge.mixin;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {
    @Redirect(method = "loadElementFromResource", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"))
    private static <T> Holder.Reference<T> changeLifeCycle(WritableRegistry<T> instance, ResourceKey<T> key, T t, RegistrationInfo registrationInfo) {
        RegistrationInfo i;
        if (QuarryPlus.modID.equals(key.location().getNamespace())) {
            // Make QuarryPlus things stable
            i = RegistrationInfo.BUILT_IN;
        } else {
            i = registrationInfo;
        }
        return instance.register(key, t, i);
    }
}
