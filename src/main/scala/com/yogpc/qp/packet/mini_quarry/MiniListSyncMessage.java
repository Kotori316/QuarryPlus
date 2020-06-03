package com.yogpc.qp.packet.mini_quarry;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.datafixers.Dynamic;
import com.yogpc.qp.machines.base.QuarryBlackList;
import com.yogpc.qp.machines.mini_quarry.MiniQuarryListGui;
import com.yogpc.qp.machines.mini_quarry.MiniQuarryTile;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.collection.immutable.Set;
import scala.jdk.javaapi.CollectionConverters;

/**
 * To both Client and Server
 */
public class MiniListSyncMessage implements IMessage<MiniListSyncMessage> {
    BlockPos pos;
    int dim;
    CompoundNBT listTag;

    public static MiniListSyncMessage create(BlockPos blockPos, int dim, Set<QuarryBlackList.Entry> blackList, Set<QuarryBlackList.Entry> whiteList) {
        return create(blockPos, dim, CollectionConverters.asJava(blackList), CollectionConverters.asJava(whiteList));
    }

    public static MiniListSyncMessage create(BlockPos blockPos, int dim, Collection<QuarryBlackList.Entry> blackList, Collection<QuarryBlackList.Entry> whiteList) {
        MiniListSyncMessage message = new MiniListSyncMessage();
        message.pos = blockPos;
        message.dim = dim;
        INBT listBlack = NBTDynamicOps.INSTANCE.createList(blackList.stream().map(e -> QuarryBlackList.writeEntry(e, NBTDynamicOps.INSTANCE)));
        INBT listWhite = NBTDynamicOps.INSTANCE.createList(whiteList.stream().map(e -> QuarryBlackList.writeEntry(e, NBTDynamicOps.INSTANCE)));
        message.listTag = new CompoundNBT();
        message.listTag.put("black", listBlack);
        message.listTag.put("white", listWhite);
        return message;
    }

    @Override
    public MiniListSyncMessage readFromBuffer(PacketBuffer buffer) {
        MiniListSyncMessage message = new MiniListSyncMessage();
        message.pos = buffer.readBlockPos();
        message.dim = buffer.readInt();
        message.listTag = buffer.readCompoundTag();
        return message;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeCompoundTag(listTag);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        Collection<QuarryBlackList.Entry> b = listTag.getList("black", Constants.NBT.TAG_COMPOUND)
            .stream().map(n -> new Dynamic<>(NBTDynamicOps.INSTANCE, n)).map(QuarryBlackList::readEntry).collect(Collectors.toList());
        Collection<QuarryBlackList.Entry> w = listTag.getList("white", Constants.NBT.TAG_COMPOUND)
            .stream().map(n -> new Dynamic<>(NBTDynamicOps.INSTANCE, n)).map(QuarryBlackList::readEntry).collect(Collectors.toList());
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            IMessage.findTile(ctx, pos, dim, MiniQuarryTile.class).ifPresent(t ->
                ctx.get().enqueueWork(() -> Minecraft.getInstance().displayGuiScreen(new MiniQuarryListGui(t, w, b))));
        } else if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
            IMessage.findTile(ctx, pos, dim, MiniQuarryTile.class).ifPresent(t -> ctx.get().enqueueWork(() -> {
                t.blackList_$eq(CollectionConverters.asScala(b).toSet());
                t.whiteList_$eq(CollectionConverters.asScala(w).toSet());
            }));
        }
    }
}
