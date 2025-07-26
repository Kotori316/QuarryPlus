package com.yogpc.qp;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.targets.FMLDataUserdevLaunchHandler;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.ForgeFeature;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.server.LanguageHook;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jline.utils.InfoCmp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.support.ReflectionSupport;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("removal")
public final class MCTestInitializer implements BeforeAllCallback {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public MCTestInitializer() {
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        setUp(Objects.requireNonNull(System.getenv("target_mod"), "Please set 'target_mod' in environment value."), () -> {
        });
    }

    public static synchronized void setUp(String modId, Runnable additional) {
        try (MockedStatic<NetworkHooks> mocked = Mockito.mockStatic(NetworkHooks.class)) {
            mocked.when(NetworkHooks::init).then(a -> null);
            if (!INITIALIZED.getAndSet(true)) {
                resolveInfoCmpError();
                SharedConstants.tryDetectVersion();
                changeDist();
                setHandler();
                Bootstrap.bootStrap();
                unfreezeGameData();
                ModLoadingContext.get().setActiveContainer(new MCTestInitializer.DummyModContainer(modId));
                registerRecipes();
                mockCapability();
                mockRegistries();
                setFluidType();
                setLanguage(modId);
                additional.run();
            }
        }
    }

    private static void resolveInfoCmpError() {
        InfoCmp.setDefaultInfoCmp("dumb-color", () -> Try.call(() -> InfoCmp.class.getDeclaredMethod("loadDefaultInfoCmp", String.class)).andThenTry((m) -> ReflectionSupport.invokeMethod(m, null, "dumb-colors")).andThenTry(String.class::cast).getOrThrow(RuntimeException::new));
    }

    private static void changeDist() {
        try {
            Field dist = FMLLoader.class.getDeclaredField("dist");
            dist.setAccessible(true);
            dist.set(null, Dist.CLIENT);
        } catch (Exception e) {
            Assertions.fail(e);
        }

    }

    private static void setHandler() {
        try {
            Field handler = FMLLoader.class.getDeclaredField("commonLaunchHandler");
            handler.setAccessible(true);
            handler.set(null, new FMLDataUserdevLaunchHandler());
        } catch (Exception e) {
            Assertions.fail(e);
        }

    }

    private static void unfreezeGameData() {
        BuiltInRegistries.REGISTRY.stream().filter((r) -> r instanceof MappedRegistry).forEach((r) -> ((MappedRegistry) r).unfreeze());
    }

    private static void mockCapability() {
        try {
            Method method = CapabilityManager.class.getDeclaredMethod("get", String.class, Boolean.TYPE);
            method.setAccessible(true);
            Capability<IEnergyStorage> cap_IEnergyStorage = (Capability) method.invoke(CapabilityManager.INSTANCE, "IEnergyStorage", false);
            Capability<IFluidHandler> cap_IFluidHandler = (Capability) method.invoke(CapabilityManager.INSTANCE, "IFluidHandler", false);
            Capability<IFluidHandlerItem> cap_IFluidHandlerItem = (Capability) method.invoke(CapabilityManager.INSTANCE, "IFluidHandlerItem", false);
            Capability<IItemHandler> cap_IItemHandler = (Capability) method.invoke(CapabilityManager.INSTANCE, "IItemHandler", false);

            try (MockedStatic<CapabilityManager> mocked = Mockito.mockStatic(CapabilityManager.class)) {
                mocked.when(() -> CapabilityManager.get(ArgumentMatchers.any())).thenReturn(cap_IEnergyStorage).thenReturn(cap_IFluidHandler).thenReturn(cap_IFluidHandlerItem).thenReturn(cap_IItemHandler);
                Assertions.assertEquals(cap_IEnergyStorage, ForgeCapabilities.ENERGY);
                Assertions.assertEquals(cap_IFluidHandler, ForgeCapabilities.FLUID_HANDLER);
                Assertions.assertEquals(cap_IFluidHandlerItem, ForgeCapabilities.FLUID_HANDLER_ITEM);
                Assertions.assertEquals(cap_IItemHandler, ForgeCapabilities.ITEM_HANDLER);
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }

        Assertions.assertNotNull(ForgeCapabilities.ENERGY);
        Assertions.assertNotNull(ForgeCapabilities.FLUID_HANDLER);
        Assertions.assertNotNull(ForgeCapabilities.FLUID_HANDLER_ITEM);
        Assertions.assertNotNull(ForgeCapabilities.ITEM_HANDLER);
    }

    private static void mockRegistries() {
        try {
            mockRegistry(ForgeRegistries.ITEMS, ForgeRegistries.class.getDeclaredField("ITEMS"));
            mockRegistry(ForgeRegistries.BLOCKS, ForgeRegistries.class.getDeclaredField("BLOCKS"));
            mockRegistry(ForgeRegistries.FLUIDS, ForgeRegistries.class.getDeclaredField("FLUIDS"));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static <T> void mockRegistry(IForgeRegistry<T> registry, Field field) throws ReflectiveOperationException {
        Method wrapperGetter = ForgeRegistry.class.getDeclaredMethod("getWrapper");
        wrapperGetter.setAccessible(true);
        Registry<T> wrapper = (Registry) wrapperGetter.invoke(registry);
        IForgeRegistry<T> s = Mockito.spy(registry);
        Mockito.when(s.getDelegate(ArgumentMatchers.<T>any())).thenAnswer((invocation) -> {
            T arg = invocation.getArgument(0);
            return Optional.of(Holder.Reference.createIntrusive(wrapper.asLookup(), arg));
        });
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), s);
    }

    private static void registerRecipes() {
        CraftingHelper.register(AndCondition.Serializer.INSTANCE);
        CraftingHelper.register(net.minecraftforge.common.crafting.conditions.FalseCondition.Serializer.INSTANCE);
        CraftingHelper.register(net.minecraftforge.common.crafting.conditions.ItemExistsCondition.Serializer.INSTANCE);
        CraftingHelper.register(net.minecraftforge.common.crafting.conditions.ModLoadedCondition.Serializer.INSTANCE);
        CraftingHelper.register(net.minecraftforge.common.crafting.conditions.NotCondition.Serializer.INSTANCE);
        CraftingHelper.register(net.minecraftforge.common.crafting.conditions.OrCondition.Serializer.INSTANCE);
        CraftingHelper.register(net.minecraftforge.common.crafting.conditions.TrueCondition.Serializer.INSTANCE);
        CraftingHelper.register(net.minecraftforge.common.crafting.conditions.TagEmptyCondition.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("forge", "compound"), net.minecraftforge.common.crafting.CompoundIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("forge", "nbt"), net.minecraftforge.common.crafting.StrictNBTIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("minecraft", "item"), VanillaIngredientSerializer.INSTANCE);
    }

    private static void setFluidType() {
        FluidType airType = new FluidType(FluidType.Properties.create().descriptionId("block.minecraft.air").motionScale(1.0F).canPushEntity(false).canSwim(false).canDrown(false).fallDistanceModifier(1.0F).pathType(null).adjacentPathType(null).density(0).temperature(0).viscosity(0));
        FluidType waterType = new FluidType(FluidType.Properties.create().descriptionId("block.minecraft.water").fallDistanceModifier(0.0F).canExtinguish(true).canConvertToSource(true).supportsBoating(true).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH).canHydrate(true));
        FluidType lavaType = new FluidType(FluidType.Properties.create().descriptionId("block.minecraft.lava").canSwim(false).canDrown(false).pathType(BlockPathTypes.LAVA).adjacentPathType(null).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA).lightLevel(15).density(3000).viscosity(6000).temperature(1300));

