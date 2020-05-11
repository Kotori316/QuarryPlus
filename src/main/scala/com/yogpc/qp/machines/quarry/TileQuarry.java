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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.Area;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.IChunkLoadTile;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.base.IMarker;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.machines.replacer.TileReplacer;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.packet.quarry.ModeMessage;
import com.yogpc.qp.packet.quarry.MoveHead;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.BooleanUtils;
import scala.Option;
import scala.Symbol;
import scala.jdk.javaapi.CollectionConverters;

import static com.yogpc.qp.machines.base.IAttachment.Attachments.EXP_PUMP;
import static com.yogpc.qp.machines.base.IAttachment.Attachments.FLUID_PUMP;
import static com.yogpc.qp.machines.base.IAttachment.Attachments.REPLACER;
import static jp.t2v.lab.syntax.MapStreamSyntax.byEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.not;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class TileQuarry extends TileBasic implements IDebugSender, IChunkLoadTile {
    public static final Symbol SYMBOL = Symbol.apply("QuarryPlus");
    private int targetX, targetY, targetZ;
    public int xMin, xMax, yMin, yMax = Integer.MIN_VALUE, zMin, zMax;
    public boolean filler;

    /**
     * The marker of {@code IAreaProvider} or {@link IMarker}.
     */
    @Nullable
    private Object areaProvider = null;

    public TileQuarry() {
        super(Holder.quarryTileType());
    }

    public TileQuarry(TileEntityType<?> type) {
        super(type);
    }

    @SuppressWarnings("fallthrough")
    protected void S_updateEntity() {
        if (this.areaProvider != null) {
            if (this.areaProvider instanceof IMarker)
                this.cacheItems.addAll(((IMarker) this.areaProvider).removeFromWorldWithItem());
//            else if (bcLoaded && areaProvider instanceof IAreaProvider) {
//                ((IAreaProvider) this.areaProvider).removeFromWorld();
//            }
            this.areaProvider = null;
        }
        Boolean faster = Config.common().fastQuarryHeadMove().get();
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
                    setNow(Mode.BREAK_BLOCK);
                    break;
                //$FALL-THROUGH$
                case NOT_NEED_BREAK:
                    broken = !filler;
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
        assert world != null;
        if (this.targetY > this.yMax)
            this.targetY = this.yMax;
        BlockPos target = new BlockPos(this.targetX, this.targetY, this.targetZ);
        final BlockState b = world.getBlockState(target);
        final float blockHardness = b.getBlockHardness(world, target);
        switch (this.now) {
            case BREAK_BLOCK:
            case MOVE_HEAD:
                if (this.targetY < yLevel) {
                    G_destroy();
                    S_checkDropItem(new AxisAlignedBB(xMin - 2, yLevel - 3, zMin - 2, xMax + 2, yLevel + 2, zMax + 2));
                    PacketHandler.sendToAround(ModeMessage.create(this), world, getPos());
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
                    setNow(Mode.MAKE_FRAME);
                    G_renew_powerConfigure();
                    this.targetX = this.xMin;
                    this.targetY = this.yMax;
                    this.targetZ = this.zMin;
                    this.addX = this.addZ = this.dug = true;
                    this.changeZ = false;
                    return S_checkTarget();
                }
                if (!isBreakableBlock(target, b, blockHardness))
                    return false;
                if (b.getBlock() == Holder.blockFrame() && !b.get(BlockFrame.DAMMING)) {
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
                    setNow(Mode.MOVE_HEAD);
                    G_renew_powerConfigure();
                    this.targetX = this.xMin + 1;
                    this.targetY = this.yMin;
                    this.targetZ = this.zMin + 1;
                    this.addX = this.addZ = this.dug = true;
                    this.changeZ = false;
                    return S_checkTarget();
                }
                if (b.getMaterial().isSolid()
                    && !(b.getBlock() == Holder.blockFrame() && !b.get(BlockFrame.DAMMING))) {
                    setNow(Mode.NOT_NEED_BREAK);
                    G_renew_powerConfigure();
                    this.targetX = this.xMin;
                    this.targetZ = this.zMin;
                    this.targetY = this.yMax;
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
                return flag > 1 && (b.getBlock() != Holder.blockFrame() || b.get(BlockFrame.DAMMING));
            case NONE:
                break;
        }
        return true;
    }

    private boolean isBreakableBlock(BlockPos target, BlockState b, float blockHardness) {
        return blockHardness >= 0 && // Not to break unbreakable
            !b.getBlock().isAir(b, world, target) && // Avoid air
            (now == Mode.NOT_NEED_BREAK || !facingMap.containsKey(REPLACER) || b != S_getFillBlock()) && // Avoid dummy block.
            !(TilePump.isLiquid(b) && !facingMap.containsKey(FLUID_PUMP)); // Fluid when pump isn't connected.
    }

    private boolean addX = true;
    private boolean addZ = true;
    private boolean dug = true;
    private boolean changeZ = false;

    private void S_setNextTarget() {
        if (this.now == Mode.MAKE_FRAME) {
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
            final int out = this.now == Mode.NOT_NEED_BREAK ? 0 : 1;
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
        assert world != null;
        this.dug = true;
        if (!PowerManager.useEnergyFrameBuild(this, this.unbreaking))
            return false;
        world.setBlockState(new BlockPos(this.targetX, this.targetY, this.targetZ), Holder.blockFrame().getDefaultState());
        S_setNextTarget();
        return true;
    }

    private boolean S_breakBlock() {
        this.dug = true;
        if (S_breakBlock(this.targetX, this.targetY, this.targetZ, S_getFillBlock())) {
            S_checkDropItem(new AxisAlignedBB(this.targetX - 4, this.targetY - 4, this.targetZ - 4,
                this.targetX + 5, this.targetY + 3, this.targetZ + 5));
            if (this.now == Mode.BREAK_BLOCK)
                setNow(Mode.MOVE_HEAD);
            S_setNextTarget();
            return true;
        }
        return false;
    }

    private void S_checkDropItem(AxisAlignedBB axis) {
        assert world != null;
        final List<ItemEntity> result = world.getEntitiesWithinAABB(ItemEntity.class, axis);
        result.stream().filter(ItemEntity::isAlive).map(ItemEntity::getItem).filter(not(ItemStack::isEmpty)).forEach(this.cacheItems::add);
        result.forEach(QuarryPlus.proxy::removeEntity);

        if (facingMap.containsKey(EXP_PUMP)) {
            List<Entity> xpOrbs = world.getEntitiesWithinAABB(Entity.class, axis);
            modules.forEach(iModule -> iModule.invoke(new IModule.CollectingItem(CollectionConverters.asScala(xpOrbs).toList())));
        }

    }

    @SuppressWarnings("Duplicates") // To avoid BC's library error.
    private void S_createBox() {
        assert world != null;
        if (this.yMax != Integer.MIN_VALUE)
            return;

        Direction facing = world.getBlockState(getPos()).get(FACING);
        /*if (bcLoaded) {
            Optional<IAreaProvider> marker = Stream.of(getNeighbors(facing))
                .map(world::getTileEntity).filter(Objects::nonNull)
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
        }*/
        Option<IMarker> marker = Area.findQuarryArea(facing, world, pos)._2;
        if (marker.isDefined()) {
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
                setDefaultRange(getPos(), facing.getOpposite());
                return;
            }
            if (this.xMax - this.xMin < 2 || this.zMax - this.zMin < 2) {
                this.yMax = Integer.MIN_VALUE;
                setDefaultRange(getPos(), facing.getOpposite());
                return;
            }
            if (this.yMax - this.yMin < 2)
                this.yMax = this.yMin + 3;
            areaProvider = iMarker;
        } else {
            setDefaultRange(getPos(), facing.getOpposite());
        }
    }

    protected BlockState S_getFillBlock() {
        assert world != null;
        if (now == Mode.NOT_NEED_BREAK || !facingMap.containsKey(REPLACER))
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
    public void setDefaultRange(BlockPos pos, Direction facing) {
        final int x = 11;
        final int y = (x - 1) / 2;//5
        pos = pos.offset(facing);
        BlockPos pos1 = pos.offset(facing);
        BlockPos pos2 = pos.offset(facing, x);
        BlockPos pos3 = pos.offset(facing.rotateY(), y);
        BlockPos pos4 = pos.offset(facing.rotateYCCW(), y);
        if (facing.getAxis() == Direction.Axis.X) {
            xMin = Math.min(pos1.getX(), pos2.getX());
            xMax = Math.max(pos1.getX(), pos2.getX());
            zMin = Math.min(pos3.getZ(), pos4.getZ());
            zMax = Math.max(pos3.getZ(), pos4.getZ());
        } else if (facing.getAxis() == Direction.Axis.Z) {
            xMin = Math.min(pos3.getX(), pos4.getX());
            xMax = Math.max(pos3.getX(), pos4.getX());
            zMin = Math.min(pos1.getZ(), pos2.getZ());
            zMax = Math.max(pos1.getZ(), pos2.getZ());
        }
        yMin = getPos().getY();
        yMax = getPos().getY() + 3;
    }

    private void S_setFirstPos() {
        this.targetX = this.xMin;
        this.targetZ = this.zMin;
        this.targetY = this.yMax;
        this.headPosX = (this.xMin + this.xMax + 1) >> 1;
        this.headPosZ = (this.zMin + this.zMax + 1) >> 1;
        this.headPosY = this.yMax - 1;
    }

    private boolean S_moveHead() {
        final double x = this.targetX - this.headPosX;
        final double y = this.targetY + 1 - this.headPosY;
        final double z = this.targetZ - this.headPosZ;
        final double distance = Math.sqrt(x * x + y * y + z * z);
        final double blocks = PowerManager.useEnergyQuarryHead(this, distance, this.unbreaking);
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
        if (world != null && !world.isRemote) {
            PacketHandler.sendToAround(ModeMessage.create(this), world, getPos());
            BlockState state = world.getBlockState(getPos());
            if (state.get(QPBlock.WORKING()) ^ isWorking()) {
//                InvUtils.setNewState(world, getPos(), this, state.with(QPBlock.WORKING(), isWorking()));
                world.setBlockState(getPos(), state.with(QPBlock.WORKING(), isWorking()));
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
        setNow(Mode.NONE);
        G_renew_powerConfigure();
    }

    @Override
    public void remove() {
        IChunkLoadTile.super.releaseTicket();
        super.remove();
    }

    @Override
    public void G_ReInit() {
        if (this.yMax == Integer.MIN_VALUE && world != null && !world.isRemote)
            S_createBox();
        setNow(Mode.NOT_NEED_BREAK);
        G_renew_powerConfigure();
        if (world != null && !world.isRemote) {
            S_setFirstPos();
            PacketHandler.sendToAround(TileMessage.create(this), world, getPos());
        }
    }

    @Override
    public void workInTick() {
        if (!this.initialized) {
            G_renew_powerConfigure();
            this.initialized = true;
        }
        S_updateEntity();
    }

    @Override
    protected boolean isWorking() {
        return now != Mode.NONE;
    }

    @Override
    public void read(final CompoundNBT nbt) {
        super.read(nbt);
        this.xMin = nbt.getInt("xMin");
        this.xMax = nbt.getInt("xMax");
        this.yMin = nbt.getInt("yMin");
        this.zMin = nbt.getInt("zMin");
        this.zMax = nbt.getInt("zMax");
        this.yMax = nbt.getInt("yMax");
        this.targetX = nbt.getInt("targetX");
        this.targetY = nbt.getInt("targetY");
        this.targetZ = nbt.getInt("targetZ");
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
    public CompoundNBT write(final CompoundNBT nbt) {
        nbt.putInt("xMin", this.xMin);
        nbt.putInt("xMax", this.xMax);
        nbt.putInt("yMin", this.yMin);
        nbt.putInt("yMax", this.yMax);
        nbt.putInt("zMin", this.zMin);
        nbt.putInt("zMax", this.zMax);
        nbt.putInt("targetX", this.targetX);
        nbt.putInt("targetY", this.targetY);
        nbt.putInt("targetZ", this.targetZ);
        nbt.putBoolean("addZ", this.addZ);
        nbt.putBoolean("addX", this.addX);
        nbt.putBoolean("dug", this.dug);
        nbt.putBoolean("changeZ", this.changeZ);
        nbt.putByte("now", ((byte) this.now.ordinal()));
        nbt.putDouble("headPosX", this.headPosX);
        nbt.putDouble("headPosY", this.headPosY);
        nbt.putDouble("headPosZ", this.headPosZ);
        nbt.putBoolean("filler", this.filler);
        return super.write(nbt);
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.quarry;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public TranslationTextComponent getName() {
        return new TranslationTextComponent(getDebugName());
    }


    public double headPosX, headPosY, headPosZ;
    private boolean initialized = true;

    private Mode now = Mode.NONE;

    @Override
    public void G_renew_powerConfigure() {
        assert world != null;
        byte pmp = 0;
        if (hasWorld()) {
            Map<IAttachment.Attachments<?>, Direction> map = facingMap.entrySet().stream()
                .filter(byEntry((attachments, facing) -> attachments.test(world.getTileEntity(getPos().offset(facing)))))
                .collect(entryToMap());
            facingMap.putAll(map);
            pmp = Optional.ofNullable(facingMap.get(FLUID_PUMP))
                .map(getPos()::offset)
                .map(world::getTileEntity)
                .flatMap(FLUID_PUMP)
                .map(p -> p.unbreaking)
                .orElse((byte) 0);
        }
        if (this.now == Mode.NONE)
            PowerManager.configure0(this);
        else if (this.now == Mode.MAKE_FRAME)
            PowerManager.configureFrameBuild(this, this.efficiency, this.unbreaking, pmp);
        else
            PowerManager.configureQuarryWork(this, this.efficiency, this.unbreaking, pmp);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        if ((now == Mode.NOT_NEED_BREAK || now == Mode.MAKE_FRAME) && yMax != Integer.MIN_VALUE) {
            return (xMax - xMin) * (xMax - xMin) + 25 + (zMax - zMin) * (zMax - zMin);
        } else if (now == Mode.BREAK_BLOCK || now == Mode.MOVE_HEAD) {
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
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TranslationTextComponent(TranslationKeys.CURRENT_MODE, G_getNow()));
        list.add(new StringTextComponent(String.format("Next target : (%d, %d, %d)", targetX, targetY, targetZ)));
        list.add(new StringTextComponent(String.format("Head Pos : (%s, %s, %s)", headPosX, headPosY, headPosZ)));
        list.add(new StringTextComponent("X : " + xMin + " to " + xMax));
        list.add(new StringTextComponent("Z : " + zMin + " to " + zMax));
        list.add(new TranslationTextComponent(filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE));
        list.add(new TranslationTextComponent(TranslationKeys.Y_LEVEL, this.yLevel));
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
