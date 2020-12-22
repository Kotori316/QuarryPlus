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

package com.yogpc.qp.machines.quarry;

import java.util.Map;
import java.util.Optional;

import com.yogpc.qp.Config;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import scala.Symbol;

import static jp.t2v.lab.syntax.MapStreamSyntax.byEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;

public class TileMiningWell extends TileBasic implements ITickableTileEntity {
    public static final Symbol SYMBOL = Symbol.apply("MiningwellPlus");

    private boolean working;

    public TileMiningWell() {
        super(Holder.miningWellTileType());
    }

    @Override
    public void G_renew_powerConfigure() {
        byte pmp = 0;
        if (hasWorld()) {
            assert world != null;
            Map<IAttachment.Attachments<?>, Direction> map = facingMap.entrySet().stream()
                .filter(byEntry((attachments, facing) -> attachments.test(world.getTileEntity(getPos().offset(facing)))))
                .collect(entryToMap());
            facingMap.putAll(map);
            pmp = Optional.ofNullable(facingMap.get(IAttachment.Attachments.FLUID_PUMP))
                .map(getPos()::offset)
                .map(world::getTileEntity)
                .flatMap(IAttachment.Attachments.FLUID_PUMP)
                .map(p -> p.unbreaking).orElse((byte) 0);
        }

        if (this.working)
            PowerManager.configureMiningWell(this, this.efficiency, this.unbreaking, pmp);
        else
            PowerManager.configure0(this);
    }

    @Override
    public StringTextComponent getName() {
        return new StringTextComponent(TranslationKeys.miningwell);
    }

    @Override
    public void workInTick() {
        assert world != null;
        int depth = getPos().getY() - 1;
        while (!S_checkTarget(depth)) {
            BlockPos pos = new BlockPos(getPos().getX(), depth, getPos().getZ());
            if (this.working && (Config.common().removeBedrock().get() || world.getBlockState(pos).getBlockHardness(world, pos) >= 0)) {
                world.setBlockState(pos, Holder.blockPlainPipe().getDefaultState());
            }
            depth--;
        }
        if (this.working && getStoredEnergy() > 0)
            S_breakBlock(getPos().getX(), depth, getPos().getZ(), Blocks.AIR.getDefaultState());
        S_pollItems();
    }

    @Override
    protected boolean isWorking() {
        return working;
    }

    private boolean S_checkTarget(final int depth) {
        if (depth < yLevel) {
            G_destroy();
            finishWork();
            return true;
        }
        BlockPos pos = new BlockPos(getPos().getX(), depth, getPos().getZ());
        assert world != null;
        final BlockState b = world.getBlockState(pos);
        final float h = b.getBlockHardness(world, pos);
        if (h < 0 || b.getBlock() == Holder.blockPlainPipe() || b.getBlock().isAir(b, world, pos) || skipped.contains(pos)) {
            return false;
        }
        if (!facingMap.containsKey(IAttachment.Attachments.FLUID_PUMP) && TilePump.isLiquid(b))
            return false;
        if (!this.working) {
            //Find something to break!
            G_ReInit();
        }
        return true;
    }

    @Override
    public void read(final CompoundNBT nbt) {
        super.read(nbt);
        setWorking(nbt.getBoolean("working"));
        G_renew_powerConfigure();
    }

    @Override
    public CompoundNBT write(final CompoundNBT nbt) {
        nbt.putBoolean("working", this.working);
        return super.write(nbt);
    }

    @Override
    public void G_ReInit() {
        setWorking(true);
        G_renew_powerConfigure();
        if (world != null && !world.isRemote)
            PacketHandler.sendToAround(TileMessage.create(this), world, getPos());
    }

    @Override
    protected void G_destroy() {
        if (world != null && !world.isRemote) {
            working = false; //TODO method cause loop. -> check.
            G_renew_powerConfigure();
            PacketHandler.sendToAround(TileMessage.create(this), world, getPos());
            removePipes();
        }
    }

    public void removePipes() {
        assert world != null;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        pos.setPos(getPos());
        for (int depth = getPos().getY() - 1; depth > 0; depth--) {
            pos.setY(depth);
            if (world.getBlockState(pos).getBlock() != Holder.blockPlainPipe())
                break;
            world.removeBlock(pos, false);
        }
    }

    private void setWorking(boolean working) {
        this.working = working;
        if (working)
            startWork();
        if (world != null) {
            BlockState old = world.getBlockState(getPos());
            world.setBlockState(getPos(), old.with(QPBlock.WORKING(), working));
        }
    }

}
