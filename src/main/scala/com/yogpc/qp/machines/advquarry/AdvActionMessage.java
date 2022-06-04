package com.yogpc.qp.machines.advquarry;

import java.util.function.Supplier;

import com.yogpc.qp.integration.ftbchunks.FTBChunksProtectionCheck;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

/**
 * To Server only
 */
public final class AdvActionMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final Area area;
    private final Actions action;


    AdvActionMessage(TileAdvQuarry quarry, Actions action, Area area) {
        this.pos = quarry.getBlockPos();
        this.dim = PacketHandler.getDimension(quarry);
        this.area = area;
        this.action = action;
    }

    AdvActionMessage(TileAdvQuarry quarry, Actions action) {
        this(quarry, action, quarry.getArea());
    }

    public AdvActionMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        this.area = Area.fromNBT(buf.readNbt()).orElse(null);
        this.action = buf.readEnum(Actions.class);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos).writeResourceLocation(this.dim.location());
        buf.writeNbt(this.area.toNBT());
        buf.writeEnum(this.action);
    }

    enum Actions {
        QUICK_START, MODULE_INV, CHANGE_RANGE
    }

    public static void onReceive(AdvActionMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            world.map(w -> w.getBlockEntity(message.pos))
                .flatMap(MapMulti.optCast(TileAdvQuarry.class))
                .ifPresent(quarry -> {
                    switch (message.action) {
                        case CHANGE_RANGE -> {
                            quarry.area = message.area;
                            if (FTBChunksProtectionCheck.isAreaProtected(message.area.shrink(1, 0, 1), quarry.getTargetWorld().dimension())) {
                                PacketHandler.getPlayer(supplier.get())
                                    .ifPresent(p -> p.displayClientMessage(new TranslatableComponent("quarryplus.chat.warn_protected_area"), false));
                            }
                        }
                        case MODULE_INV -> PacketHandler.getPlayer(supplier.get())
                            .flatMap(MapMulti.optCast(ServerPlayer.class))
                            .ifPresent(quarry::openModuleGui);
                        case QUICK_START -> quarry.setAction(new AdvQuarryAction.BreakBlock(quarry));
                    }
                }));
    }
}
