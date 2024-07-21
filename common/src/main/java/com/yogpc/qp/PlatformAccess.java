package com.yogpc.qp;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface PlatformAccess {
    static PlatformAccess getAccess() {
        return PlatformAccessHolder.instance;
    }

    static QuarryConfig getConfig() {
        return getAccess().quarryConfig();
    }

    String platformName();

    RegisterObjects registerObjects();

    interface RegisterObjects {
        Supplier<? extends QuarryBlock> quarryBlock();

        Supplier<? extends FrameBlock> frameBlock();

        Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block);

        Stream<Supplier<? extends InCreativeTabs>> allItems();
    }

    Packet packetHandler();

    interface Packet {
        void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level);
    }

    QuarryConfig quarryConfig();
}

class PlatformAccessHolder {
    static final PlatformAccess instance;

    static {
        QuarryPlus.LOGGER.info("[PlatformAccess] loading");
        instance = ServiceLoader.load(PlatformAccess.class).findFirst().orElseThrow();
        QuarryPlus.LOGGER.info("[PlatformAccess] loaded for {}, {}", instance.platformName(), instance.getClass().getSimpleName());
    }
}