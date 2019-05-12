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

package com.yogpc.qp.machines.pump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.FluidStore;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.IAttachable;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.base.IDummyFluidHandler;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.packet.pump.Now;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowingFluid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Fluids;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import scala.Symbol;

public class TilePump extends APacketTile implements IEnchantableTile, ITickable, IDebugSender, IAttachment {
    @Nullable
    public EnumFacing connectTo = null;
    private boolean initialized = false;

    private EnumFacing preFacing;

    public byte unbreaking;
    protected byte fortune;
    protected boolean silktouch;
    private final List<FluidStack> liquids = new ArrayList<>();
    public final EnumMap<EnumFacing, LinkedList<String>> mapping = new EnumMap<>(EnumFacing.class);
//    public final EnumMap<EnumFacing, PumpTank> tankMap = new EnumMap<>(EnumFacing.class);

    public TilePump() {
        super(Holder.pumpTileType());
        for (EnumFacing value : EnumFacing.values()) {
//            tankMap.put(value, new PumpTank(value));
            mapping.put(value, new LinkedList<>());
        }
    }

    public IAttachable G_connected() {
        if (connectTo != null) {
            final TileEntity te = world.getTileEntity(getPos().offset(connectTo));
            if (te instanceof IAttachable)
                return (IAttachable) te;
            else {
                setConnectTo(null);
                if (!world.isRemote)
                    S_sendNowPacket();
                return null;
            }
        }
        return null;
    }

    public boolean G_working() {
        return this.py >= this.cy;
    }

    @Override
    public void read(final NBTTagCompound nbt) {
        super.read(nbt);
        this.silktouch = nbt.getBoolean("silktouch");
        this.fortune = nbt.getByte("fortune");
        this.unbreaking = nbt.getByte("unbreaking");
        if (nbt.hasKey("connectTo")) {
            setConnectTo(EnumFacing.byIndex(nbt.getByte("connectTo")));
            preFacing = this.connectTo;
        }
        if (nbt.getTag("mapping0") instanceof NBTTagList)
            for (int i = 0; i < this.mapping.size(); i++) {
                LinkedList<String> list = this.mapping.get(EnumFacing.byIndex(i));
                list.clear();
                list.addAll(nbt.getList("mapping" + i, Constants.NBT.TAG_STRING).stream().map(INBTBase::getString).collect(Collectors.toList()));
            }
        this.range = nbt.getByte("range");
        this.quarryRange = nbt.getBoolean("quarryRange");
        this.autoChangedRange = nbt.getBoolean("autoChangedRange");
        if (this.silktouch) {
            this.liquids.clear();
            final NBTTagList liquids = nbt.getList("liquids", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < liquids.size(); i++)
                this.liquids.add(FluidStack.loadFluidStackFromNBT(liquids.getCompound(i)));
        }
    }

