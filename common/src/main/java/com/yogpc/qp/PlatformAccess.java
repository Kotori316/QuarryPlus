package com.yogpc.qp;

import com.yogpc.qp.config.EnableMap;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.MachineLootFunction;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.marker.ChunkMarkerBlock;
import com.yogpc.qp.machine.marker.FlexibleMarkerBlock;
import com.yogpc.qp.machine.marker.MarkerContainer;
import com.yogpc.qp.machine.marker.NormalMarkerBlock;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import com.yogpc.qp.machine.misc.YSetterContainer;
import com.yogpc.qp.machine.module.BedrockModuleItem;
import com.yogpc.qp.machine.module.ModuleContainer;
import com.yogpc.qp.machine.mover.MoverBlock;
import com.yogpc.qp.machine.mover.MoverContainer;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.storage.DebugStorageBlock;
import com.yogpc.qp.machine.storage.DebugStorageContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface PlatformAccess {
    static PlatformAccess getAccess() {
        return PlatformAccessHolder.instance;
    }

    static QuarryConfig config() {
        return getAccess().getConfig().get();
    }

    default int priority() {
        return 0;
    }

    String platformName();

    RegisterObjects registerObjects();

    interface RegisterObjects {
        Supplier<? extends QuarryBlock> quarryBlock();

        Supplier<? extends FrameBlock> frameBlock();

        Supplier<? extends GeneratorBlock> generatorBlock();

        Supplier<? extends NormalMarkerBlock> markerBlock();

        Supplier<? extends MoverBlock> moverBlock();

        Supplier<? extends FlexibleMarkerBlock> flexibleMarkerBlock();

        Supplier<? extends ChunkMarkerBlock> chunkMarkerBlock();

        Supplier<? extends DebugStorageBlock> debugStorageBlock();

        Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block);

        Map<String, EnableMap.EnableOrNot> defaultEnableSetting();

        Supplier<? extends BedrockModuleItem> bedrockModuleItem();

        Stream<Supplier<? extends InCreativeTabs>> allItems();

        Supplier<MenuType<? extends YSetterContainer>> ySetterContainer();

        Supplier<MenuType<? extends MoverContainer>> moverContainer();

        Supplier<MenuType<? extends ModuleContainer>> moduleContainer();

        Supplier<MenuType<? extends MarkerContainer>> flexibleMarkerContainer();

        Supplier<MenuType<? extends MarkerContainer>> chunkMarkerContainer();

        Supplier<MenuType<? extends DebugStorageContainer>> debugStorageContainer();

        Supplier<LootItemFunctionType<? extends MachineLootFunction>> machineLootFunction();
    }

    Packet packetHandler();

    interface Packet {
        void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level);

        void sendToClientPlayer(@NotNull CustomPacketPayload message, @NotNull ServerPlayer player);

        void sendToServer(@NotNull CustomPacketPayload message);
    }

    Path configPath();

    Supplier<? extends QuarryConfig> getConfig();

    boolean isInDevelopmentEnvironment();

    interface Transfer {
        /**
         * @return items that is not moved. In other words, the rest of item.
         */
        ItemStack transferItem(Level level, BlockPos pos, ItemStack stack, Direction side, boolean simulate);

        FluidStackLike transferFluid(Level level, BlockPos pos, FluidStackLike stack, Direction side, boolean simulate);
    }

    Transfer transfer();

    FluidStackLike getFluidInItem(ItemStack stack);

    Component getFluidName(FluidStackLike stack);

    <T extends AbstractContainerMenu> void openGui(ServerPlayer player, GeneralScreenHandler<T> handler);
}

class PlatformAccessHolder {
    static final PlatformAccess instance;

    static {
        QuarryPlus.LOGGER.info("[PlatformAccess] loading");
        instance = ServiceLoader.load(PlatformAccess.class, PlatformAccess.class.getClassLoader()).stream()
            .map(ServiceLoader.Provider::get)
            .min(Comparator.comparingInt(PlatformAccess::priority))
            .orElseThrow(() -> new IllegalStateException("PlatformAccess not found. It's a bug."));
        QuarryPlus.LOGGER.info("[PlatformAccess] loaded for {}, {}", instance.platformName(), instance.getClass().getSimpleName());
    }
}
