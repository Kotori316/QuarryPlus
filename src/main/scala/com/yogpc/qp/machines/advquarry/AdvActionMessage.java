package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * To Server only
 */
public final class AdvActionMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final Area area;
    private final Actions action;
    private final WorkConfig workConfig;

    AdvActionMessage(TileAdvQuarry quarry, Actions action, Area area, WorkConfig workConfig) {
        this.pos = quarry.getBlockPos();
        this.dim = PacketHandler.getDimension(quarry);
        this.area = area;
        this.action = action;
        this.workConfig = workConfig;
        AdvQuarry.LOGGER.debug(AdvQuarry.MESSAGE, "Message is created. {} {} {} {}", this.pos, this.dim.location(), this.area, this.action);
    }

    AdvActionMessage(TileAdvQuarry quarry, Actions action, Area area) {
        this(quarry, action, area, quarry.workConfig);
    }

    AdvActionMessage(TileAdvQuarry quarry, Actions action) {
        this(quarry, action, quarry.getArea(), quarry.workConfig);
    }

    AdvActionMessage(TileAdvQuarry quarry, Actions action, WorkConfig workConfig) {
        this(quarry, action, quarry.getArea(), workConfig);
    }

    public AdvActionMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        this.area = Area.fromNBT(buf.readNbt()).orElse(null);
        this.action = buf.readEnum(Actions.class);
        this.workConfig = new WorkConfig(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos).writeResourceLocation(this.dim.location());
        buf.writeNbt(this.area.toNBT());
        buf.writeEnum(this.action);
        this.workConfig.writePacket(buf);
    }

    enum Actions {
        QUICK_START, MODULE_INV, CHANGE_RANGE, SYNC
    }

    public static void onReceive(AdvActionMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
                world.map(w -> w.getBlockEntity(message.pos))
                        .flatMap(MapMulti.optCast(TileAdvQuarry.class))
                        .ifPresent(quarry -> {
                            AdvQuarry.LOGGER.debug(AdvQuarry.MESSAGE, "onReceive. {}, {}", message.pos, message.action);
                            switch (message.action) {
                                case CHANGE_RANGE -> quarry.setArea(message.area);
                                case MODULE_INV -> PacketHandler.getPlayer(supplier.get())
                                        .flatMap(MapMulti.optCast(ServerPlayer.class))
                                        .ifPresent(quarry::openModuleGui);
                                case QUICK_START -> {
                                    quarry.workConfig = quarry.workConfig.startSoonConfig();
                                    if (quarry.canStartWork()) {
                                        AdvQuarryAction.startQuarry(quarry);
                                    }
                                }
                                case SYNC -> quarry.workConfig = message.workConfig;
                            }
                        }));
    }
}
