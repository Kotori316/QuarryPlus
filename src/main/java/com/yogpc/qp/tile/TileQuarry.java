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

import java.util.List;
import java.util.Set;

import buildcraft.api.core.IAreaProvider;
import com.google.common.collect.Sets;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockFrame;
import com.yogpc.qp.block.BlockQuarry;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.packet.quarry.ModeMessage;
import com.yogpc.qp.packet.quarry.MoveHead;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.yogpc.qp.tile.TileQuarry.Mode.BREAKBLOCK;
import static com.yogpc.qp.tile.TileQuarry.Mode.MAKEFRAME;
import static com.yogpc.qp.tile.TileQuarry.Mode.MOVEHEAD;
import static com.yogpc.qp.tile.TileQuarry.Mode.NONE;
import static com.yogpc.qp.tile.TileQuarry.Mode.NOTNEEDBREAK;

public class TileQuarry extends TileBasic {
    private int targetX, targetY, targetZ;
    public int xMin, xMax, yMin, yMax = Integer.MIN_VALUE, zMin, zMax;
    public boolean filler;

    /**
     * TODO Cause {@link NoClassDefFoundError}
     */
    private IAreaProvider areaProvider = null;

    private void S_updateEntity() {
        if (this.areaProvider != null) {
            if (this.areaProvider instanceof TileMarker)
                this.cacheItems.addAll(((TileMarker) this.areaProvider).removeFromWorldWithItem());
            else
                this.areaProvider.removeFromWorld();
            this.areaProvider = null;
        }
        switch (this.now) {
            case MAKEFRAME:
                if (S_makeFrame())
                    while (!S_checkTarget())
                        S_setNextTarget();
                break;
            case MOVEHEAD:
                final boolean done = S_moveHead();
                PacketHandler.sendToAround(MoveHead.create(this), getWorld(), getPos());
                if (!done)
                    break;
                this.now = BREAKBLOCK;
                //$FALL-THROUGH$
            case NOTNEEDBREAK:
            case BREAKBLOCK:
                if (S_breakBlock())
                    while (!S_checkTarget())
                        S_setNextTarget();
                break;
            case NONE:
                break;
        }
        S_pollItems();
    }

    private boolean S_checkTarget() {
        if (this.targetY > this.yMax)
            this.targetY = this.yMax;
        BlockPos target = new BlockPos(this.targetX, this.targetY, this.targetZ);
        final IBlockState b = getWorld().getChunkProvider().getLoadedChunk(this.targetX >> 4, this.targetZ >> 4).getBlockState(target);
        final float h = b.getBlockHardness(getWorld(), target);
        switch (this.now) {
            case BREAKBLOCK:
            case MOVEHEAD:
                if (this.targetY < 1) {
                    G_destroy();
                    sendNowPacket(this);
                    return true;
                }
                return !(h < 0) && !b.getBlock().isAir(b, getWorld(), target) && (this.pump != null || !TilePump.isLiquid(b, false, getWorld(), target));
            case NOTNEEDBREAK:
                if (this.targetY < this.yMin) {
                    if (this.filler) {
                        G_destroy();
                        sendNowPacket(this);
                        return true;
                    }
                    this.now = MAKEFRAME;
                    G_renew_powerConfigure();
                    this.targetX = this.xMin;
                    this.targetY = this.yMax;
                    this.targetZ = this.zMin;
                    this.addX = this.addZ = this.digged = true;
                    this.changeZ = false;
                    sendNowPacket(this);
                    return S_checkTarget();
                }
                if (h < 0 || b.getBlock().isAir(b, getWorld(), target))
                    return false;
                if (this.pump == null && TilePump.isLiquid(b, false, getWorld(), target))
                    return false;
                if (b.getBlock() == QuarryPlusI.blockFrame && !b.getValue(BlockFrame.DAMMING)) {
                    byte flag = 0;
                    if (this.targetX == this.xMin || this.targetX == this.xMax)
                        flag++;
                    if (this.targetY == this.yMin || this.targetY == this.yMax)
                        flag++;
                    if (this.targetZ == this.zMin || this.targetZ == this.zMax)
                        flag++;
                    if (flag > 1)
                        return false;
                }
                return true;
            case MAKEFRAME:
                if (this.targetY < this.yMin) {
                    this.now = MOVEHEAD;
                    G_renew_powerConfigure();
                    this.targetX = this.xMin + 1;
                    this.targetY = this.yMin;
                    this.targetZ = this.zMin + 1;
                    this.addX = this.addZ = this.digged = true;
                    this.changeZ = false;
                    sendNowPacket(this);
                    return S_checkTarget();
                }
                if (b.getMaterial().isSolid()
                        && !(b == QuarryPlusI.blockFrame && !b.getValue(BlockFrame.DAMMING))) {
                    this.now = NOTNEEDBREAK;
                    G_renew_powerConfigure();
                    this.targetX = this.xMin;
                    this.targetZ = this.zMin;
                    this.targetY = this.yMax;
                    this.addX = this.addZ = this.digged = true;
                    this.changeZ = false;
                    sendNowPacket(this);
                    return S_checkTarget();
                }
                byte flag = 0;
                if (this.targetX == this.xMin || this.targetX == this.xMax)
                    flag++;
                if (this.targetY == this.yMin || this.targetY == this.yMax)
                    flag++;
                if (this.targetZ == this.zMin || this.targetZ == this.zMax)
                    flag++;
                return flag > 1 && (b != QuarryPlusI.blockFrame || b.getValue(BlockFrame.DAMMING));
            case NONE:
                break;
        }
        return true;
    }

