package com.yogpc.qp.packet.enchantment;

import java.io.IOException;

import com.yogpc.qp.container.ContainerMover;
import com.yogpc.qp.container.ContainerMover.D;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MoverMessage {
    /**
     * To server only.
     * For container player opening.
     */
    public static class Move implements IMessage {

        BlockPos pos;
        int id;

        public static Move create(BlockPos pos, int id) {
            Move move = new Move();
            move.pos = pos;
            move.id = id;
            return move;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) throws IOException {
            pos = buffer.readBlockPos();
            id = buffer.readInt();
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeInt(id);
        }

        @Override
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            Container container = ctx.getServerHandler().playerEntity.openContainer;
            if (container.windowId == id) {
                ((ContainerMover) container).moveEnchant();
            }
            return null;
        }
    }

    /**
     * To server only.
     * For container player opening.
     */
    public static class Cursor implements IMessage {
        D d;
        int id;
        BlockPos pos;

        public static Cursor create(BlockPos pos, int id, D d) {
            Cursor cursor = new Cursor();
            cursor.d = d;
            cursor.id = id;
            cursor.pos = pos;
            return cursor;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) throws IOException {
            d = buffer.readEnumValue(D.class);
            pos = buffer.readBlockPos();
            id = buffer.readInt();
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeEnumValue(d).writeBlockPos(pos).writeInt(id);
        }

        @Override
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            Container container = ctx.getServerHandler().playerEntity.openContainer;
            if (container.windowId == id) {
                ((ContainerMover) container).setAvail(d);
            }
            return null;
        }
    }

}
