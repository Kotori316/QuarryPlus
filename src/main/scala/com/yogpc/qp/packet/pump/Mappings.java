package com.yogpc.qp.packet.pump;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import com.yogpc.qp.machines.pump.TankPump;
import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class Mappings {

    /**
     * To client only.
     */
    public static class All implements IMessage<All> {
        BlockPos pos;
        int dim;
        Direction facing;
        EnumMap<Direction, List<FluidStack>> map = new EnumMap<>(Direction.class);

        public static All create(TilePump pump, Direction facing) {
            All message = new All();
            message.pos = pump.getPos();
            message.dim = IMessage.getDimId(pump.getWorld());
            message.facing = facing;
            message.map = new EnumMap<>(((TankPump) pump.getStorage()).mapping);
            return message;
        }

        @Override
        public All readFromBuffer(PacketBuffer buffer) {
            pos = buffer.readBlockPos();
            dim = buffer.readInt();
            facing = buffer.readEnumValue(Direction.class);
            for (Direction direction : Direction.values()) {
                int l = buffer.readInt();
                List<FluidStack> stacks = new ArrayList<>(l);
                for (int j = 0; j < l; j++) {
                    stacks.add(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                }
                map.put(direction, stacks);
            }

            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeInt(dim);
            buffer.writeEnumValue(facing);
            for (List<FluidStack> stacks : map.values()) {
                buffer.writeInt(stacks.size());
                stacks.forEach(s -> buffer.writeCompoundTag(s.writeToNBT(new CompoundNBT())));
            }
        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            IMessage.findTile(ctx, pos, dim, TilePump.class)
                .ifPresent(pump -> {
                    ((TankPump) pump.getStorage()).mapping.clear();
                    ((TankPump) pump.getStorage()).mapping.putAll(map);
                });
        }

    }

    /**
     * To server only.
     */
    public static class Update implements IMessage<Update> {

        Type type;
        Direction facing;
        BlockPos pos;
        int dim;
        ResourceLocation fluidName;

        public static Update create(TilePump pump, Direction facing, Type type, ResourceLocation fluidName) {
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
            facing = buffer.readEnumValue(Direction.class);
            type = buffer.readEnumValue(Type.class);
            fluidName = buffer.readResourceLocation();
            dim = buffer.readInt();
            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(facing).writeEnumValue(type).writeResourceLocation(fluidName).writeInt(dim);

        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            IMessage.findTile(ctx, pos, dim, TilePump.class)
                .ifPresent(pump -> ctx.get().enqueueWork(() -> {
                    List<FluidStack> list = ((TankPump) pump.getStorage()).mapping.get(facing);
                    typeAction(list, fluidName, type);
                }));
        }

        public static void typeAction(List<FluidStack> list, ResourceLocation fluidName, Type type) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
            if (fluid != null) {
                FluidStack e = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                int i = list.indexOf(e);
                switch (type) {
                    case Add:
                        list.add(e);
                        break;
                    case Remove:
                        list.remove(e);
                        break;
                    case Up:
                        if (i > 0) {
                            list.remove(i);
                            list.add(i - 1, e);
                        }
                        break;
                    case Top:
                        if (i > 0) {
                            list.remove(i);
                            list.add(0, e);
                        }
                        break;
                    case Down:
                        if (i >= 0 && i != list.size() - 1) {
                            list.remove(i);
                            list.add(i + 1, e);
                        }
                        break;
                    case Bottom:
                        if (i >= 0 && i != list.size() - 1) {
                            list.remove(i);
                            list.add(e);
                        }
                        break;
                    case None:
                        break;
                }
            }

        }
    }

    /**
     * To server only.
     */
    public static class Copy implements IMessage<Copy> {

        BlockPos pos;
        int dim;
        Direction dest;
        List<FluidStack> list;

        public static Copy create(TilePump pump, Direction dest, List<FluidStack> list) {
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
            dest = buffer.readEnumValue(Direction.class);
            dim = buffer.readInt();
            int length = buffer.readInt();
            list = new LinkedList<>();
            for (int i = 0; i < length; i++) {
                list.add(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
            }
            return this;
        }

        @Override
        public void writeToBuffer(PacketBuffer buffer) {
            buffer.writeBlockPos(pos).writeEnumValue(dest).writeInt(dim).writeInt(list.size());
            list.stream().map(s -> s.writeToNBT(new CompoundNBT())).forEach(buffer::writeCompoundTag);
        }

        @Override
        public void onReceive(Supplier<NetworkEvent.Context> ctx) {
            IMessage.findTile(ctx, pos, dim, TilePump.class)
                .ifPresent(pump -> ctx.get().enqueueWork(() -> ((TankPump) pump.getStorage()).mapping.put(dest, list)));
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