    private static void sendNowPacket(TileQuarry quarry) {
        com.yogpc.qp.packet.PacketHandler.sendToAround(ModeMessage.create(quarry), quarry.getWorld(), quarry.getPos());
    }

    private boolean addX = true;
    private boolean addZ = true;
    private boolean digged = true;
    private boolean changeZ = false;

    private void S_setNextTarget() {
        if (this.now == MAKEFRAME) {
            if (this.changeZ) {
                if (this.addZ)
                    this.targetZ++;
                else
                    this.targetZ--;
            } else if (this.addX)
                this.targetX++;
            else
                this.targetX--;
            if (this.targetX < this.xMin || this.xMax < this.targetX) {
                this.addX = !this.addX;
                this.changeZ = true;
                this.targetX = Math.max(this.xMin, Math.min(this.xMax, this.targetX));
            }
            if (this.targetZ < this.zMin || this.zMax < this.targetZ) {
                this.addZ = !this.addZ;
                this.changeZ = false;
                this.targetZ = Math.max(this.zMin, Math.min(this.zMax, this.targetZ));
            }
            if (this.xMin == this.targetX && this.zMin == this.targetZ)
                if (this.digged)
                    this.digged = false;
                else
                    this.targetY--;
        } else {
            if (this.addX)
                this.targetX++;
            else
                this.targetX--;
            final int out = this.now == NOTNEEDBREAK ? 0 : 1;
            if (this.targetX < this.xMin + out || this.xMax - out < this.targetX) {
                this.addX = !this.addX;
                this.targetX = Math.max(this.xMin + out, Math.min(this.targetX, this.xMax - out));
                if (this.addZ)
                    this.targetZ++;
                else
                    this.targetZ--;
                if (this.targetZ < this.zMin + out || this.zMax - out < this.targetZ) {
                    this.addZ = !this.addZ;
                    this.targetZ = Math.max(this.zMin + out, Math.min(this.targetZ, this.zMax - out));
                    if (this.digged)
                        this.digged = false;
                    else {
                        this.targetY--;
                        final double aa = S_getDistance(this.xMin + 1, this.targetY, this.zMin + out);
                        final double ad = S_getDistance(this.xMin + 1, this.targetY, this.zMax - out);
                        final double da = S_getDistance(this.xMax - 1, this.targetY, this.zMin + out);
                        final double dd = S_getDistance(this.xMax - 1, this.targetY, this.zMax - out);
                        final double res = Math.min(aa, Math.min(ad, Math.min(da, dd)));
                        if (res == aa) {
                            this.addX = true;
                            this.addZ = true;
                            this.targetX = this.xMin + out;
                            this.targetZ = this.zMin + out;
                        } else if (res == ad) {
                            this.addX = true;
                            this.addZ = false;
                            this.targetX = this.xMin + out;
                            this.targetZ = this.zMax - out;
                        } else if (res == da) {
                            this.addX = false;
                            this.addZ = true;
                            this.targetX = this.xMax - out;
                            this.targetZ = this.zMin + out;
                        } else if (res == dd) {
                            this.addX = false;
                            this.addZ = false;
                            this.targetX = this.xMax - out;
                            this.targetZ = this.zMax - out;
                        }
                    }
                }
            }
        }
    }

