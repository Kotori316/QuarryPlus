package com.yogpc.qp;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.yogpc.qp.machines.workbench.EnableCondition;
import com.yogpc.qp.machines.workbench.EnchantmentIngredient;
import com.yogpc.qp.machines.workbench.QuarryDebugCondition;
import cpw.mods.modlauncher.Launcher;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.FalseCondition;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.OrCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.targets.FMLDataUserdevLaunchHandler;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.GameData;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.fail;

public final class QuarryPlusTest implements BeforeAllCallback {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext context) {
        setup();
    }

    static synchronized void setup() {
        if (!INITIALIZED.getAndSet(true)) {
            resolveInfoCmpError();
            SharedConstants.tryDetectVersion();
            //initLoader();
            changeDist();
            setHandler();
            Bootstrap.bootStrap();
            unfreezeGameData();
            ModLoadingContext.get().setActiveContainer(new DummyModContainer());
            registerRecipes();
            setConfig();
        }
    }

    private static void resolveInfoCmpError() {
        var name = Terminal.TYPE_DUMB_COLOR;
        InfoCmp.setDefaultInfoCmp(name, () ->
            Try.call(() -> InfoCmp.class.getDeclaredMethod("loadDefaultInfoCmp", String.class))
                // Setting name is dumb-color, but file name is dumb-colors
                .andThenTry(m -> ReflectionUtils.invokeMethod(m, null, name + "s"))
                .andThenTry(String.class::cast)
                .getOrThrow(RuntimeException::new)
        );
    }

    private static void changeDist() {
        try {
            Field dist = FMLLoader.class.getDeclaredField("dist");
            dist.setAccessible(true);
            dist.set(null, Dist.CLIENT);
        } catch (Exception e) {
            fail(e);
        }
    }

    private static void setHandler() {
        try {
            Field handler = FMLLoader.class.getDeclaredField("commonLaunchHandler");
            handler.setAccessible(true);
            handler.set(null, new FMLDataUserdevLaunchHandler());
        } catch (Exception e) {
            fail(e);
        }
    }

    private static void initLoader() {
        try {
            // Currently, this cause class cast error of ModuleClassLoader.
            Constructor<Launcher> constructor = Launcher.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * Copied from {@link GameData#unfreezeData()} to avoid caller check which is to be installed in {@link GameData} class.
     */
    @SuppressWarnings("deprecation")
    private static void unfreezeGameData() {
        Registry.REGISTRY.stream().filter(r -> r instanceof MappedRegistry).forEach(r -> ((MappedRegistry<?>) r).unfreeze());
    }

    private static void registerRecipes() {
        CraftingHelper.register(AndCondition.Serializer.INSTANCE);
        CraftingHelper.register(FalseCondition.Serializer.INSTANCE);
        CraftingHelper.register(ItemExistsCondition.Serializer.INSTANCE);
        CraftingHelper.register(ModLoadedCondition.Serializer.INSTANCE);
        CraftingHelper.register(NotCondition.Serializer.INSTANCE);
        CraftingHelper.register(OrCondition.Serializer.INSTANCE);
        CraftingHelper.register(TrueCondition.Serializer.INSTANCE);
        CraftingHelper.register(TagEmptyCondition.Serializer.INSTANCE);
        CraftingHelper.register(new EnableCondition.Serializer());
        CraftingHelper.register(new QuarryDebugCondition.Serializer());

        CraftingHelper.register(new ResourceLocation("forge", "compound"), CompoundIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("forge", "nbt"), NBTIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("minecraft", "item"), VanillaIngredientSerializer.INSTANCE);
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

    private static class DummyModContainer extends ModContainer {

        public DummyModContainer() {
            super(new DummyModInfo());
            contextExtension = Object::new;
        }

        @Override
        public boolean matches(Object mod) {
            return mod == getMod();
        }

        @Override
        public Object getMod() {
            return "Quarry Test";
        }
    }

    private static class DummyModInfo implements IModInfo, IConfigurable {

        @Override
        public IModFileInfo getOwningFile() {
            return null;
        }

        @Override
        public String getModId() {
            return QuarryPlus.modID;
        }

        @Override
        public String getDisplayName() {
            return "QuarryPlus Test";
        }

        @Override
        public String getDescription() {
            return getDisplayName();
        }

        @Override
        public ArtifactVersion getVersion() {
            return new DefaultArtifactVersion("1.0");
        }

        @Override
        public List<? extends ModVersion> getDependencies() {
            return List.of();
        }

        @Override
        public String getNamespace() {
            return getModId();
        }

        @Override
        public Map<String, Object> getModProperties() {
            return Map.of();
        }

        @Override
        public Optional<URL> getUpdateURL() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getLogoFile() {
            return Optional.empty();
        }

        @Override
        public boolean getLogoBlur() {
            return false;
        }

        @Override
        public IConfigurable getConfig() {
            return this;
        }

        @Override
        public <T> Optional<T> getConfigElement(String... key) {
            return Optional.empty();
        }

        @Override
        public List<? extends IConfigurable> getConfigList(String... key) {
            return List.of();
        }
    }
}
