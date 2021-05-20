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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.tiles.TilesAPI;
import com.yogpc.qp.Config;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockFrame;
import com.yogpc.qp.block.BlockQuarry;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.packet.quarry.ModeMessage;
import com.yogpc.qp.packet.quarry.MoveHead;
import com.yogpc.qp.version.VersionUtil;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.BooleanUtils;
import scala.Symbol;
import scala.collection.JavaConverters;

import static com.yogpc.qp.tile.IAttachment.Attachments.EXP_PUMP;
import static com.yogpc.qp.tile.IAttachment.Attachments.FLUID_PUMP;
import static com.yogpc.qp.tile.IAttachment.Attachments.REPLACER;
import static com.yogpc.qp.tile.TileQuarry.Mode.BREAK_BLOCK;
import static com.yogpc.qp.tile.TileQuarry.Mode.MAKE_FRAME;
import static com.yogpc.qp.tile.TileQuarry.Mode.MOVE_HEAD;
import static com.yogpc.qp.tile.TileQuarry.Mode.NONE;
import static com.yogpc.qp.tile.TileQuarry.Mode.NOT_NEED_BREAK;
import static jp.t2v.lab.syntax.MapStreamSyntax.byEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.streamCast;

public class TileQuarry extends TileBasic implements IDebugSender, IChunkLoadTile {
    public static final Symbol SYMBOL = Symbol.apply("QuarryPlus");
    private int targetX, targetY, targetZ;
    private int preX, preY, preZ;
    private int coolFillerMode;
    public int xMin, xMax, yMin, yMax = Integer.MIN_VALUE, zMin, zMax;
    public boolean filler;

    /**
     * The marker of {@link IAreaProvider} or {@link IMarker}.
     */
    @Nullable
    private Object areaProvider = null;

    @Override
    protected Symbol getSymbol() {
        return SYMBOL;
    }

    @SuppressWarnings("fallthrough")
    protected void S_updateEntity() {
        if (machineDisabled) return;
        if (this.areaProvider != null) {
            if (this.areaProvider instanceof IMarker)
                this.cacheItems.addAll(((IMarker) this.areaProvider).removeFromWorldWithItem());
            else if (bcLoaded && areaProvider instanceof IAreaProvider) {
                ((IAreaProvider) this.areaProvider).removeFromWorld();
            }
            this.areaProvider = null;
        }
        boolean faster = Config.content().fastQuarryHeadMove();
        boolean broken = false;
        for (int i = 0; i < efficiency + 1 && !broken; i++) {
            if (!faster) broken = true;
            switch (this.now) {
                case MAKE_FRAME:
                    if (S_makeFrame())
                        while (!S_checkTarget())
                            S_setNextTarget();
                    broken = true;
                    break;
                case MOVE_HEAD:
                    final boolean done = S_moveHead();
                    MoveHead.send(this);
                    if (!done) {
                        broken = true;
                        break;
                    }
                    setNow(BREAK_BLOCK);
                    break;
                //$FALL-THROUGH$
                case NOT_NEED_BREAK:
                    broken = !filler && coolFillerMode <= 0;
                    if (coolFillerMode-- > 0) break;
                case BREAK_BLOCK:
                    if (S_breakBlock())
                        while (!S_checkTarget())
                            S_setNextTarget();
                    break;
                case NONE:
                    broken = true;
                    break;
            }
        }
        S_pollItems();
    }

