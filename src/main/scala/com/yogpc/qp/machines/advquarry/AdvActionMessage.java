package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.packet.IMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * To Server only
 */
public final class AdvActionMessage implements IMessage<AdvActionMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "adv_action_message");
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final Area area;
    private final Actions action;


    AdvActionMessage(TileAdvQuarry quarry, Actions action, Area area) {
        this.pos = quarry.getBlockPos();
        this.dim = quarry.getLevel() != null ? quarry.getLevel().dimension() : Level.OVERWORLD;
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
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos).writeResourceLocation(this.dim.location());
        buf.writeNbt(this.area.toNBT());
        buf.writeEnum(this.action);
    }

    @Override
    public AdvActionMessage readFromBuffer(FriendlyByteBuf buffer) {
        return new AdvActionMessage(buffer);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    enum Actions {
        QUICK_START, MODULE_INV, CHANGE_RANGE
    }

    public static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new AdvActionMessage(buf);
        server.execute(() -> {
            var world = server.getLevel(message.dim);
            if (world != null) {
                if (world.getBlockEntity(message.pos) instanceof TileAdvQuarry quarry) {
                    switch (message.action) {
                        case CHANGE_RANGE -> quarry.area = message.area;
                        case MODULE_INV -> QuarryPlus.LOGGER.warn("({}) Asked to open module inventory but not implemented.", AdvActionMessage.class);
                        case QUICK_START -> quarry.setAction(new AdvQuarryAction.BreakBlock(quarry));
                    }
                }
            }
        });
    };
}