        try {
            Field field = Fluid.class.getDeclaredField("forgeFluidType");
            UnsafeHacks.setField(field, Fluids.EMPTY, airType);
            UnsafeHacks.setField(field, Fluids.WATER, waterType);
            UnsafeHacks.setField(field, Fluids.LAVA, lavaType);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void setLanguage(String modId) {
        LanguageHook.loadForgeAndMCLangs();
        Try.call(() -> LanguageHook.class.getDeclaredMethod("loadLocaleData", InputStream.class)).andThenTry((m) -> ReflectionSupport.invokeMethod(m, null, MCTestInitializer.class.getResourceAsStream("/assets/%s/lang/en_us.json".formatted(modId))));
    }

    private static class DummyModContainer extends ModContainer {
        private final String name;

        public DummyModContainer(String name) {
            super(new DummyModInfo(name));
            this.name = name;
            this.contextExtension = Object::new;
        }

        @Override
        public boolean matches(Object mod) {
            return mod == this.getMod();
        }

        @Override
        public Object getMod() {
            return this.name + " Test";
        }
    }

    private record DummyModInfo(String name) implements IModInfo, IConfigurable {
        @Override
        public IModFileInfo getOwningFile() {
            return new DummyModFileInfo();
        }

        @Override
        public String getModId() {
            return this.name;
        }

        @Override
        public String getDisplayName() {
            return this.getModId() + " Test";
        }

        @Override
        public String getDescription() {
            return this.getDisplayName();
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
        public List<? extends ForgeFeature.Bound> getForgeFeatures() {
            return List.of();
        }

        @Override
        public String getNamespace() {
            return this.getModId();
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
        public Optional<URL> getModURL() {
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

        @Override
        public String name() {
            return this.name;
        }
    }

    private static class DummyModFileInfo implements IModFileInfo {

        @Override
        public List<IModInfo> getMods() {
            return List.of();
        }

        @Override
        public List<LanguageSpec> requiredLanguageLoaders() {
            return List.of();
        }

        @Override
        public boolean showAsResourcePack() {
            return false;
        }

        @Override
        public Map<String, Object> getFileProperties() {
            return Map.of();
        }

        @Override
        public String getLicense() {
            return "";
        }

        @Override
        public String moduleName() {
            return "";
        }

        @Override
        public String versionString() {
            return "";
        }

        @Override
        public List<String> usesServices() {
            return List.of();
        }

        @Override
        public IModFile getFile() {
            return null;
        }

        @Override
        public IConfigurable getConfig() {
            return null;
        }
    }
}