    private boolean S_checkTarget() {
        if (this.targetY > this.yMax)
            this.targetY = this.yMax;
        BlockPos target = new BlockPos(this.targetX, this.targetY, this.targetZ);
        Chunk loadedChunk = getWorld().getChunkProvider().getLoadedChunk(this.targetX >> 4, this.targetZ >> 4);
        final IBlockState b;
        if (loadedChunk != null) {
            b = loadedChunk.getBlockState(target);
        } else {
            b = getWorld().getBlockState(target);
        }
        final float blockHardness = b.getBlockHardness(getWorld(), target);
        switch (this.now) {
            case BREAK_BLOCK:
            case MOVE_HEAD:
                if (this.targetY < yLevel) {
                    G_destroy();
                    S_checkDropItem(new AxisAlignedBB(xMin - 2, yLevel - 3, zMin - 2, xMax + 2, yLevel + 2, zMax + 2));
                    PacketHandler.sendToAround(ModeMessage.create(this), getWorld(), getPos());
                    return true;
                }
                return isBreakableBlock(target, b, blockHardness);
            case NOT_NEED_BREAK:
                if (this.targetY < this.yMin) {
                    if (this.filler) {
                        G_destroy();
//                        sendNowPacket(this);
                        return true;
                    }
                    setNow(MAKE_FRAME);
                    G_renew_powerConfigure();
                    initTargetPos();
                    this.addX = this.addZ = this.dug = true;
                    this.changeZ = false;
                    return S_checkTarget();
                }
                if (!isBreakableBlock(target, b, blockHardness))
                    return false;
                this.coolFillerMode = scala.Int.unbox(Config.content().tickDelay().apply(getSymbol()));
                if (b.getBlock() == QuarryPlusI.blockFrame() && !b.getValue(BlockFrame.DAMMING)) {
                    return Stream.of(
                        this.targetX == this.xMin || this.targetX == this.xMax,
                        this.targetY == this.yMin || this.targetY == this.yMax,
                        this.targetZ == this.zMin || this.targetZ == this.zMax)
                        .mapToInt(BooleanUtils::toInteger)
                        .sum() < 2; // true if 0 or 1 condition is true.
                }
                return true;
            case MAKE_FRAME:
                if (this.targetY < this.yMin) {
                    setNow(MOVE_HEAD);
                    G_renew_powerConfigure();
                    this.targetX = this.xMin + 1;
                    this.targetY = this.yMin;
                    this.targetZ = this.zMin + 1;
                    this.addX = this.addZ = this.dug = true;
                    this.changeZ = false;
                    return S_checkTarget();
                }
                if (blockHardness < 0)
                    return false; // Avoid dead lock caused by infinite loop. Bedrock or unbreakable blocks.
                if (b.getMaterial().isSolid()
                    && !(b.getBlock() == QuarryPlusI.blockFrame() && !b.getValue(BlockFrame.DAMMING))) {
                    if (Config.content().disableFillerMode()) {
                        G_destroy();
                        return false;
                    }
                    setNow(NOT_NEED_BREAK);
                    this.coolFillerMode = scala.Int.unbox(Config.content().tickDelay().apply(getSymbol()));
                    G_renew_powerConfigure();
                    initTargetPos();
                    this.addX = this.addZ = this.dug = true;
                    this.changeZ = false;
                    return S_checkTarget();
                }
                byte flag = 0;
                if (this.targetX == this.xMin || this.targetX == this.xMax)
                    flag++;
                if (this.targetY == this.yMin || this.targetY == this.yMax)
                    flag++;
                if (this.targetZ == this.zMin || this.targetZ == this.zMax)
                    flag++;
                return flag > 1 && (b.getBlock() != QuarryPlusI.blockFrame() || b.getValue(BlockFrame.DAMMING));
            case NONE:
                break;
        }
        return true;
    }

    private boolean isBreakableBlock(BlockPos target, IBlockState b, float blockHardness) {
        return blockHardness >= 0 && // Not to break unbreakable
            !b.getBlock().isAir(b, getWorld(), target) && // Avoid air
            (now == NOT_NEED_BREAK || !facingMap.containsKey(REPLACER) || b != S_getFillBlock()) && // Avoid dummy block.
            !(!facingMap.containsKey(FLUID_PUMP) && TilePump.isLiquid(b)); // Fluid when pump isn't connected.
    }

    private boolean addX = true;
    private boolean addZ = true;
    private boolean dug = true;
    private boolean changeZ = false;