    @Override
    public NBTTagCompound write(final NBTTagCompound nbt) {
        nbt.setBoolean("silktouch", this.silktouch);
        nbt.setByte("fortune", this.fortune);
        nbt.setByte("unbreaking", this.unbreaking);
        if (connectTo != null)
            nbt.setByte("connectTo", (byte) this.connectTo.ordinal());
        for (int i = 0; i < this.mapping.size(); i++)
            nbt.setTag("mapping" + i, this.mapping.get(EnumFacing.byIndex(i)).stream().map(NBTTagString::new).collect(Collectors.toCollection(NBTTagList::new)));
        nbt.setByte("range", this.range);
        nbt.setBoolean("quarryRange", this.quarryRange);
        nbt.setBoolean("autoChangedRange", this.autoChangedRange);
        if (this.silktouch) {
            nbt.setTag("liquids",
                this.liquids.stream().map(f -> f.writeToNBT(new NBTTagCompound())).collect(Collectors.toCollection(NBTTagList::new)));
        }
        return super.write(nbt);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            /*for (EnumFacing facing : EnumFacing.values()) {
                BlockPos offset = getPos().offset(facing);
                IBlockState state = world.getBlockState(offset);
                if (state.getBlock().hasTileEntity(state)) {
                    Optional.ofNullable(world.getTileEntity(offset))
                        .map(t -> t.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()))
                        .ifPresent(handlerOpt -> {
                            IFluidHandler handler = handlerOpt.orElse(EmptyFluidHandler.INSTANCE);
                            PumpTank tank = tankMap.get(facing);
                            FluidStack resource = tank.drain(Fluid.BUCKET_VOLUME, false);
                            if (resource != null) {
                                int fill = handler.fill(resource, false);
                                if (fill > 0) {
                                    handler.fill(tank.drain(fill, true), true);
                                }
                            }
                        });
                }
            }*/
            if (!initialized) {
                if (connectTo != null) {
                    TileEntity te = world.getTileEntity(getPos().offset(connectTo));
                    if (te instanceof IAttachable && ((IAttachable) te).connect(this.connectTo.getOpposite(), Attachments.FLUID_PUMP)) {
                        ((IAttachable) te).connectAttachment(this.connectTo.getOpposite(), Attachments.FLUID_PUMP, false);
                        S_sendNowPacket();
                        this.initialized = true;
                    } else if (world.isAirBlock(getPos().offset(connectTo))) {
                        setConnectTo(null);
                        S_sendNowPacket();
                        this.initialized = true;
                    }
                }
            }
        }
    }

    @Override
    public void G_ReInit() {
        if (!world.isRemote) {
            TileEntity te;
            for (EnumFacing facing : EnumFacing.values()) {
                te = world.getTileEntity(getPos().offset(facing));
                if (te instanceof IAttachable && ((IAttachable) te).connect(facing.getOpposite(), Attachments.FLUID_PUMP)) {
                    ((IAttachable) te).connectAttachment(facing.getOpposite(), Attachments.FLUID_PUMP, false);
                    setConnectTo(facing);
                    S_sendNowPacket();
                    return;
                }
            }
            setConnectTo(null);
            S_sendNowPacket();
        }
    }

    private void S_sendNowPacket() {
        //when connection changed or working changed
        if (preFacing != connectTo || world.getBlockState(getPos()).get(QPBlock.WORKING()) != G_working()) {
            preFacing = connectTo;
            PacketHandler.sendToAround(Now.create(this), world, getPos());
        }
    }

    @Override
    public void setConnectTo(@Nullable EnumFacing connectTo) {
        this.connectTo = connectTo;
        if (hasWorld()) {
            IBlockState state = world.getBlockState(getPos());
            if (connectTo != null ^ state.get(BlockStateProperties.ENABLED)) {
                InvUtils.setNewState(world, getPos(), this, state.with(BlockStateProperties.ENABLED, connectTo != null));
            }
        }
    }

    public void setWorking(boolean b) {
        if (b) {
            this.cy = this.py = -1;
        } else {
            this.py = Integer.MIN_VALUE;
        }
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(getPos());
            InvUtils.setNewState(world, getPos(), this, state.with(QPBlock.WORKING(), b));
        }
    }

    public void S_OpenGUI(EnumFacing facing, final EntityPlayer ep) {
        PacketHandler.sendToClient(Mappings.All.create(this, facing), world);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int Y_SIZE = 256;
    private static final int CHUNK_SCALE = 16;

    private byte[][][] blocks;
    private ChunkSection[][][] storageArray;
    private int xOffset, yOffset, zOffset, px, py = Integer.MIN_VALUE;
    private int cx, cy = -1, cz;
    private byte range = 0;
    private boolean quarryRange = true;
    private boolean autoChangedRange = false;

    private int block_side_x, block_side_z;

    private static final int ARRAY_MAX = 0x80000;
    private static final int[] xb = new int[ARRAY_MAX];
    private static final int[] yb = new int[ARRAY_MAX];
    private static final int[] zb = new int[ARRAY_MAX];
    private static int cp = 0;
    private long fwt;

    public void S_changeRange(final EntityPlayer ep) {
        if (this.range >= (this.fortune + 1) * 2) {
            if (G_connected() instanceof TileQuarry)
                this.quarryRange = true;
            this.range = 0;
        } else if (this.quarryRange)
            this.quarryRange = false;
        else
            this.range++;
        if (this.quarryRange)
            ep.sendStatusMessage(new TextComponentTranslation(TranslationKeys.PUMP_RTOGGLE_QUARRY), false);
        else
            ep.sendStatusMessage(new TextComponentTranslation(TranslationKeys.PUMP_RTOGGLE_NUM, Integer.toString(this.range * 2 + 1)), false);
        this.fwt = 0;
    }

    private static void S_put(final int x, final int y, final int z) {
        xb[cp] = x;
        yb[cp] = y;
        zb[cp] = z;
        cp++;
        if (cp == ARRAY_MAX)
            cp = 0;
    }

    @SuppressWarnings("ConditionalCanBeOptional")
    private void S_searchLiquid(final int x, final int y, final int z) {
        this.fwt = world.getDayTime();
        int cg;
        cp = cg = 0;
        int chunk_side_x, chunk_side_z;
        this.cx = x;
        this.cy = y;
        this.cz = z;
        this.yOffset = y & 0xFFFFFFF0;
        this.py = Y_SIZE - 1;
        this.px = -1;
        final IAttachable tb = G_connected();
        @Nullable TileQuarry b = null;
        if (tb instanceof TileQuarry)
            b = (TileQuarry) tb;
        if (b != null && b.yMax != Integer.MIN_VALUE) {
            chunk_side_x = 1 + (b.xMax >> 4) - (b.xMin >> 4);
            chunk_side_z = 1 + (b.zMax >> 4) - (b.zMin >> 4);
            this.xOffset = b.xMin & 0xFFFFFFF0;
            this.zOffset = b.zMin & 0xFFFFFFF0;
            final int x_add = this.range * 2 + 1 - chunk_side_x;
            if (x_add > 0) {
                chunk_side_x += x_add;
                this.xOffset -=
                    ((x_add & 0xFFFFFFFE) << 3)
                        + (x_add % 2 != 0 && (b.xMax + b.xMin + 1) / 2 % 0x10 <= 8 ? 0x10 : 0);
            }
            final int z_add = this.range * 2 + 1 - chunk_side_z;
            if (z_add > 0) {
                chunk_side_z += z_add;
                this.zOffset -=
                    ((z_add & 0xFFFFFFFE) << 3)
                        + (z_add % 2 != 0 && (b.zMax + b.zMin + 1) / 2 % 0x10 <= 8 ? 0x10 : 0);
            }
        } else {
            this.quarryRange = false;
            chunk_side_x = chunk_side_z = 1 + this.range * 2;
            this.xOffset = (x >> 4) - this.range << 4;
            this.zOffset = (z >> 4) - this.range << 4;

        }
        if (!this.quarryRange)
            b = null;
        this.block_side_x = chunk_side_x * CHUNK_SCALE;
        this.block_side_z = chunk_side_z * CHUNK_SCALE;
        this.blocks = new byte[Y_SIZE - this.yOffset][this.block_side_x][this.block_side_z];
        this.storageArray = new ChunkSection[chunk_side_x][chunk_side_z][];
        int kx, kz;
        for (kx = 0; kx < chunk_side_x; kx++)
            for (kz = 0; kz < chunk_side_z; kz++)
                this.storageArray[kx][kz] = world.getChunkProvider()
                    .provideChunk(kx + (this.xOffset >> 4), kz + (this.zOffset >> 4), true, false)
                    .getSections();
        S_put(x - this.xOffset, y, z - this.zOffset);
        IBlockState b_c;
        ChunkSection ebs_c;
        while (cp != cg) {
            ebs_c = this.storageArray[xb[cg] >> 4][zb[cg] >> 4][yb[cg] >> 4];
            if (ebs_c != null) {
                b_c = ebs_c.get(xb[cg] & 0xF, yb[cg] & 0xF, zb[cg] & 0xF);
                if (this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] == 0 && isLiquid(b_c)) {
                    this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x3F;

                    if ((b != null ? b.xMin & 0xF : 0) < xb[cg])
                        S_put(xb[cg] - 1, yb[cg], zb[cg]);
                    else
                        this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

                    if (xb[cg] < (b != null ? b.xMax - this.xOffset : this.block_side_x - 1))
                        S_put(xb[cg] + 1, yb[cg], zb[cg]);
                    else
                        this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

                    if ((b != null ? b.zMin & 0xF : 0) < zb[cg])
                        S_put(xb[cg], yb[cg], zb[cg] - 1);
                    else
                        this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

                    if (zb[cg] < (b != null ? b.zMax - this.zOffset : this.block_side_z - 1))
                        S_put(xb[cg], yb[cg], zb[cg] + 1);
                    else
                        this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

                    if (yb[cg] + 1 < Y_SIZE)
                        S_put(xb[cg], yb[cg] + 1, zb[cg]);
                }
            }
            cg++;
            if (cg == ARRAY_MAX)
                cg = 0;
        }
    }

    public boolean S_removeLiquids(final APowerTile tile, final int x, final int y, final int z) {
        S_sendNowPacket();
        if (this.cx != x || this.cy != y || this.cz != z || this.py < this.cy
            || world.getDayTime() - this.fwt > 200)
            S_searchLiquid(x, y, z);
        else {
            this.storageArray = new ChunkSection[this.storageArray.length][this.storageArray[0].length][];
            for (int kx = 0; kx < this.storageArray.length; kx++) {
                for (int kz = 0; kz < this.storageArray[0].length; kz++) {
                    this.storageArray[kx][kz] = world.getChunkProvider()
                        .provideChunk(kx + (this.xOffset >> 4), kz + (this.zOffset >> 4), true, false)
                        .getSections();
                }
            }
        }

        int count = 0;
        IBlockState bb;
        int bz;
        do {
            do {
                if (this.px == -1) {
                    int bx;
                    for (bx = 0; bx < this.block_side_x; bx++)
                        for (bz = 0; bz < this.block_side_z; bz++)
                            if ((this.blocks[this.py - this.yOffset][bx][bz] & 0x40) != 0) {
                                bb = this.storageArray[bx >> 4][bz >> 4][this.py >> 4].get(bx & 0xF, this.py & 0xF, bz & 0xF);
                                if (isLiquid(bb))
                                    count++;
                            }
                } else {
                    BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                    for (bz = 0; bz < this.block_side_z; bz++)
                        if (this.blocks[this.py - this.yOffset][this.px][bz] != 0) {
                            bb = this.storageArray[this.px >> 4][bz >> 4][this.py >> 4].get(this.px & 0xF, this.py & 0xF, bz & 0xF);
                            mutableBlockPos.setPos(this.px + this.xOffset, this.py, bz + this.zOffset);
                            if (isLiquid(bb, Config.common().removeOnlySource().get(), world, mutableBlockPos))
                                count++;
                        }
                }
                if (count > 0)
                    break;
            } while (++this.px < this.block_side_x);
            if (count > 0)
                break;
            this.px = -1;

        } while (--this.py >= this.cy);
        if (count > 0 && PowerManager.useEnergyPump(tile, this.unbreaking, count, this.px == -1 ? count : 0))
            if (this.px == -1) {
                int bx;
                for (bx = 0; bx < this.block_side_x; bx++)
                    for (bz = 0; bz < this.block_side_z; bz++)
                        if ((this.blocks[this.py - this.yOffset][bx][bz] & 0x40) != 0) {
                            drainBlock(bx, bz, Holder.blockFrame().getDammingState());
                            if (tile instanceof TileQuarry) {
                                TileQuarry quarry = (TileQuarry) tile;
                                int xTarget = bx + xOffset;
                                int zTarget = bz + zOffset;
                                if (quarry.G_getNow() != TileQuarry.Mode.NOT_NEED_BREAK) {
                                    if (Config.common().debug()) {
                                        if ((quarry.xMin < xTarget && xTarget < quarry.xMax) && (quarry.zMin < zTarget && zTarget < quarry.zMax))
                                            QuarryPlus.LOGGER.warn(String.format("Quarry placed frame at %d, %d, %d", xTarget, py, zTarget));
                                    }
                                    autoChange(false);
                                } else {
                                    if ((quarry.xMin <= xTarget && xTarget <= quarry.xMax) && (quarry.zMin <= zTarget && zTarget <= quarry.zMax)) {
                                        if (Config.common().debug())
                                            QuarryPlus.LOGGER.warn(String.format("Quarry placed frame at %d, %d, %d", xTarget, py, zTarget));
                                        autoChange(true);
                                    }
                                }
                            }
                        }
            } else
                for (bz = 0; bz < this.block_side_z; bz++)
                    if (this.blocks[this.py - this.yOffset][this.px][bz] != 0)
                        drainBlock(this.px, bz, Blocks.AIR.getDefaultState());
        S_sendNowPacket();
        return this.py < this.cy;
    }

    private void autoChange(boolean on) {
        if (on) {
            this.autoChangedRange = true;
            this.quarryRange = false;
        } else if (this.autoChangedRange) {
            this.autoChangedRange = false;
            this.quarryRange = true;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param state      BlockState
     * @param findSource if true, return whether you can drain the liquid.
     * @param world      When source is false, it can be null.
     * @param pos        When source is false, it can be any value.
     * @return true if the blockstate is liquid state.
     */
    public static boolean isLiquid(@Nonnull final IBlockState state, final boolean findSource, final World world, final BlockPos pos) {
        Block block = state.getBlock();
        if (state.getFluidState() != Fluids.EMPTY.getDefaultState()) return true;
        if (block instanceof IFluidBlock)
            return !findSource || ((IFluidBlock) block).canDrain(world, pos);
        else {
            return (block == Blocks.WATER || block == Blocks.LAVA || state.getMaterial().isLiquid())
                && (!findSource || state.get(BlockFlowingFluid.LEVEL) == 0);
        }
    }

    public static boolean isLiquid(@Nonnull IBlockState state) {
        return isLiquid(state, false, null, null);
    }

    @Override
    protected Symbol getSymbol() {
        return Symbol.apply("PumpPlus");
    }

    /*private class PumpTank extends FluidTank {
        final EnumFacing facing;

        private PumpTank(EnumFacing facing) {
            super(Integer.MAX_VALUE);
            this.facing = facing;
            setCanFill(false);
            setTileEntity(TilePump.this);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }
            final int index = liquids.indexOf(resource);
            if (index == -1)
                return null;
            final FluidStack fs = liquids.get(index);
            if (fs == null)
                return null;

            int drained = Math.min(fs.amount, resource.amount);
            final FluidStack ret = new FluidStack(fs, drained);
            if (doDrain) {
                doDrain(() -> liquids.remove(fs), fs, ret.amount);
            }
            return ret;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            final LinkedList<FluidTankProperties> ret = new LinkedList<>();
            if (mapping.get(facing).isEmpty()) {
                if (liquids.isEmpty())
                    return IDummyFluidHandler.emptyPropertyArray;
                else
                    for (final FluidStack fs : liquids)
                        ret.add(new FluidTankProperties(fs, Integer.MAX_VALUE, false, true));
            } else {
                for (final String s : mapping.get(facing)) {
                    Optional.ofNullable(getFluidStack(s, 0)).ifPresent(fluidStack -> {
                        int index = liquids.indexOf(fluidStack);
                        if (index != -1)
                            ret.add(new FluidTankProperties(liquids.get(index), Integer.MAX_VALUE, false, true));
                        else
                            ret.add(new FluidTankProperties(fluidStack, Integer.MAX_VALUE, false, true));
                    });
                }
            }
            return ret.toArray(new FluidTankProperties[0]);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (mapping.get(facing).isEmpty()) {
                return liquids.isEmpty() ? null : drainI(0, maxDrain, doDrain);
            }
            int index;
            FluidStack fs;
            for (final String s : mapping.get(facing)) {
                fs = getFluidStack(s, maxDrain);
                if (fs != null) {
                    index = liquids.indexOf(fs);
                    if (index != -1) {
                        return drainI(index, maxDrain, doDrain);
                    }
                }
            }
            return null;
        }

        //TODO change to better code.
        private FluidStack getFluidStack(String name, int amount) {
            return null;
        }

        private FluidStack drainI(int index, int maxDrain, boolean doDrain) {
            FluidStack stack = liquids.get(index);
            int drained = Math.min(maxDrain, stack.amount);
            FluidStack ret = new FluidStack(stack, drained);
            if (doDrain) {
                doDrain(() -> liquids.remove(index), stack, drained);
            }
            return ret;
        }

        private void doDrain(Runnable remove, FluidStack stack, int drained) {
            stack.amount -= drained;
            if (stack.amount <= 0) {
                remove.run();
            }
            onContentsChanged();
            FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(stack.amount <= 0 ? null : stack, world, getPos(), this, drained));
        }

    }*/

    private void drainBlock(final int bx, final int bz, final IBlockState tb) {
        if (isLiquid(this.storageArray[bx >> 4][bz >> 4][this.py >> 4].get(bx & 0xF, this.py & 0xF, bz & 0xF))) {
            BlockPos blockPos = new BlockPos(bx + xOffset, py, bz + zOffset);
            /*FluidUtil.getFluidHandler(world, blockPos, EnumFacing.UP).ifPresent(handler -> {
                FluidStack stack = handler.drain(Fluid.BUCKET_VOLUME, true);
                if (stack != null) {
                    final int index = this.liquids.indexOf(stack);
                    if (index != -1)
                        this.liquids.get(index).amount += stack.amount;
                    else
                        this.liquids.add(stack);
                }
            });*/
            IFluidState fluidState = world.getFluidState(blockPos);
            if (fluidState.isSource())
                FluidStore.injectToNearTile(world, pos, fluidState.getFluid());
            world.setBlockState(blockPos, tb);
        }
    }

    public List<ITextComponent> C_getNames() {
        if (!liquids.isEmpty()) {
            List<ITextComponent> list = new ArrayList<>(liquids.size() + 1);
            list.add(new TextComponentTranslation(TranslationKeys.PUMP_CONTAIN));
            liquids.forEach(s -> list.add(new TextComponentTranslation(TranslationKeys.LIQUID_FORMAT,
                new TextComponentTranslation(s.getUnlocalizedName()), Integer.toString(s.amount))));
            return list;
        } else {
            return Collections.singletonList(new TextComponentTranslation(TranslationKeys.PUMP_CONTAIN_NO));
        }
    }

    @Override
    public List<ITextComponent> getDebugMessages() {
        ArrayList<ITextComponent> list = new ArrayList<>();
        list.add(toComponentString.apply("Connection : " + this.connectTo));
        for (EnumFacing facing : EnumFacing.values()) {
            this.mapping.get(facing).stream()
                .reduce(combiner).map(toComponentString)
                .ifPresent(list::add);
        }
        if (!liquids.isEmpty()) {
            list.add(new TextComponentTranslation(TranslationKeys.PUMP_CONTAIN));
            liquids.stream().map(fluidStack -> fluidStack.getLocalizedName() + fluidStack.amount + "mB")
                .reduce(combiner).map(toComponentString)
                .ifPresent(list::add);
        } else {
            list.add(new TextComponentTranslation(TranslationKeys.PUMP_CONTAIN_NO));
        }
        return list;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.pump;
    }

    @Nonnull
    @Override
    public Map<ResourceLocation, Integer> getEnchantments() {
        Map<ResourceLocation, Integer> ret = new HashMap<>();
        if (this.fortune > 0)
            ret.put(FortuneID, (int) this.fortune);
        if (this.unbreaking > 0)
            ret.put(UnbreakingID, (int) this.unbreaking);
        if (this.silktouch)
            ret.put(SilktouchID, 1);
        return ret;
    }

    @Override
    public void setEnchantment(ResourceLocation id, short val) {
        if (id.equals(FortuneID))
            this.fortune = (byte) val;
        else if (id.equals(UnbreakingID))
            this.unbreaking = (byte) val;
        else if (id.equals(SilktouchID))
            this.silktouch = val > 0;

    }

    private final IFluidHandler tankAll = (IDummyFluidHandler) () -> {
        IFluidTankProperties[] array = TilePump.this.liquids.stream()
            .map(fluidStack -> new FluidTankProperties(fluidStack, fluidStack.amount, false, false))
            .toArray(IFluidTankProperties[]::new);
        return array.length == 0 ? IDummyFluidHandler.emptyPropertyArray : array;
    };

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
//        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
//            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> side == null ? tankAll : tankMap.get(side)));
//        }
        return super.getCapability(cap, side);
    }
}
