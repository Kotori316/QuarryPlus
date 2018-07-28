package com.yogpc.qp.packet.pump;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TilePump;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Mappings {

    /**
     * To client only.
     */
    public static class All implements IMessage {
        BlockPos pos;
        EnumFacing facing;
        EnumMap<EnumFacing, LinkedList<String>> map = new EnumMap<>(EnumFacing.class);

        public static All create(TilePump pump, EnumFacing facing) {
            All message = new All();
            message.pos = pump.getPos();
            message.facing = facing;
            message.map = new EnumMap<>(pump.mapping);
            return message;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            facing = buffer.readEnumValue(EnumFacing.class);
            for (EnumFacing VALUE : EnumFacing.VALUES) {
                int l = buffer.readInt();
                LinkedList<String> strings = new LinkedList<>();
                for (int j = 0; j < l; j++) {
                    strings.add(buffer.readString(Short.MAX_VALUE));
                }
                map.put(VALUE, strings);
            }
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(facing);
            for (LinkedList<String> strings : map.values()) {
                buffer.writeInt(strings.size());
                strings.forEach(buffer::writeString);
            }
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            TilePump pumpC = (TilePump) QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getTileEntity(pos);
            if (pumpC != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    pumpC.mapping.clear();
                    pumpC.mapping.putAll(map);
                });
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
        int dim;
        String fluidName;

        public static Update create(TilePump pump, EnumFacing facing, Type type, String fluidName) {
            Update update = new Update();
            update.facing = facing;
            update.pos = pump.getPos();
            update.type = type;
            update.fluidName = fluidName;
            update.dim = pump.getWorld().provider.getDimension();
            return update;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            facing = buffer.readEnumValue(EnumFacing.class);
            type = buffer.readEnumValue(Type.class);
            fluidName = buffer.readString(Short.MAX_VALUE);
            dim = buffer.readInt();
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(facing).writeEnumValue(type).writeString(fluidName).writeInt(dim);
        }

        @Override
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
            MinecraftServer server = world.getMinecraftServer();
            if (world.provider.getDimension() == dim && server != null) {
                TilePump pump = (TilePump) world.getTileEntity(pos);
                if (pump != null) {
                    server.addScheduledTask(() -> {
                        LinkedList<String> list = pump.mapping.get(facing);
                        typeAction(list, fluidName, type);
                    });
                }
            }
            return null;
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
    public static class Copy implements IMessage {

        BlockPos pos;
        int dim;
        EnumFacing dest;
        LinkedList<String> list;

        public static Copy create(TilePump pump, EnumFacing dest, LinkedList<String> list) {
            Copy copy = new Copy();
            copy.dest = dest;
            copy.pos = pump.getPos();
            copy.list = list;
            copy.dim = pump.getWorld().provider.getDimension();
            return copy;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            dest = buffer.readEnumValue(EnumFacing.class);
            dim = buffer.readInt();
            int length = buffer.readInt();
            list = new LinkedList<>();
            for (int i = 0; i < length; i++) {
                list.add(buffer.readString(Short.MAX_VALUE));
            }
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(dest).writeInt(dim).writeInt(list.size());
            list.forEach(buffer::writeString);
        }

        @Override
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
            MinecraftServer server = world.getMinecraftServer();
            if (world.provider.getDimension() == dim && server != null) {
                TilePump pump = (TilePump) world.getTileEntity(pos);
                if (pump != null)
                    server.addScheduledTask(() -> pump.mapping.put(dest, list));
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