    private double S_getDistance(final int x, final int y, final int z) {
        return Math.sqrt(Math.pow(x - this.headPosX, 2) + Math.pow(y + 1 - this.headPosY, 2)
                + Math.pow(z - this.headPosZ, 2));
    }

    private boolean S_makeFrame() {
        this.digged = true;
        if (!PowerManager.useEnergyF(this, this.unbreaking))
            return false;
        getWorld().setBlockState(new BlockPos(this.targetX, this.targetY, this.targetZ), QuarryPlusI.blockFrame.getDefaultState());
        S_setNextTarget();
        return true;
    }

    private boolean S_breakBlock() {
        this.digged = true;
        if (S_breakBlock(this.targetX, this.targetY, this.targetZ)) {
            S_checkDropItem();
            if (this.now == BREAKBLOCK)
                this.now = MOVEHEAD;
            S_setNextTarget();
            return true;
        }
        return false;
    }

    private void S_checkDropItem() {
        final AxisAlignedBB axis = new AxisAlignedBB(this.targetX - 4, this.targetY - 4, this.targetZ - 4, this.targetX + 6, this.targetY + 6, this.targetZ + 6);
        final List<EntityItem> result = getWorld().getEntitiesWithinAABB(EntityItem.class, axis);
        for (EntityItem entityItem : result) {
            if (entityItem.isDead)
                continue;
            final ItemStack drop = entityItem.getEntityItem();
            if (drop.isEmpty())
                continue;
            QuarryPlus.proxy.removeEntity(entityItem);
            this.cacheItems.add(drop);
        }
    }

    private void S_createBox() {
        //TODO make simply as 1.12
        if (this.yMax != Integer.MIN_VALUE)
            return;
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        if (!S_checkIAreaProvider(xCoord - 1, yCoord, zCoord))
            if (!S_checkIAreaProvider(xCoord + 1, yCoord, zCoord))
                if (!S_checkIAreaProvider(xCoord, yCoord, zCoord - 1))
                    if (!S_checkIAreaProvider(xCoord, yCoord, zCoord + 1))
                        if (!S_checkIAreaProvider(xCoord, yCoord - 1, zCoord))
                            if (!S_checkIAreaProvider(xCoord, yCoord + 1, zCoord)) {
                                final EnumFacing o = getWorld().getBlockState(getPos()).getValue(BlockQuarry.FACING).getOpposite();
                                switch (o) {
                                    case EAST:
                                        xMin = xCoord + 1;
                                        zMin = zCoord - 5;
                                        break;
                                    case WEST:
                                        xMin = xCoord - 11;
                                        zMin = zCoord - 5;
                                        break;
                                    case SOUTH:
                                        xMin = xCoord - 5;
                                        zMin = zCoord + 1;
                                        break;
                                    case DOWN:
                                    case UP:
                                    case NORTH:
                                        xMin = xCoord - 5;
                                        zMin = zCoord - 11;
                                        break;
                                }
                                yMin = yCoord;
                                xMax = xMin + 10;
                                zMax = zMin + 10;
                                yMax = yCoord + 4;
                            }
    }

