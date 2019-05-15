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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.ModLoadingContext;

public abstract class APacketTile extends TileEntity implements IDisabled {
    public static final BinaryOperator<String> combiner = (s, s2) -> s + ", " + s2;
    public static final Function<String, TextComponentString> toComponentString = TextComponentString::new;
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
            displayName = new TextComponentTranslation(sender.getDebugName());
        } else {
            displayName = new TextComponentString("APacketTile");
        }

        machineDisabled = ModLoadingContext.get().extension() != null || !enabled();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        read(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return write(new NBTTagCompound());
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

    @Override
    public abstract scala.Symbol getSymbol();

    @SuppressWarnings({"SameParameterValue"})
    protected static <T> T invoke(Method method, Class<T> returnType, Object ref, Object... param) {
        try {
            return returnType.cast(method.invoke(ref, param));
        } catch (ReflectiveOperationException e) {
            QuarryPlus.LOGGER.warn(e);
            return null;
        }
    }
}