    private void S_setNextTarget() {
        preX = targetX;
        preY = targetY;
        preZ = targetZ;
        if (this.now == MAKE_FRAME) {
            if (this.changeZ) {
                if (this.addZ) {
                    this.targetZ++;
                } else {
                    this.targetZ--;
                }
            } else if (this.addX) {
                this.targetX++;
            } else {
                this.targetX--;
            }
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
                if (this.dug)
                    this.dug = false;
                else
                    this.targetY--;
        } else {
            if (this.addX)
                this.targetX++;
            else
                this.targetX--;
            final int out = this.now == NOT_NEED_BREAK ? 0 : 1;
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
                    if (this.dug)
                        this.dug = false;
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
        return Math.sqrt(Math.pow(x - this.headPosX, 2) + Math.pow(y + 1 - this.headPosY, 2) + Math.pow(z - this.headPosZ, 2));
    }

    private boolean S_makeFrame() {
        this.dug = true;
        if (!PowerManager.useEnergyFrameBuild(this, this.unbreaking))
            return false;
        getWorld().setBlockState(new BlockPos(this.targetX, this.targetY, this.targetZ), QuarryPlusI.blockFrame().getDefaultState());
        S_setNextTarget();
        return true;
    }

    private boolean S_breakBlock() {
        this.dug = true;
        if (S_breakBlock(this.targetX, this.targetY, this.targetZ, S_getFillBlock())) {
            S_checkDropItem(new AxisAlignedBB(this.targetX - 4, this.targetY - 4, this.targetZ - 4,
                this.targetX + 5, this.targetY + 3, this.targetZ + 5));
            if (this.now == BREAK_BLOCK)
                setNow(MOVE_HEAD);
            S_setNextTarget();
            return true;
        }
        return false;
    }

    private void S_checkDropItem(AxisAlignedBB axis) {
        final List<EntityItem> result = getWorld().getEntitiesWithinAABB(EntityItem.class, axis);
        result.stream().filter(EntityItem::isEntityAlive).map(EntityItem::getItem).filter(VersionUtil::nonEmpty).forEach(this.cacheItems::add);
        result.forEach(QuarryPlus.proxy::removeEntity);

        if (facingMap.containsKey(EXP_PUMP)) {
            List<Entity> xpOrbs = world.getEntitiesWithinAABB(Entity.class, axis);
            modules.forEach(iModule -> iModule.invoke(new IModule.CollectingItem(JavaConverters.asScalaBufferConverter(xpOrbs).asScala().toList())));
        }

    }

    @SuppressWarnings("Duplicates") // To avoid BC's library error.
    private void S_createBox() {
        if (this.yMax != Integer.MIN_VALUE)
            return;

        EnumFacing facing = getWorld().getBlockState(getPos()).getValue(BlockQuarry.FACING).getOpposite();
        if (bcLoaded) {
            Optional<IAreaProvider> marker = Stream.of(getNeighbors(facing))
                .map(getWorld()::getTileEntity).filter(Objects::nonNull)
                .flatMap(t ->
                    Stream.concat(streamCast(IAreaProvider.class).apply(t), Stream.of(t.getCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, null)))
                ).filter(nonNull).findFirst();
            if (marker.isPresent()) {
                IAreaProvider provider = marker.get();
                if (provider.min().getX() == provider.max().getX() || provider.min().getZ() == provider.max().getZ()) {
                    setDefaultRange(getPos(), facing);
                } else {
                    this.xMin = provider.min().getX();
                    this.yMin = provider.min().getY();
                    this.zMin = provider.min().getZ();
                    this.xMax = provider.max().getX();
                    this.yMax = provider.max().getY();
                    this.zMax = provider.max().getZ();

                    if (getPos().getX() >= this.xMin && getPos().getX() <= this.xMax && getPos().getY() >= this.yMin
                        && getPos().getY() <= this.yMax && getPos().getZ() >= this.zMin && getPos().getZ() <= this.zMax) {
                        this.yMax = Integer.MIN_VALUE;
                        setDefaultRange(getPos(), facing);
                        return;
                    }
                    if (this.xMax - this.xMin < 2 || this.zMax - this.zMin < 2) {
                        this.yMax = Integer.MIN_VALUE;
                        setDefaultRange(getPos(), facing);
                        return;
                    }
                    if (this.yMax - this.yMin < 2)
                        this.yMax = this.yMin + 3;
                    areaProvider = provider;
                }
                return;
            }
        }
        Optional<IMarker> marker = Stream.of(getNeighbors(facing))
            .map(getWorld()::getTileEntity)
            .flatMap(streamCast(IMarker.class))
            .filter(IMarker::hasLink)
            .findFirst();
        if (marker.isPresent()) {
            IMarker iMarker = marker.get();
            this.xMin = iMarker.min().getX();
            this.yMin = iMarker.min().getY();
            this.zMin = iMarker.min().getZ();
            this.xMax = iMarker.max().getX();
            this.yMax = iMarker.max().getY();
            this.zMax = iMarker.max().getZ();
            if (getPos().getX() >= this.xMin && getPos().getX() <= this.xMax && getPos().getY() >= this.yMin
                && getPos().getY() <= this.yMax && getPos().getZ() >= this.zMin && getPos().getZ() <= this.zMax) {
                this.yMax = Integer.MIN_VALUE;
                setDefaultRange(getPos(), facing);
                return;
            }
            if (this.xMax - this.xMin < 2 || this.zMax - this.zMin < 2) {
                this.yMax = Integer.MIN_VALUE;
                setDefaultRange(getPos(), facing);
                return;
            }
            if (this.yMax - this.yMin < 2)
                this.yMax = this.yMin + 3;
            areaProvider = iMarker;
        } else {
            setDefaultRange(getPos(), facing);
        }
    }

