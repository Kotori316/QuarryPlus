package com.yogpc.qp.packet.mover;

import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.mover.ContainerMover;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class MoverMessage {
    /**
     * To server only.
     * For container player opening.
     */
    public static class Move implements IMessage<Move> {

        BlockPos pos;
        int id;

        public static Move create(BlockPos pos, int id) {
            Move move = new Move();
            move.pos = pos;
            move.id = id;
            return move;
        }

        @Override
        public Move readFromBuffer(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            id = buffer.readInt();
            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeInt(id);
        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            QuarryPlus.proxy.getPacketWorld(ctx.get()).map(World::getServer).ifPresent(s ->
                ctx.get().enqueueWork(() ->
                    QuarryPlus.proxy.getPacketPlayer(ctx.get())
                        .map(p -> p.openContainer)
                        .filter(c -> c.windowId == id)
                        .ifPresent(container -> ((ContainerMover) container).moveEnchant())
                ));
        }

    }

    /**
     * To server only.
     * For container player opening.
     */
    public static class Cursor implements IMessage<Cursor> {
        ContainerMover.D d;
        int id;
        BlockPos pos;

        public static Cursor create(BlockPos pos, int id, ContainerMover.D d) {
            Cursor cursor = new Cursor();
            cursor.d = d;
            cursor.id = id;
            cursor.pos = pos;
            return cursor;
        }

        @Override
        public Cursor readFromBuffer(PacketBuffer buffer) {
            d = buffer.readEnumValue(ContainerMover.D.class);
            pos = buffer.readBlockPos();
            id = buffer.readInt();
            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeEnumValue(d).writeBlockPos(pos).writeInt(id);

        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                QuarryPlus.proxy.getPacketPlayer(ctx.get())
                    .map(p -> p.openContainer)
                    .filter(c -> c.windowId == id)
                    .ifPresent(container -> ((ContainerMover) container).setAvail(d)))            ;
        }
    }

}
