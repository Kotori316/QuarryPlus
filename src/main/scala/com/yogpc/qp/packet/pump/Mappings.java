package com.yogpc.qp.packet.pump;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.function.Supplier;

import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class Mappings {

    /**
     * To client only.
     */
    public static class All implements IMessage<All> {
        BlockPos pos;
        int dim;
        EnumFacing facing;
        EnumMap<EnumFacing, LinkedList<String>> map = new EnumMap<>(EnumFacing.class);

        public static All create(TilePump pump, EnumFacing facing) {
            All message = new All();
            message.pos = pump.getPos();
            message.dim = IMessage.getDimId(pump.getWorld());
            message.facing = facing;
            message.map = new EnumMap<>(pump.mapping);
            return message;
        }

        @Override
        public All readFromBuffer(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            dim = buffer.readInt();
            facing = buffer.readEnumValue(EnumFacing.class);
            for (EnumFacing VALUE : EnumFacing.values()) {
                int l = buffer.readInt();
                LinkedList<String> strings = new LinkedList<>();
                for (int j = 0; j < l; j++) {
                    strings.add(buffer.readString(Short.MAX_VALUE));
                }
                map.put(VALUE, strings);
            }

            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeInt(dim);
            buffer.writeEnumValue(facing);
            for (LinkedList<String> strings : map.values()) {
                buffer.writeInt(strings.size());
                strings.forEach(buffer::writeString);
            }
        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            IMessage.findTile(ctx, pos, dim, TilePump.class)
                .ifPresent(pump -> {
                    pump.mapping.clear();
                    pump.mapping.putAll(map);
                });
        }

    }

    /**
     * To server only.
     */
    public static class Update implements IMessage<Update> {

        Type type;
        EnumFacing facing;
        BlockPos pos;
        int dim;
        String fluidName;

        public static Update create(TilePump pump, EnumFacing facing, Type type, String fluidName) {
            Update update = new Update();
            update.facing = facing;
            update.pos = pump.getPos();
            update.type = type;
            update.fluidName = fluidName;
            update.dim = IMessage.getDimId(pump.getWorld());
            return update;
        }

        @Override
        public Update readFromBuffer(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            facing = buffer.readEnumValue(EnumFacing.class);
            type = buffer.readEnumValue(Type.class);
            fluidName = buffer.readString(Short.MAX_VALUE);
            dim = buffer.readInt();
            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(facing).writeEnumValue(type).writeString(fluidName).writeInt(dim);

        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            IMessage.findTile(ctx, pos, dim, TilePump.class)
                .ifPresent(pump -> ctx.get().enqueueWork(() -> {
                    LinkedList<String> list = pump.mapping.get(facing);
                    typeAction(list, fluidName, type);
                }));
        }

        public static void typeAction(LinkedList<String> list, String fluidName, Type type) {
            int i = list.indexOf(fluidName);
            switch (type) {
                case Add:
                    list.add(fluidName);
                    break;
                case Remove:
                    list.remove(fluidName);
                    break;
                case Up:
                    if (i > 0) {
                        list.remove(i);
                        list.add(i - 1, fluidName);
                    }
                    break;
                case Top:
                    if (i > 0) {
                        list.remove(i);
                        list.addFirst(fluidName);
                    }
                    break;
                case Down:
                    if (i >= 0 && i != list.size() - 1) {
                        list.remove(i);
                        list.add(i + 1, fluidName);
                    }
                    break;
                case Bottom:
                    if (i >= 0 && i != list.size() - 1) {
                        list.remove(i);
                        list.addLast(fluidName);
                    }
                    break;
                case None:
                    break;
            }
        }
    }

    /**
     * To server only.
     */
    public static class Copy implements IMessage<Copy> {

        BlockPos pos;
        int dim;
        EnumFacing dest;
        LinkedList<String> list;

        public static Copy create(TilePump pump, EnumFacing dest, LinkedList<String> list) {
            Copy copy = new Copy();
            copy.dest = dest;
            copy.pos = pump.getPos();
            copy.list = list;
            copy.dim = IMessage.getDimId(pump.getWorld());
            return copy;
        }

        @Override
        public Copy readFromBuffer(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            dest = buffer.readEnumValue(EnumFacing.class);
            dim = buffer.readInt();
            int length = buffer.readInt();
            list = new LinkedList<>();
            for (int i = 0; i < length; i++) {
                list.add(buffer.readString(Short.MAX_VALUE));
            }
            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(dest).writeInt(dim).writeInt(list.size());
            list.forEach(buffer::writeString);
        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            IMessage.findTile(ctx, pos, dim, TilePump.class)
                .ifPresent(pump -> ctx.get().enqueueWork(() -> pump.mapping.put(dest, list)));
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
