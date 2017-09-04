package com.yogpc.qp.packet.pump;

import java.io.IOException;
import java.util.LinkedList;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TilePump;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Mappings {

    /**
     * To both client and server only.
     */
    public static class All implements IMessage {
        BlockPos pos;
        EnumFacing facing;
        LinkedList<String> list;

        public static All create(TilePump pump, EnumFacing facing) {
            return create(pump, facing, pump.mapping.get(facing));
        }

        public static All create(TilePump pump, EnumFacing facing, LinkedList<String> list) {
            All mappings = new All();
            mappings.pos = pump.getPos();
            mappings.facing = facing;
            mappings.list = list;
            return mappings;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) throws IOException {
            pos = buffer.readBlockPos();
            facing = buffer.readEnumValue(EnumFacing.class);
            int l = buffer.readInt();
            LinkedList<String> strings = new LinkedList<>();
            for (int i = 0; i < l; i++) {
                strings.add(buffer.readString(Short.MAX_VALUE));
            }
            list = strings;
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos);
            buffer.writeEnumValue(facing);
            buffer.writeInt(list.size());
            list.forEach(buffer::writeString);
        }

        @Override
        @SuppressWarnings("MethodCallSideOnly")
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            switch (ctx.side) {
                case CLIENT:
                    TilePump pumpC = (TilePump) Minecraft.getMinecraft().world.getTileEntity(pos);
                    assert pumpC != null;
                    pumpC.mapping.put(facing, list);
                    break;
                case SERVER:
                    TilePump pumpS = (TilePump) ctx.getServerHandler().playerEntity.world.getTileEntity(pos);
                    assert pumpS != null;
                    pumpS.mapping.put(facing, list);
                    break;
            }

            return null;
        }
    }

    /**
     * To server only.
     */
    public static class Update implements IMessage {

        Type type;
        EnumFacing facing;
        BlockPos pos;
        String fluidName;

        public static Update create(TilePump pump, EnumFacing facing, Type type, String fluidName) {
            Update update = new Update();
            update.facing = facing;
            update.pos = pump.getPos();
            update.type = type;
            update.fluidName = fluidName;
            return update;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) throws IOException {
            pos = buffer.readBlockPos();
            facing = buffer.readEnumValue(EnumFacing.class);
            type = buffer.readEnumValue(Type.class);
            fluidName = buffer.readString(Short.MAX_VALUE);
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(facing).writeEnumValue(type).writeString(fluidName);
        }

        @Override
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            TilePump pump = (TilePump) ctx.getServerHandler().playerEntity.world.getTileEntity(pos);
            assert pump != null;
            LinkedList<String> list = pump.mapping.get(facing);
            int i = list.indexOf(fluidName);
            switch (type) {
                case Add:
                    list.add(fluidName);
                    break;
                case Remove:
                    list.remove(fluidName);
                    break;
                case Up:
                    if (i > -1) {
                        list.remove(i);
                        list.add(i - 1, fluidName);
                    }
                    break;
                case Top:
                    if (i > -1) {
                        list.remove(i);
                        list.addFirst(fluidName);
                    }
                    break;
                case Down:
                    if (i > -1) {
                        list.remove(i);
                        list.add(i + 1, fluidName);
                    }
                    break;
                case Bottom:
                    if (i > -1) {
                        list.remove(i);
                        list.addLast(fluidName);
                    }
                    break;
                case None:
                    break;
            }
            return null;
        }
    }

    public enum Type {
        None(0), Remove(1), Add(2), Up(3), Top(4), Down(5), Bottom(6);
        public final int id;

        Type(int i) {
            id = i;
        }

        public int getId() {
            return id;
        }

        public static Type fromID(int id) {
            switch (id) {
                case 1:
                    return Remove;
                case 2:
                    return Add;
                case 3:
                    return Up;
                case 4:
                    return Top;
                case 5:
                    return Down;
                case 6:
                    return Bottom;
                default:
                    return None;
            }
        }
    }
}
