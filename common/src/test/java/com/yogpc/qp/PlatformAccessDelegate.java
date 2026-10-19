package com.yogpc.qp;

import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.config.TestQuarryConfig;
import com.yogpc.qp.machine.GeneralScreenHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;

public class PlatformAccessDelegate implements PlatformAccess {
    private PlatformAccess access;
    @Nullable
    private QuarryConfig config;

    public PlatformAccessDelegate() {
        this.access = new VanillaImpl();
    }

    public void reset() {
        this.access = new VanillaImpl();
        setConfig(null);
    }

    public void setAccess(PlatformAccess access) {
        this.access = access;
    }

    public void setConfig(@Nullable QuarryConfig config) {
        this.config = config;
    }

    @Override
    public int priority() {
        return -1;
    }

    @Override
    public ModdingPlatform platformName() {
        return ModdingPlatform.TEST;
    }

    @Override
    public RegisterObjects registerObjects() {
        return access.registerObjects();
    }

    @Override
    public Packet packetHandler() {
        return access.packetHandler();
    }

    @Override
    public Path configPath() {
        return access.configPath();
    }

    @Override
    public Supplier<? extends QuarryConfig> getConfig() {
        if (config == null) {
            config = TestQuarryConfig.defaults().withDebug(this.isInDevelopmentEnvironment());
        }
        return () -> config;
    }

    @Override
    public boolean isInDevelopmentEnvironment() {
        return access.isInDevelopmentEnvironment();
    }

    @Override
    public Transfer transfer() {
        return access.transfer();
    }

    @Override
    public FluidStackLike getFluidInItem(ItemStack stack) {
        return access.getFluidInItem(stack);
    }

    @Override
    public Component getFluidName(FluidStackLike stack) {
        return access.getFluidName(stack);
    }

    @Override
    public <T extends AbstractContainerMenu> void openGui(ServerPlayer player, GeneralScreenHandler<T> handler) {
        access.openGui(player, handler);
    }

    @Override
    public Mining mining() {
        return access.mining();
    }

    public static PlatformAccess createVanilla() {
        return new VanillaImpl();
    }

    static final class VanillaImpl implements PlatformAccess {

        @Override
        public ModdingPlatform platformName() {
            return ModdingPlatform.VANILLA;
        }

        @Override
        public RegisterObjects registerObjects() {
            return null;
        }

        @Override
        public Packet packetHandler() {
            return null;
        }

        @Override
        public Path configPath() {
            return Path.of("logs", "config", "quarry_test.toml");
        }

        @Override
        public Supplier<? extends QuarryConfig> getConfig() {
            return TestQuarryConfig::defaults;
        }

        @Override
        public boolean isInDevelopmentEnvironment() {
            return false;
        }

        @Override
        public Transfer transfer() {
            return null;
        }

        @Override
        public FluidStackLike getFluidInItem(ItemStack stack) {
            return null;
        }

        @Override
        public Component getFluidName(FluidStackLike stack) {
            var name = BuiltInRegistries.FLUID.getKey(stack.fluid());
            return Component.literal(name.toString());
        }

        @Override
        public <T extends AbstractContainerMenu> void openGui(ServerPlayer player, GeneralScreenHandler<T> handler) {
        }

        @Override
        public Mining mining() {
            return null;
        }
    }
}
