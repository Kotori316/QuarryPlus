package com.yogpc.qp;

import java.util.concurrent.atomic.AtomicBoolean;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.yogpc.qp.machines.workbench.EnchantmentIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.kotori316.testutil.MCTestInitializer;

public final class QuarryPlusTest implements BeforeAllCallback {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext context) {
        MCTestInitializer.setUp(QuarryPlus.modID);
        setup();
    }

    static synchronized void setup() {
        if (!INITIALIZED.getAndSet(true)) {
            registerRecipes();
            setConfig();
        }
    }

    private static void registerRecipes() {
        CraftingHelper.register(new ResourceLocation(QuarryPlus.modID, EnchantmentIngredient.NAME), EnchantmentIngredient.Serializer.INSTANCE);
    }

    private static void setConfig() {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        QuarryPlus.config = new Config(common);
        var config = common.build();
        final CommentedConfig commentedConfig = CommentedConfig.inMemory();
        config.correct(commentedConfig);
        config.acceptConfig(commentedConfig);
    }

    public static ResourceLocation id(String location) {
        return new ResourceLocation(QuarryPlus.modID, location);
    }

}
