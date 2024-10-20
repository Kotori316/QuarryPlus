package com.yogpc.qp;

import com.electronwill.nightconfig.core.Config;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.machine.GeneralScreenHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class PlatformAccessDelegate implements PlatformAccess {
    private PlatformAccess access;
    @Nullable
    private Path configPath;
    @Nullable
    private QuarryConfig config;

    public PlatformAccessDelegate() {
        this.access = new VanillaImpl();

        if (Files.exists(configPath())) {
            config = QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment);
        }
    }

    public void reset() {
        this.access = new VanillaImpl();
        this.configPath = null;
        setConfig(null);
    }

    public void setAccess(PlatformAccess access) {
        this.access = access;
    }

    public void setConfigPath(Path configPath) {
        this.configPath = configPath;
        this.config = QuarryConfig.load(configPath, this::isInDevelopmentEnvironment);
    }

    public void setConfig(@Nullable QuarryConfig config) {
        this.config = config;
    }

    public void setConfig(Config config, boolean debug) {
        setConfig(QuarryConfig.fromConfig(config, debug));
    }

    @Override
    public int priority() {
        return -1;
    }

    @Override
    public String platformName() {
        return "test";
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
        if (configPath == null) {
            return access.configPath();
        }
        return configPath;
    }

    @Override
    public Supplier<? extends QuarryConfig> getConfig() {
        if (config == null) {
            if (configPath == null) {
                config = QuarryConfig.defaultConfig(this.isInDevelopmentEnvironment());
            } else {
                config = QuarryConfig.load(configPath, this::isInDevelopmentEnvironment);
            }
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
        public String platformName() {
            return "vanilla";
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
            return () -> QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment);
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
