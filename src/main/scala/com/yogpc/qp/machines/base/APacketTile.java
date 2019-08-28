/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines.base;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class APacketTile extends TileEntity {
    public static final BinaryOperator<String> combiner = (s, s2) -> s + ", " + s2;
    public static final Function<String, StringTextComponent> toComponentString = StringTextComponent::new;
    public static final Consumer<IChunkLoadTile> requestTicket = IChunkLoadTile::requestTicket;

    private final ITextComponent displayName;
    protected final boolean machineDisabled;
    protected final boolean isDebugSender = this instanceof IDebugSender;
    protected final List<Runnable> startListener = new ArrayList<>();
    protected final List<Runnable> finishListener = new ArrayList<>();

    protected APacketTile(TileEntityType<?> type) {
        super(type);
        if (this instanceof HasInv) {
            HasInv hasInv = (HasInv) this;
            displayName = hasInv.getName();
        } else if (isDebugSender) {
            IDebugSender sender = (IDebugSender) this;
            displayName = new TranslationTextComponent(sender.getDebugName());
        } else {
            displayName = new StringTextComponent("APacketTile");
        }

        machineDisabled = !Holder.tiles().apply(type).enabled();
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    public ITextComponent getDisplayName() {
        return displayName;
    }

    protected final void startWork() {
        if (hasWorld())
            startListener.forEach(Runnable::run);
    }

    protected final void finishWork() {
        if (hasWorld())
            finishListener.forEach(Runnable::run);
    }

    public final boolean enabled() {
        return !machineDisabled;
    }

    public static <T> T invoke(Method method, Class<T> returnType, Object ref, Object... param) {
        try {
            return returnType.cast(method.invoke(ref, param));
        } catch (ReflectiveOperationException e) {
            QuarryPlus.LOGGER.warn(e);
            return null;
        }
    }
}
