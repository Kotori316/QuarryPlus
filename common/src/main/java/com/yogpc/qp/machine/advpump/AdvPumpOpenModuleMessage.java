package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.module.ModuleContainer;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Client to server only. Sent when the "Modules" button in {@link AdvPumpScreen} is pressed.
 */
public final class AdvPumpOpenModuleMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final Identifier NAME = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "adv_pump_open_module_message");
    public static final CustomPacketPayload.Type<AdvPumpOpenModuleMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdvPumpOpenModuleMessage> STREAM_CODEC = CustomPacketPayload.codec(
        AdvPumpOpenModuleMessage::write, AdvPumpOpenModuleMessage::new
    );
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    AdvPumpOpenModuleMessage(BlockPos pos, ResourceKey<Level> dim) {
        this.pos = pos;
        this.dim = dim;
    }

    AdvPumpOpenModuleMessage(AdvPumpEntity entity) {
        this(entity.getBlockPos(), Objects.requireNonNull(entity.getLevel()).dimension());
    }

    AdvPumpOpenModuleMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readResourceKey(Registries.DIMENSION);
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceKey(dim);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof AdvPumpEntity pump && pump.enabled
            && player instanceof ServerPlayer serverPlayer
            && !PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            PlatformAccess.getAccess().openGui(serverPlayer, new GeneralScreenHandler<>(pump, ModuleContainer::new));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
