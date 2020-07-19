package com.yogpc.qp.packet.filler;

import java.util.function.Supplier;

import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.filler.FillerTile;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class FillerModuleMessage implements IMessage<FillerModuleMessage> {
    private BlockPos pos;
    private ResourceLocation dim;

    public static FillerModuleMessage create(FillerTile tile) {
        FillerModuleMessage message = new FillerModuleMessage();
        message.dim = IMessage.getDimId(tile.getWorld());
        message.pos = tile.getPos();
        return message;
    }

    @Override
    public FillerModuleMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readResourceLocation();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, FillerTile.class).ifPresent(t ->
            ContainerQuarryModule.InteractionObject.openGUI(t, ctx.get().getSender(), TranslationKeys.filler)
        );
    }
}
