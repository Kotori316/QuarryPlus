package com.yogpc.qp.machines.pump;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.FluidStore;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IModule;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import scala.collection.JavaConverters;
import scala.collection.immutable.Set;

public abstract class PumpModule implements IModule {
    public static final String ID = QuarryPlus.modID + ":" + "module_pump";
    private static final Set<ModuleType> TYPE_SET =
        JavaConverters.asScalaSet(Stream.of(TypeBeforeBreak$.MODULE$).collect(Collectors.toSet())).toSet();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<ModuleType> calledWhen() {
        return TYPE_SET;
    }

    public static PumpModule fromTile(TilePump pump, APowerTile tile) {
        return new Tile(pump, tile);
    }

    public static PumpModule fromModule(APowerTile connected, IntSupplier unbreaking) {
        return new Module(connected, unbreaking);
    }

    @Override
    public String toString() {
        String simpleName = getClass().getName();
        return simpleName.substring(simpleName.lastIndexOf(".") + 1);
    }

    private static class Tile extends PumpModule {
        private final TilePump pump;
        private final APowerTile tile;

        public Tile(TilePump pump, APowerTile tile) {
            this.pump = Objects.requireNonNull(pump);
            this.tile = Objects.requireNonNull(tile);
        }

        @Override
        public boolean action(CalledWhen when) {
            if (when instanceof BeforeBreak) {
                BeforeBreak beforeBreak = (BeforeBreak) when;
                BlockPos target = beforeBreak.pos();
                BlockState state = beforeBreak.world().getBlockState(target);
                if (TilePump.isLiquid(state)) {
                    return pump.S_removeLiquids(tile, target.getX(), target.getY(), target.getZ());
                }
            }
            return true;
        }
    }

    private static class Module extends PumpModule {
        private final APowerTile tile;
        private World world;
        private final BlockPos pos;
        private final IntSupplier unbreaking;

        public Module(APowerTile connected, IntSupplier unbreaking) {
            this.tile = connected;
            this.world = connected.getWorld();
            this.pos = connected.getPos();
            this.unbreaking = unbreaking;
        }

        @Override
        public boolean action(CalledWhen when) {
            if (when instanceof BeforeBreak) {
                BeforeBreak beforeBreak = (BeforeBreak) when;
                BlockPos target = beforeBreak.pos();
                BlockState state = beforeBreak.world().getBlockState(target);
                if (TilePump.isLiquid(state)) {
                    world = tile.getWorld();
                    return S_removeLiquids(tile, target.getX(), target.getY(), target.getZ());
                }
            }
            return true;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        private static final int Y_SIZE = 256;
        private static final int CHUNK_SCALE = 16;

        private byte[][][] blocks;
        private ChunkSection[][][] storageArray;
        private int xOffset, yOffset, zOffset, px, py = Integer.MIN_VALUE;
        private int cx, cy = -1, cz;
        private boolean quarryRange = true;
        private boolean autoChangedRange = false;

        private int block_side_x, block_side_z;

        private static final int ARRAY_MAX = 0x80000;
        private static final int[] xb = new int[ARRAY_MAX];
        private static final int[] yb = new int[ARRAY_MAX];
        private static final int[] zb = new int[ARRAY_MAX];
        private static int cp = 0;
        private long fwt;

        private APowerTile G_connected() {
            return tile;
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
            final APowerTile tb = G_connected();
            @Nullable RangeWrapper b = null;
//            if (tb instanceof TileQuarry || tb instanceof TileQuarry2)
//                b = RangeWrapper.of(tb);
            int range = 0;
            if (b != null && b.yMax != Integer.MIN_VALUE) {
                chunk_side_x = 1 + (b.xMax >> 4) - (b.xMin >> 4);
                chunk_side_z = 1 + (b.zMax >> 4) - (b.zMin >> 4);
                this.xOffset = b.xMin & 0xFFFFFFF0;
                this.zOffset = b.zMin & 0xFFFFFFF0;
                final int x_add = range * 2 + 1 - chunk_side_x;
                if (x_add > 0) {
                    chunk_side_x += x_add;
                    this.xOffset -=
                        ((x_add & 0xFFFFFFFE) << 3)
                            + (x_add % 2 != 0 && (b.xMax + b.xMin + 1) / 2 % 0x10 <= 8 ? 0x10 : 0);
                }
                final int z_add = range * 2 + 1 - chunk_side_z;
                if (z_add > 0) {
                    chunk_side_z += z_add;
                    this.zOffset -=
                        ((z_add & 0xFFFFFFFE) << 3)
                            + (z_add % 2 != 0 && (b.zMax + b.zMin + 1) / 2 % 0x10 <= 8 ? 0x10 : 0);
                }
            } else {
                this.quarryRange = false;
                chunk_side_x = chunk_side_z = 1 + range * 2;
                this.xOffset = (x >> 4) - range << 4;
                this.zOffset = (z >> 4) - range << 4;

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
                    if (this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] == 0 && TilePump.isLiquid(b_c)) {
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
                                    if (TilePump.isLiquid(bb))
                                        count++;
                                }
                    } else {
                        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                        for (bz = 0; bz < this.block_side_z; bz++)
                            if (this.blocks[this.py - this.yOffset][this.px][bz] != 0) {
                                bb = this.storageArray[this.px >> 4][bz >> 4][this.py >> 4].getBlockState(this.px & 0xF, this.py & 0xF, bz & 0xF);
                                mutableBlockPos.setPos(this.px + this.xOffset, this.py, bz + this.zOffset);
                                if (TilePump.isLiquid(bb, Config.common().removeOnlySource().get(), world, mutableBlockPos))
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
            if (count > 0 && PowerManager.useEnergyPump(tile, this.unbreaking.getAsInt(), count, this.px == -1 ? count : 0))
                if (this.px == -1) {
                    int bx;
                    for (bx = 0; bx < this.block_side_x; bx++)
                        for (bz = 0; bz < this.block_side_z; bz++)
                            if ((this.blocks[this.py - this.yOffset][bx][bz] & 0x40) != 0) {
                                drainBlock(bx, bz, Holder.blockFrame().getDammingState());
                                /*if (tile instanceof TileQuarry || tile instanceof TileQuarry2) {
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
                                }*/
                            }
                } else
                    for (bz = 0; bz < this.block_side_z; bz++)
                        if (this.blocks[this.py - this.yOffset][this.px][bz] != 0)
                            drainBlock(this.px, bz, Blocks.AIR.getDefaultState());
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

        private void drainBlock(final int bx, final int bz, final BlockState tb) {
            if (TilePump.isLiquid(this.storageArray[bx >> 4][bz >> 4][this.py >> 4].getBlockState(bx & 0xF, this.py & 0xF, bz & 0xF))) {
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
                    if (tile instanceof HasStorage) {
                        HasStorage.Storage storage = ((HasStorage) tile).getStorage();
                        storage.insertFluid(fluidState.getFluid(), FluidStore.AMOUNT);
                    } else
                        FluidStore.injectToNearTile(world, pos, fluidState.getFluid());
                }
                world.setBlockState(blockPos, tb);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
}
