package com.yogpc.qp.packet.pump;

import java.io.IOException;
import java.util.LinkedList;

import com.yogpc.qp.gui.GuiP_List;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TilePump;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
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
        @SuppressWarnings("unchecked")
        LinkedList<String>[] lists = new LinkedList[6];

        @SuppressWarnings("unchecked")
        public static All create(TilePump pump, EnumFacing facing) {
            All mappings = new All();
            mappings.pos = pump.getPos();
            mappings.facing = facing;
            mappings.lists[0] = pump.mapping.get(EnumFacing.DOWN);
            mappings.lists[1] = pump.mapping.get(EnumFacing.UP);
            mappings.lists[2] = pump.mapping.get(EnumFacing.NORTH);
            mappings.lists[3] = pump.mapping.get(EnumFacing.SOUTH);
            mappings.lists[4] = pump.mapping.get(EnumFacing.WEST);
            mappings.lists[5] = pump.mapping.get(EnumFacing.EAST);

            return mappings;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) throws IOException {
            pos = buffer.readBlockPos();
            facing = buffer.readEnumValue(EnumFacing.class);
            for (int i = 0; i < lists.length; i++) {
                int l = buffer.readInt();
                LinkedList<String> strings = new LinkedList<>();
                for (int j = 0; j < l; j++) {
                    strings.add(buffer.readString(Short.MAX_VALUE));
                }
                lists[i] = strings;
            }
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(facing);
            for (LinkedList<String> strings : lists) {
                buffer.writeInt(strings.size());
                strings.forEach(buffer::writeString);
            }
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            TilePump pumpC = (TilePump) Minecraft.getMinecraft().world.getTileEntity(pos);
            assert pumpC != null;
            pumpC.mapping.clear();
            for (EnumFacing facing : EnumFacing.VALUES) {
                pumpC.mapping.put(facing, lists[facing.ordinal()]);
            }
            Minecraft.getMinecraft().displayGuiScreen(new GuiP_List((byte) facing.ordinal(), pumpC));
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
            World world = ctx.getServerHandler().playerEntity.world;
            if (world.provider.getDimension() == dim) {
                TilePump pump = (TilePump) world.getTileEntity(pos);
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
            }
            return null;
        }
    }

    public static class Copy implements IMessage {

        BlockPos pos;
        EnumFacing dest;
        LinkedList<String> list;

        public static Copy create(TilePump pump, EnumFacing dest, LinkedList<String> list) {
            Copy copy = new Copy();
            copy.dest = dest;
            copy.pos = pump.getPos();
            copy.list = list;
            return copy;
        }

        @Override
        public void fromBytes(PacketBuffer buffer) throws IOException {
            pos = buffer.readBlockPos();
            dest = buffer.readEnumValue(EnumFacing.class);
            int length = buffer.readInt();
            list = new LinkedList<>();
            for (int i = 0; i < length; i++) {
                list.add(buffer.readString(Short.MAX_VALUE));
            }
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(dest).writeInt(list.size());
            list.forEach(buffer::writeString);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onRecieve(IMessage message, MessageContext ctx) {
            TilePump pumpC = (TilePump) Minecraft.getMinecraft().world.getTileEntity(pos);
            assert pumpC != null;
            pumpC.mapping.put(dest, list);
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
