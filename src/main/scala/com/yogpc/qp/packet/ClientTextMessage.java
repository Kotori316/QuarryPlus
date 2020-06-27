package com.yogpc.qp.packet;

import java.util.Objects;
import java.util.function.Supplier;

import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.TextInClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientTextMessage implements IMessage<ClientTextMessage> {

    public ClientTextMessage(TextInClient text, BlockPos pos, ResourceLocation dim) {
        this.text = text;
        this.pos = pos;
        this.dim = dim;
    }

    public ClientTextMessage(TextInClient text, BlockPos pos, World world) {
        this(text, pos, IMessage.getDimId(world));
    }

    public ClientTextMessage() {
        this.text = null;
        this.pos = BlockPos.ZERO;
        this.dim = World.field_234918_g_.func_240901_a_();
    }

    private final TextInClient text;
    private final BlockPos pos;
    private final ResourceLocation dim;

    @Override
    public ClientTextMessage readFromBuffer(PacketBuffer buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation dim = buffer.readResourceLocation();
        TextInClient textInClient = TextInClient.read(buffer);
        return new ClientTextMessage(textInClient, pos, dim);
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim);
        Objects.requireNonNull(text).write(buffer);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, APowerTile.class).ifPresent(t ->
            t.clientText = text);
    }
}
