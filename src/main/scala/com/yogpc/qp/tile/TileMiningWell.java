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

package com.yogpc.qp.tile;

import com.yogpc.qp.PowerManager;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockMiningWell;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import scala.Symbol;

public class TileMiningWell extends TileBasic implements ITickable {
    public static final scala.Symbol SYMBOL = scala.Symbol.apply("MiningwellPlus");

    public boolean working;

    @Override
    public void G_renew_powerConfigure() {
        byte pmp = 0;
        if (hasWorld() && this.pump != null) {
            final TileEntity te = getWorld().getTileEntity(getPos().offset(pump));
            if (te instanceof TilePump)
                pmp = ((TilePump) te).unbreaking;
            else
                this.pump = null;
        }
        if (this.working)
            PowerManager.configureMiningWell(this, this.efficiency, this.unbreaking, pmp);
        else
            PowerManager.configure0(this);
    }

    @Override
    public String getName() {
        return TranslationKeys.miningwell;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && !machineDisabled) {
            int depth = getPos().getY() - 1;
            while (!S_checkTarget(depth)) {
                if (this.working)
                    getWorld().setBlockState(new BlockPos(getPos().getX(), depth, getPos().getZ()), QuarryPlusI.blockPlainPipe().getDefaultState());
                depth--;
            }
            if (this.working)
                S_breakBlock(getPos().getX(), depth, getPos().getZ());
            S_pollItems();
        }
    }

    @Override
    protected boolean isWorking() {
        return working;
    }

    private boolean S_checkTarget(final int depth) {
        if (depth < 1) {
            G_destroy();
            return true;
        }
        BlockPos pos = new BlockPos(getPos().getX(), depth, getPos().getZ());
        Chunk loadedChunk = getWorld().getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
        final IBlockState b;
        if (loadedChunk != null) {
            b = loadedChunk.getBlockState(pos);
        } else {
            b = getWorld().getBlockState(pos);
        }
        final float h = b.getBlockHardness(getWorld(), pos);
        if (h < 0 || b.getBlock() == QuarryPlusI.blockPlainPipe() || b.getBlock().isAir(b, getWorld(), pos)) {
            return false;
        }
        if (this.pump == null && b.getMaterial().isLiquid())
            return false;
        if (!this.working) {
            //Find something to break!
            G_reinit();
        }
        return true;
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        setWorking(nbttc.getBoolean("working"));
        G_renew_powerConfigure();
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttc) {
        nbttc.setBoolean("working", this.working);
        return super.writeToNBT(nbttc);
    }

    @Override
    public void G_reinit() {
        setWorking(true);
        G_renew_powerConfigure();
        if (!getWorld().isRemote)
            PacketHandler.sendToAround(TileMessage.create(this), getWorld(), getPos());
    }

    @Override
    protected void G_destroy() {
        if (!getWorld().isRemote) {
            working = false; //TODO method cause loop. -> check.
            G_renew_powerConfigure();
            PacketHandler.sendToAround(TileMessage.create(this), getWorld(), getPos());
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(getPos());
            for (int depth = getPos().getY() - 1; depth > 0; depth--) {
                pos.setY(depth);
                if (getWorld().getBlockState(pos).getBlock() != QuarryPlusI.blockPlainPipe())
                    break;
                getWorld().setBlockToAir(pos);
            }
        }
    }

    public void setWorking(boolean working) {
        this.working = working;
        if (hasWorld()) {
            IBlockState old = getWorld().getBlockState(getPos());
            validate();
            getWorld().setBlockState(getPos(), old.withProperty(BlockMiningWell.ACTING, working));
            validate();
            getWorld().setTileEntity(getPos(), this);
        }
    }

    @Override
    protected Symbol getSymbol() {
        return SYMBOL;
    }
}