    protected IBlockState S_getFillBlock() {
        if (now == NOT_NEED_BREAK || !facingMap.containsKey(REPLACER))
            return Blocks.AIR.getDefaultState();
        else {
            return Optional.ofNullable(facingMap.get(REPLACER))
                .map(pos::offset).map(world::getTileEntity)
                .flatMap(REPLACER)
                .map(TileReplacer::getReplaceState)
                .orElse(Blocks.AIR.getDefaultState());
        }
    }

    @SuppressWarnings("Duplicates")
    public void setDefaultRange(BlockPos pos, EnumFacing facing) {
        final int x = 11;
        final int y = (x - 1) / 2;//5
        pos = pos.offset(facing);
        BlockPos pos1 = pos.offset(facing);
        BlockPos pos2 = pos.offset(facing, x);
        BlockPos pos3 = pos.offset(facing.rotateY(), y);
        BlockPos pos4 = pos.offset(facing.rotateYCCW(), y);
        if (facing.getAxis() == EnumFacing.Axis.X) {
            xMin = Math.min(pos1.getX(), pos2.getX());
            xMax = Math.max(pos1.getX(), pos2.getX());
            zMin = Math.min(pos3.getZ(), pos4.getZ());
            zMax = Math.max(pos3.getZ(), pos4.getZ());
        } else if (facing.getAxis() == EnumFacing.Axis.Z) {
            xMin = Math.min(pos3.getX(), pos4.getX());
            xMax = Math.max(pos3.getX(), pos4.getX());
            zMin = Math.min(pos1.getZ(), pos2.getZ());
            zMax = Math.max(pos1.getZ(), pos2.getZ());
        }
        yMin = getPos().getY();
        yMax = getPos().getY() + 3;
    }

    private void S_setFirstPos() {
        initTargetPos();
        this.headPosX = (this.xMin + this.xMax + 1) >> 1;
        this.headPosZ = (this.zMin + this.zMax + 1) >> 1;
        this.headPosY = this.yMax - 1;
    }

    private void initTargetPos() {
        this.targetX = this.xMin;
        this.targetY = this.yMax;
        this.targetZ = this.zMin;
        this.preX = this.xMin;
        this.preY = this.yMax;
        this.preZ = this.zMin;
    }