    private boolean S_checkIAreaProvider(final int x, final int y, final int z) {
        //TODO make simply as 1.12
        final TileEntity te = getWorld().getTileEntity(new BlockPos(x, y, z));
        if (ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.BuildCraft_core) && te instanceof IAreaProvider) {
            this.areaProvider = (IAreaProvider) te;
            this.xMin = this.areaProvider.min().getX();
            this.yMin = this.areaProvider.min().getY();
            this.zMin = this.areaProvider.min().getZ();
            this.xMax = this.areaProvider.max().getX();
            this.yMax = this.areaProvider.max().getY();
            this.zMax = this.areaProvider.max().getZ();
            int tmp;
            if (this.xMin > this.xMax) {
                tmp = this.xMin;
                this.xMin = this.xMax;
                this.xMax = tmp;
            }
            if (this.yMin > this.yMax) {
                tmp = this.yMin;
                this.yMin = this.yMax;
                this.yMax = tmp;
            }
            if (this.zMin > this.zMax) {
                tmp = this.zMin;
                this.zMin = this.zMax;
                this.zMax = tmp;
            }
            if (getPos().getX() >= this.xMin && getPos().getX() <= this.xMax && getPos().getY() >= this.yMin && getPos().getY() <= this.yMax && getPos().getZ() >= this.zMin && getPos().getZ() <= this.zMax) {
                this.yMax = Integer.MIN_VALUE;
                return false;
            }
            if (this.xMax - this.xMin < 2 || this.zMax - this.zMin < 2) {
                this.yMax = Integer.MIN_VALUE;
                return false;
            }
            if (this.yMax - this.yMin < 2)
                this.yMax = this.yMin + 3;
            return true;
        }
        return false;
    }

    private void S_setFirstPos() {
        this.targetX = this.xMin;
        this.targetZ = this.zMin;
        this.targetY = this.yMax;
        this.headPosX = (this.xMin + this.xMax + 1) / 2;
        this.headPosZ = (this.zMin + this.zMax + 1) / 2;
        this.headPosY = this.yMax - 1;
    }

    private boolean S_moveHead() {
        final double x = this.targetX - this.headPosX;
        final double y = this.targetY + 1 - this.headPosY;
        final double z = this.targetZ - this.headPosZ;
        final double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        final double blocks = PowerManager.useEnergyH(this, distance, this.unbreaking);
        if (blocks * 2 >= distance) {
            this.headPosX = this.targetX;
            this.headPosY = this.targetY + 1;
            this.headPosZ = this.targetZ;
            return true;
        }
        if (blocks > 0) {
            this.headPosX += x * blocks / distance;
            this.headPosY += y * blocks / distance;
            this.headPosZ += z * blocks / distance;
        }
        return false;
    }

    public Mode G_getNow() {
        return this.now;
    }

    public void setNow(Mode now) {
        this.now = now;
    }

    @Override
    protected void G_destroy() {
        this.now = NONE;
        G_renew_powerConfigure();
        if (!getWorld().isRemote) {
            sendNowPacket(this);
        }
        ForgeChunkManager.releaseTicket(this.chunkTicket);
    }

    @Override
    public void G_reinit() {
        if (this.yMax == Integer.MIN_VALUE && !getWorld().isRemote)
            S_createBox();
        this.now = NOTNEEDBREAK;
        G_renew_powerConfigure();
        if (!getWorld().isRemote) {
            S_setFirstPos();
            PacketHandler.sendToAround(TileMessage.create(this), getWorld(), getPos());
        }
    }

    private Ticket chunkTicket;

    public void requestTicket() {
        if (this.chunkTicket != null)
            return;
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, getWorld(), Type.NORMAL);
        if (this.chunkTicket == null)
            return;
        final NBTTagCompound tag = this.chunkTicket.getModData();

        tag.setInteger("quarryX", getPos().getX());
        tag.setInteger("quarryY", getPos().getY());
        tag.setInteger("quarryZ", getPos().getZ());
        forceChunkLoading(this.chunkTicket);
    }

    public void forceChunkLoading(final Ticket ticket) {
        if (this.chunkTicket == null)
            this.chunkTicket = ticket;
        final Set<ChunkPos> chunks = Sets.newHashSet();
        final ChunkPos quarryChunk = new ChunkPos(getPos());
        chunks.add(quarryChunk);
        ForgeChunkManager.forceChunk(ticket, quarryChunk);
    }

    @Override
    public void update() {
        super.update();
        if (!this.initialized) {
            G_renew_powerConfigure();
            this.initialized = true;
        }
        if (!getWorld().isRemote)
            S_updateEntity();
    }

    @Override
    protected boolean isWorking() {
        return now != NONE;
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        this.xMin = nbttc.getInteger("xMin");
        this.xMax = nbttc.getInteger("xMax");
        this.yMin = nbttc.getInteger("yMin");
        this.zMin = nbttc.getInteger("zMin");
        this.zMax = nbttc.getInteger("zMax");
        this.yMax = nbttc.getInteger("yMax");
        this.targetX = nbttc.getInteger("targetX");
        this.targetY = nbttc.getInteger("targetY");
        this.targetZ = nbttc.getInteger("targetZ");
        this.addZ = nbttc.getBoolean("addZ");
        this.addX = nbttc.getBoolean("addX");
        this.digged = nbttc.getBoolean("digged");
        this.changeZ = nbttc.getBoolean("changeZ");
        this.now = Mode.values()[nbttc.getByte("now")];
        this.headPosX = nbttc.getDouble("headPosX");
        this.headPosY = nbttc.getDouble("headPosY");
        this.headPosZ = nbttc.getDouble("headPosZ");
        this.filler = nbttc.getBoolean("filler");
        this.initialized = false;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbttc) {
        nbttc.setInteger("xMin", this.xMin);
        nbttc.setInteger("xMax", this.xMax);
        nbttc.setInteger("yMin", this.yMin);
        nbttc.setInteger("yMax", this.yMax);
        nbttc.setInteger("zMin", this.zMin);
        nbttc.setInteger("zMax", this.zMax);
        nbttc.setInteger("targetX", this.targetX);
        nbttc.setInteger("targetY", this.targetY);
        nbttc.setInteger("targetZ", this.targetZ);
        nbttc.setBoolean("addZ", this.addZ);
        nbttc.setBoolean("addX", this.addX);
        nbttc.setBoolean("digged", this.digged);
        nbttc.setBoolean("changeZ", this.changeZ);
        nbttc.setByte("now", ((byte) this.now.ordinal()));
        nbttc.setDouble("headPosX", this.headPosX);
        nbttc.setDouble("headPosY", this.headPosY);
        nbttc.setDouble("headPosZ", this.headPosZ);
        nbttc.setBoolean("filler", this.filler);
        return super.writeToNBT(nbttc);
    }

    public double headPosX, headPosY, headPosZ;
    private boolean initialized = true;

    private Mode now = NONE;

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
        if (this.now == NONE)
            PowerManager.configure0(this);
        else if (this.now == MAKEFRAME)
            PowerManager.configureF(this, this.efficiency, this.unbreaking, pmp);
        else
            PowerManager.configureB(this, this.efficiency, this.unbreaking, pmp);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {

        if (this.yMax == Integer.MIN_VALUE)
            return new AxisAlignedBB(getPos().getX(), Double.NEGATIVE_INFINITY, getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
        final double xn = this.xMin + 0.46875;
        final double xx = this.xMax + 0.53125;
        final double yx = this.yMax + 0.75;
        final double zn = this.zMin + 0.46875;
        final double zx = this.zMax + 0.53125;
        double x1, x2, y2, z1, z2;
        if (xn < getPos().getX())
            x1 = xn;
        else x1 = getPos().getX();

        if (xx > getPos().getX() + 1)
            x2 = xx;
        else x2 = getPos().getX() + 1;

        if (getPos().getY() + 1 < yx)
            y2 = yx;
        else y2 = getPos().getY() + 1;

        if (zn < getPos().getZ())
            z1 = zn;
        else z1 = getPos().getZ();

        if (zx > getPos().getZ() + 1)
            z2 = zx;
        else z2 = getPos().getZ() + 1;

        return new AxisAlignedBB(x1, Double.NEGATIVE_INFINITY, z1, x2, y2, z2);
    }

    public enum Mode {
        NONE,
        NOTNEEDBREAK,
        MAKEFRAME,
        MOVEHEAD,
        BREAKBLOCK
    }
}
