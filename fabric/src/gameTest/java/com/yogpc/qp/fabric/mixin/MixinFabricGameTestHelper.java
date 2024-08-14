package com.yogpc.qp.fabric.mixin;

import com.yogpc.qp.fabric.LoadTest;
import net.fabricmc.fabric.impl.gametest.FabricGameTestHelper;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FabricGameTestHelper.class)
public abstract class MixinFabricGameTestHelper {
    @Inject(method = "runHeadlessServer", at = @At("HEAD"))
    private static void runHeadlessServer(LevelStorageSource.LevelStorageAccess session, PackRepository resourcePackManager, CallbackInfo ci) {
        LoadTest.register();
    }
}