    private boolean S_moveHead() {
        final double maxDistance = Math.sqrt((targetX - preX) * (targetX - preX) + (targetY - preY) * (targetY - preY) + (targetZ - preZ) * (targetZ - preZ))
            / ((double) scala.Int.unbox(Config.content().tickDelay().apply(getSymbol())) + 1);
        final double x = this.targetX - this.headPosX;
        final double y = this.targetY + 1 - this.headPosY;
        final double z = this.targetZ - this.headPosZ;
        final double distance = Math.sqrt(x * x + y * y + z * z);
        final double blocks = PowerManager.useEnergyQuarryHead(this, Math.min(distance, maxDistance * 2), this.unbreaking);
        if (blocks * 2 - distance > -1e-5) {
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
        if (!getWorld().isRemote) {
            PacketHandler.sendToAround(ModeMessage.create(this), getWorld(), getPos());
            IBlockState state = getWorld().getBlockState(getPos());
            if (state.getValue(BlockQuarry.ACTING) ^ isWorking()) {
                InvUtils.setNewState(getWorld(), getPos(), this, state.withProperty(BlockQuarry.ACTING, isWorking()));
                if (isWorking()) {
                    startWork();
                } else {
                    finishWork();
                }
            }
        }
    }

    public BlockPos getMinPos() {
        return new BlockPos(xMin, yMin, zMin);
    }

    public BlockPos getMaxPos() {
        return new BlockPos(xMax, yMax, zMax);
    }

    @Override
    protected void G_destroy() {
        setNow(NONE);
        G_renew_powerConfigure();
        ForgeChunkManager.releaseTicket(this.chunkTicket);
    }

    @Override
    public void G_ReInit() {
        if (this.yMax == Integer.MIN_VALUE && !getWorld().isRemote)
            S_createBox();
        setNow(Config.content().disableFillerMode() ? MAKE_FRAME : NOT_NEED_BREAK);
        this.coolFillerMode = scala.Int.unbox(Config.content().tickDelay().apply(getSymbol()));
        G_renew_powerConfigure();
        if (!getWorld().isRemote) {
            S_setFirstPos();
            PacketHandler.sendToAround(TileMessage.create(this), getWorld(), getPos());
        }
    }

    private Ticket chunkTicket;

    @Override
    public void requestTicket() {
        if (this.chunkTicket != null)
            return;
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, getWorld(), Type.NORMAL);
        setTileData(this.chunkTicket, getPos());
    }

