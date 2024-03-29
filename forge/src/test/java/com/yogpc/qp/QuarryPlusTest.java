package com.yogpc.qp;

import com.kotori316.testutil.MCTestInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class QuarryPlusTest implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        MCTestInitializer.setUp(QuarryPlus.modID, QuarryPlusTest::setup, QuarryPlus.Register::registerAll);
    }

    static void setup() {
        registerRecipes();
        setConfig();
    }

    private static void registerRecipes() {
        // CraftingHelper.register(new ResourceLocation(QuarryPlus.modID, EnchantmentIngredient.NAME), EnchantmentIngredient.Serializer.INSTANCE);
    }

    private static void setConfig() {
        QuarryPlus.registerConfig(true);
    }

    public static ResourceLocation id(String location) {
        return new ResourceLocation(QuarryPlus.modID, location);
    }

    public static FluidState getFluidState(BlockState blockState) {
        blockState.initCache();
        return blockState.getFluidState();
    }
}
