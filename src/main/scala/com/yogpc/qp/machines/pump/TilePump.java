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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IAttachable;
import com.yogpc.qp.machines.base.IAttachment;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.pump.Mappings;
import com.yogpc.qp.packet.pump.Now;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import scala.Symbol;

public class TilePump extends APacketTile implements IEnchantableTile, ITickableTileEntity, IDebugSender, IAttachment, HasStorage {
    public static final Symbol SYMBOL = Symbol.apply("PumpPlus");
    @Nullable
    public Direction connectTo = null;
    private boolean initialized = false;

    private Direction preFacing;

    public byte unbreaking;
    protected byte fortune;
    protected boolean silktouch;

    private final TankPump tankPump = new TankPump();

    public TilePump() {
        super(Holder.pumpTileType());
    }

    public IAttachable G_connected() {
        if (world != null && connectTo != null) {
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
    public void read(final CompoundNBT nbt) {
        super.read(nbt);
        this.silktouch = nbt.getBoolean("silktouch");
        this.fortune = nbt.getByte("fortune");
        this.unbreaking = nbt.getByte("unbreaking");
        if (nbt.contains("connectTo")) {
            setConnectTo(Direction.byIndex(nbt.getByte("connectTo")));
            preFacing = this.connectTo;
        }

        this.range = nbt.getByte("range");
        this.quarryRange = nbt.getBoolean("quarryRange");
        this.autoChangedRange = nbt.getBoolean("autoChangedRange");
        this.tankPump.deserializeNBT(nbt.getCompound("tankPump"), this.silktouch);
    }

    @Override
    public CompoundNBT write(final CompoundNBT nbt) {
        nbt.putBoolean("silktouch", this.silktouch);
        nbt.putByte("fortune", this.fortune);
        nbt.putByte("unbreaking", this.unbreaking);
        if (connectTo != null)
            nbt.putByte("connectTo", (byte) this.connectTo.ordinal());
        nbt.putByte("range", this.range);
        nbt.putBoolean("quarryRange", this.quarryRange);
        nbt.putBoolean("autoChangedRange", this.autoChangedRange);
        nbt.put("tankPump", tankPump.serializeNBT(this.silktouch));
        return super.write(nbt);
    }

    @Override
    public void tick() {
        if (world != null && !world.isRemote) {
            for (Direction facing : Direction.values()) {
                BlockPos offset = getPos().offset(facing);
                FluidUtil.getFluidHandler(world, offset, facing.getOpposite()).ifPresent(destination -> {
                    IFluidHandler pumpTank = tankPump.pumpTankEnumMap.get(facing);
                    FluidUtil.tryFluidTransfer(destination, pumpTank, Integer.MAX_VALUE, true);
                });
            }
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
        if (world != null && !world.isRemote) {
            TileEntity te;
            for (Direction facing : Direction.values()) {
                te = world.getTileEntity(getPos().offset(facing));
                if (te instanceof IAttachable && ((IAttachable) te).connect(facing.getOpposite(), Attachments.FLUID_PUMP)) {
                    setConnectTo(facing);
                    ((IAttachable) te).connectAttachment(facing.getOpposite(), Attachments.FLUID_PUMP, false);
                    S_sendNowPacket();
                    return;
                }
            }
            setConnectTo(null);
            S_sendNowPacket();
        }
    }

    private void S_sendNowPacket() {
        assert world != null;
        //when connection changed or working changed
        if (preFacing != connectTo || world.getBlockState(getPos()).get(QPBlock.WORKING()) != G_working()) {
            preFacing = connectTo;
            PacketHandler.sendToAround(Now.create(this), world, getPos());
        }
    }

    @Override
    public void setConnectTo(@Nullable Direction connectTo) {
        this.connectTo = connectTo;
        if (world != null) {
            BlockState state = world.getBlockState(getPos());
            if (connectTo != null ^ state.get(BlockStateProperties.ENABLED)) {
                world.setBlockState(getPos(), state.with(BlockStateProperties.ENABLED, connectTo != null));
            }
        }
    }

    @Override
    public IModule getModule() {
        return PumpModule.fromTile(this, (APowerTile) G_connected());
    }

    public void setWorking(boolean b) {
        if (b) {
            this.cy = this.py = -1;
        } else {
            this.py = Integer.MIN_VALUE;
        }
        if (world != null && !world.isRemote) {
            BlockState state = world.getBlockState(getPos());
            world.setBlockState(getPos(), state.with(QPBlock.WORKING(), b));
        }
    }

    public void S_OpenGUI(Direction facing, final PlayerEntity ep) {
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

    public void S_changeRange(final PlayerEntity ep) {
        if (this.range >= (this.fortune + 1) * 2) {
//            if (G_connected() instanceof TileQuarry || G_connected() instanceof TileQuarry2)
//                this.quarryRange = true;
            this.range = 0;
        } else if (this.quarryRange)
            this.quarryRange = false;
        else
            this.range++;
        if (this.quarryRange)
            ep.sendStatusMessage(new TranslationTextComponent(TranslationKeys.PUMP_RTOGGLE_QUARRY), false);
        else
            ep.sendStatusMessage(new TranslationTextComponent(TranslationKeys.PUMP_RTOGGLE_NUM, Integer.toString(this.range * 2 + 1)), false);
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
        assert world != null;
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
        @Nullable RangeWrapper b = null;
        if (tb instanceof TileQuarry /* TODO|| tb instanceof TileQuarry2*/)
            b = RangeWrapper.of(tb);
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
                    .getChunk(kx + (this.xOffset >> 4), kz + (this.zOffset >> 4), true)
                    .getSections();
        S_put(x - this.xOffset, y, z - this.zOffset);
        BlockState b_c;
        ChunkSection ebs_c;
        while (cp != cg) {
            ebs_c = this.storageArray[xb[cg] >> 4][zb[cg] >> 4][yb[cg] >> 4];
            if (ebs_c != null) {
                b_c = ebs_c.getBlockState(xb[cg] & 0xF, yb[cg] & 0xF, zb[cg] & 0xF);
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
        assert world != null;
        if (this.cx != x || this.cy != y || this.cz != z || this.py < this.cy
            || world.getDayTime() - this.fwt > 200)
            S_searchLiquid(x, y, z);
        else {
            this.storageArray = new ChunkSection[this.storageArray.length][this.storageArray[0].length][];
            for (int kx = 0; kx < this.storageArray.length; kx++) {
                for (int kz = 0; kz < this.storageArray[0].length; kz++) {
                    this.storageArray[kx][kz] = world.getChunkProvider()
                        .getChunk(kx + (this.xOffset >> 4), kz + (this.zOffset >> 4), true)
                        .getSections();
                }
            }
        }

        int count = 0;
        BlockState bb;
        int bz;
        do {
            do {
                if (this.px == -1) {
                    int bx;
                    for (bx = 0; bx < this.block_side_x; bx++)
                        for (bz = 0; bz < this.block_side_z; bz++)
                            if ((this.blocks[this.py - this.yOffset][bx][bz] & 0x40) != 0) {
                                bb = this.storageArray[bx >> 4][bz >> 4][this.py >> 4].getBlockState(bx & 0xF, this.py & 0xF, bz & 0xF);
                                if (isLiquid(bb))
                                    count++;
                            }
                } else {
                    BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                    for (bz = 0; bz < this.block_side_z; bz++)
                        if (this.blocks[this.py - this.yOffset][this.px][bz] != 0) {
                            bb = this.storageArray[this.px >> 4][bz >> 4][this.py >> 4].getBlockState(this.px & 0xF, this.py & 0xF, bz & 0xF);
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
                            if (tile instanceof TileQuarry /* TODO || tile instanceof TileQuarry2*/) {
                                RangeWrapper wrapper = RangeWrapper.of(tile);
                                int xTarget = bx + xOffset;
                                int zTarget = bz + zOffset;
                                if (wrapper.waiting()) {
                                    if ((wrapper.xMin <= xTarget && xTarget <= wrapper.xMax) && (wrapper.zMin <= zTarget && zTarget <= wrapper.zMax)) {
                                        if (Config.common().debug())
                                            QuarryPlus.LOGGER.warn(String.format("Quarry placed frame at %d, %d, %d", xTarget, py, zTarget));
                                        autoChange(true);
                                    }
                                } else {
                                    if (Config.common().debug()) {
                                        if ((wrapper.xMin < xTarget && xTarget < wrapper.xMax) && (wrapper.zMin < zTarget && zTarget < wrapper.zMax))
                                            QuarryPlus.LOGGER.warn(String.format("Quarry placed frame at %d, %d, %d", xTarget, py, zTarget));
                                    }
                                    autoChange(false);
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
    public static boolean isLiquid(@Nonnull final BlockState state, final boolean findSource, final World world, final BlockPos pos) {
        if (state.getFluidState() != Fluids.EMPTY.getDefaultState()) return true;
        Block block = state.getBlock();
        if (block instanceof IFluidBlock)
            return !findSource || ((IFluidBlock) block).canDrain(world, pos);
        else {
            return (block == Blocks.WATER || block == Blocks.LAVA || state.getMaterial().isLiquid())
                && (!findSource || state.get(FlowingFluidBlock.LEVEL) == 0);
        }
    }

    public static boolean isLiquid(@Nonnull BlockState state) {
        return isLiquid(state, false, null, null);
    }

    private void drainBlock(final int bx, final int bz, final BlockState tb) {
        assert world != null;
        if (isLiquid(this.storageArray[bx >> 4][bz >> 4][this.py >> 4].getBlockState(bx & 0xF, this.py & 0xF, bz & 0xF))) {
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
            if (fluidState.isSource()) {
                HasStorage.Storage storage = G_connected() instanceof HasStorage ? ((HasStorage) G_connected()).getStorage() : getStorage();
                storage.insertFluid(new FluidStack(fluidState.getFluid(), FluidAttributes.BUCKET_VOLUME));
            }
            world.setBlockState(blockPos, tb);
        }
    }

    public List<ITextComponent> C_getNames() {
        Collection<FluidStack> allContents = tankPump.getAllContents();
        if (!allContents.isEmpty()) {
            List<ITextComponent> list = new ArrayList<>(allContents.size() + 1);
            list.add(new TranslationTextComponent(TranslationKeys.PUMP_CONTAIN));
            allContents.forEach(s -> list.add(new TranslationTextComponent(TranslationKeys.LIQUID_FORMAT,
                s.getDisplayName(), Integer.toString(s.getAmount()))));
            return list;
        } else {
            return Collections.singletonList(new TranslationTextComponent(TranslationKeys.PUMP_CONTAIN_NO));
        }
    }

    @Override
    public List<ITextComponent> getDebugMessages() {
        ArrayList<ITextComponent> list = new ArrayList<>();
        list.add(toComponentString.apply("Connection : " + this.connectTo));
//        for (Direction facing : Direction.values()) {
//            this.mapping.get(facing).stream()
//                .reduce(combiner).map(toComponentString)
//                .ifPresent(list::add);
//        }
        Collection<FluidStack> allContents = tankPump.getAllContents();
        if (!allContents.isEmpty()) {
            list.add(new TranslationTextComponent(TranslationKeys.PUMP_CONTAIN));
            allContents.stream().map(fluidStack -> Objects.toString(fluidStack.getFluid().getRegistryName()) + fluidStack.getAmount() + "mB")
                .reduce(combiner).map(toComponentString)
                .ifPresent(list::add);
        } else {
            list.add(new TranslationTextComponent(TranslationKeys.PUMP_CONTAIN_NO));
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> pumpCapability = tankPump.getCapability(cap, side);
        if (pumpCapability.isPresent()) return pumpCapability;
        else return super.getCapability(cap, side);
    }

    @Override
    public TankPump getStorage() {
        return tankPump;
    }
}