    @Override
    public void forceChunkLoading(final Ticket ticket) {
        if (this.chunkTicket == null)
            this.chunkTicket = ticket;
        ForgeChunkManager.forceChunk(ticket, new ChunkPos(getPos()));
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
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.xMin = nbt.getInteger("xMin");
        this.xMax = nbt.getInteger("xMax");
        this.yMin = nbt.getInteger("yMin");
        this.zMin = nbt.getInteger("zMin");
        this.zMax = nbt.getInteger("zMax");
        this.yMax = nbt.getInteger("yMax");
        this.targetX = nbt.getInteger("targetX");
        this.targetY = nbt.getInteger("targetY");
        this.targetZ = nbt.getInteger("targetZ");
        this.preX = nbt.getInteger("preX");
        this.preY = nbt.getInteger("preY");
        this.preZ = nbt.getInteger("preZ");
        this.addZ = nbt.getBoolean("addZ");
        this.addX = nbt.getBoolean("addX");
        this.dug = nbt.getBoolean("dug");
        this.changeZ = nbt.getBoolean("changeZ");
        this.now = Mode.values()[nbt.getByte("now")];
        this.headPosX = nbt.getDouble("headPosX");
        this.headPosY = nbt.getDouble("headPosY");
        this.headPosZ = nbt.getDouble("headPosZ");
        this.filler = nbt.getBoolean("filler");
        this.initialized = false;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        nbt.setInteger("xMin", this.xMin);
        nbt.setInteger("xMax", this.xMax);
        nbt.setInteger("yMin", this.yMin);
        nbt.setInteger("yMax", this.yMax);
        nbt.setInteger("zMin", this.zMin);
        nbt.setInteger("zMax", this.zMax);
        nbt.setInteger("targetX", this.targetX);
        nbt.setInteger("targetY", this.targetY);
        nbt.setInteger("targetZ", this.targetZ);
        nbt.setInteger("preX", this.preX);
        nbt.setInteger("preY", this.preY);
        nbt.setInteger("preZ", this.preZ);
        nbt.setBoolean("addZ", this.addZ);
        nbt.setBoolean("addX", this.addX);
        nbt.setBoolean("dug", this.dug);
        nbt.setBoolean("changeZ", this.changeZ);
        nbt.setByte("now", ((byte) this.now.ordinal()));
        nbt.setDouble("headPosX", this.headPosX);
        nbt.setDouble("headPosY", this.headPosY);
        nbt.setDouble("headPosZ", this.headPosZ);
        nbt.setBoolean("filler", this.filler);
        return super.writeToNBT(nbt);
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.quarry;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName() {
        return getDebugName();
    }

    public double headPosX, headPosY, headPosZ;
    private boolean initialized = true;

    private Mode now = NONE;

    @Override
    public void G_renew_powerConfigure() {
        byte pmp = 0;
        if (hasWorld()) {
            Map<IAttachment.Attachments<?>, EnumFacing> map = facingMap.entrySet().stream()
                .filter(byEntry((attachments, facing) -> attachments.test(getWorld().getTileEntity(getPos().offset(facing)))))
                .collect(entryToMap());
            facingMap.putAll(map);
            pmp = Optional.ofNullable(facingMap.get(FLUID_PUMP))
                .map(getPos()::offset)
                .map(getWorld()::getTileEntity)
                .flatMap(FLUID_PUMP)
                .map(p -> p.unbreaking)
                .orElse((byte) 0);
        }
        if (this.now == NONE)
            PowerManager.configure0(this);
        else if (this.now == MAKE_FRAME)
            PowerManager.configureFrameBuild(this, this.efficiency, this.unbreaking, pmp);
        else
            PowerManager.configureQuarryWork(this, this.efficiency, this.unbreaking, pmp);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.yMax == Integer.MIN_VALUE)
            return new AxisAlignedBB(getPos().getX(), Double.NEGATIVE_INFINITY, getPos().getZ(),
                getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
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

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        if ((now == NOT_NEED_BREAK || now == MAKE_FRAME) && yMax != Integer.MIN_VALUE) {
            return (xMax - xMin) * (xMax - xMin) + 25 + (zMax - zMin) * (zMax - zMin);
        } else if (now == BREAK_BLOCK || now == MOVE_HEAD) {
            return (xMax - xMin) * (xMax - xMin) + yMax * yMax + (zMax - zMin) * (zMax - zMin);
        } else {
            return super.getMaxRenderDistanceSquared();
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public List<ITextComponent> getDebugMessages() {
        ArrayList<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation(TranslationKeys.CURRENT_MODE, G_getNow()));
        list.add(new TextComponentString(String.format("Next target : (%d, %d, %d)", targetX, targetY, targetZ)));
        list.add(new TextComponentString(String.format("Head Pos : (%s, %s, %s)", headPosX, headPosY, headPosZ)));
        list.add(new TextComponentString("X : " + xMin + " to " + xMax));
        list.add(new TextComponentString("Z : " + zMin + " to " + zMax));
        list.add(new TextComponentTranslation(filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE));
        list.add(new TextComponentTranslation(TranslationKeys.Y_LEVEL, this.yLevel));
        return list;
    }

    public enum Mode {
        NONE,
        NOT_NEED_BREAK,
        MAKE_FRAME,
        MOVE_HEAD,
        BREAK_BLOCK
    }
}
