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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.lib.misc.PositionUtil;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockMarker;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.marker.LinkReply;
import com.yogpc.qp.packet.marker.LinkRequest;
import com.yogpc.qp.packet.marker.LinkUpdate;
import com.yogpc.qp.packet.marker.RemoveLink;
import com.yogpc.qp.render.Box;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Symbol;

@Optional.Interface(iface = "buildcraft.api.tiles.ITileAreaProvider", modid = QuarryPlus.Optionals.Buildcraft_tiles)
public class TileMarker extends APacketTile implements ITileAreaProvider, ITickable, IChunkLoadTile, IDebugSender {
    public static final List<Link> linkList = Collections.synchronizedList(new ArrayList<>());
    public static final List<Laser> laserList = Collections.synchronizedList(new ArrayList<>());
    public static final IndexOnlyList<Link> LINK_INDEX = new IndexOnlyList<>(linkList, linkList);
    public static final IndexOnlyList<Laser> LASER_INDEX = new IndexOnlyList<>(laserList, laserList);

    private static final int MAX_SIZE = 256;
    public static final Symbol SYMBOL = Symbol.apply("MarkerPlus");
    private final boolean bcLoaded;

    public TileMarker() {
        this.bcLoaded = ModAPIManager.INSTANCE.hasAPI(QuarryPlus.Optionals.Buildcraft_tiles);
    }

    @Override
    protected Symbol getSymbol() {
        return SYMBOL;
    }

    public Link link;
    public Laser laser;

    @Override
    public void update() {
        if (this.vlF) {
            this.vlF = false;
            int i = LINK_INDEX.indexOf(this);
            if (i >= 0)
                this.link = linkList.get(i);
            int i2 = LASER_INDEX.indexOf(this);
            if (i2 >= 0)
                this.laser = laserList.get(i2);
            G_updateSignal();
            if (this.getWorld().isRemote)
                PacketHandler.sendToServer(LinkRequest.create(this));
        }
    }

    @Override
    public BlockPos min() {
        return this.link == null ? getPos() : link.minPos();
    }

    @Override
    public BlockPos max() {
        return this.link == null ? getPos() : link.maxPos();
    }

    @Override
    public void removeFromWorld() {
        if (this.link == null) {
            QuarryPlusI.blockMarker().dropBlockAsItem(getWorld(), getPos(), getWorld().getBlockState(getPos()), 0);
            getWorld().setBlockToAir(getPos());
            return;
        }
        final ArrayList<ItemStack> al = this.link.removeConnection(true);
        for (final ItemStack is : al) {
            InventoryHelper.spawnItemStack(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), is);
        }
    }

    public List<ItemStack> removeFromWorldWithItem() {
        if (this.link != null)
            return this.link.removeConnection(true);
        NonNullList<ItemStack> ret = NonNullList.create();
        QuarryPlusI.blockMarker().getDrops(ret, getWorld(), getPos(), getWorld().getBlockState(getPos()), 0);
        getWorld().setBlockToAir(getPos());
        return ret;
    }

    private static void S_renewConnection(final Link l, final World w, final int x, final int y, final int z) {
        int tx = 0, ty = 0, tz = 0;
        Block b;
        if (l.xMax == l.xMin) {
            for (tx = 1; tx <= MAX_SIZE; tx++) {
                b = w.getBlockState(new BlockPos(x + tx, y, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x + tx, y, z))) {
                    l.xMax = x + tx;
                    break;
                }
                b = w.getBlockState(new BlockPos(x - tx, y, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x - tx, y, z))) {
                    tx = -tx;
                    l.xMin = x + tx;
                    break;
                }
            }
            if (l.xMax == l.xMin)
                tx = 0;
        }
        if (l.yMax == l.yMin) {
            for (ty = 1; ty <= MAX_SIZE; ty++) {
                b = w.getBlockState(new BlockPos(x, y + ty, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y + ty, z))) {
                    l.yMax = y + ty;
                    break;
                }
                b = w.getBlockState(new BlockPos(x, y - ty, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y - ty, z))) {
                    ty = -ty;
                    l.yMin = y + ty;
                    break;
                }
            }
            if (l.yMax == l.yMin)
                ty = 0;
        }
        if (l.zMax == l.zMin) {
            for (tz = 1; tz <= MAX_SIZE; tz++) {
                b = w.getBlockState(new BlockPos(x, y, z + tz)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y, z + tz))) {
                    l.zMax = z + tz;
                    break;
                }
                b = w.getBlockState(new BlockPos(x, y, z - tz)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y, z - tz))) {
                    tz = -tz;
                    l.zMin = z + tz;
                    break;
                }
            }
            if (l.zMax == l.zMin)
                tz = 0;
        }
        if (l.xMax == l.xMin && ty != 0)
            S_renewConnection(l, w, x, y + ty, z);
        if (l.xMax == l.xMin && tz != 0)
            S_renewConnection(l, w, x, y, z + tz);
        if (l.yMax == l.yMin && tx != 0)
            S_renewConnection(l, w, x + tx, y, z);
        if (l.yMax == l.yMin && tz != 0)
            S_renewConnection(l, w, x, y, z + tz);
        if (l.zMax == l.zMin && tx != 0)
            S_renewConnection(l, w, x + tx, y, z);
        if (l.zMax == l.zMin && ty != 0)
            S_renewConnection(l, w, x, y + ty, z);

    }

    public void G_updateSignal() {
        if (this.laser != null) {
            this.laser.destructor();
            this.laser = null;
        }
        if (!this.getWorld().isRemote) {
            if (getWorld().isBlockPowered(getPos()) && (this.link == null ||
                this.link.xMin == this.link.xMax || this.link.yMin == this.link.yMax || this.link.zMin == this.link.zMax)) {
                //create
                this.laser = new Laser(this.getWorld(), getPos(), this.link);
                PacketHandler.sendToAround(LinkUpdate.create(this, true), getWorld(), getPos());
            } else {
                //remove
                PacketHandler.sendToAround(LinkUpdate.create(this, false), getWorld(), getPos());
            }
        }
    }

    public void S_tryConnection() {// onBlockActivated
        if (this.link != null)
            this.link.removeConnection(false);
        this.link = new Link(getWorld(), getPos());
        S_renewConnection(this.link, getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
        if (this.link.xMax == this.link.xMin && this.link.yMax == this.link.yMin && this.link.zMax == this.link.zMin) {
            this.link = null;
            return;
        }
        this.link.init();
        this.link.makeLaser();
        PacketHandler.sendToAround(LinkReply.create(this), getWorld(), getPos());
        G_updateSignal();
    }

    void G_destroy() {
        if (this.link != null)
            this.link.removeConnection(false);
        if (this.laser != null)
            this.laser.destructor();
        ForgeChunkManager.releaseTicket(this.chunkTicket);
    }

    private Ticket chunkTicket;

    @Override
    public void requestTicket() {// onPostBlockPlaced
        if (this.chunkTicket != null)
            return;
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, this.getWorld(), Type.NORMAL);
        if (this.chunkTicket == null)
            return;
        final NBTTagCompound tag = this.chunkTicket.getModData();
        tag.setInteger("quarryX", getPos().getX());
        tag.setInteger("quarryY", getPos().getY());
        tag.setInteger("quarryZ", getPos().getZ());
        forceChunkLoading(this.chunkTicket);
    }

    @Override
    public void forceChunkLoading(final Ticket ticket) {// ticketsLoaded
        if (this.chunkTicket == null)
            this.chunkTicket = ticket;
        ForgeChunkManager.forceChunk(ticket, new ChunkPos(getPos()));
    }

    private boolean vlF;

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        if (laser != null || link != null) {
            return 256 * 256 * 4;
        } else {
            return super.getMaxRenderDistanceSquared();
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (bcLoaded) {
            if (capability == TilesAPI.CAP_TILE_AREA_PROVIDER) {
                return true;
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (bcLoaded) {
            if (capability == TilesAPI.CAP_TILE_AREA_PROVIDER) {
                return TilesAPI.CAP_TILE_AREA_PROVIDER.cast(this);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void validate() {
        super.validate();
        this.vlF = true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getWorld().getBlockState(getPos()).getBlock() != QuarryPlusI.blockMarker())
            G_destroy();
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public boolean isValidFromLocation(BlockPos pos) {
        if (link != null) {
            boolean xFlag = link.xMin <= pos.getX() && link.xMax <= pos.getX();
            boolean yFlag = link.yMin <= pos.getY() && link.yMax <= pos.getY();
            boolean zFlag = link.zMin <= pos.getZ() && link.zMax <= pos.getZ();
            if (xFlag && yFlag && zFlag) {
                return false;
            }
            for (BlockPos p : PositionUtil.getCorners(min(), max())) {
                if (PositionUtil.isNextTo(p, pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.marker;
    }

    @Override
    public List<? extends ITextComponent> getDebugmessages() {
        return Arrays.asList(new TextComponentString("Link : " + link),
            new TextComponentString("Laser : " + laser));
    }

    public static class BlockIndex {
        final World w;
        final int x, y, z;

        public BlockIndex(final World pw, final int px, final int py, final int pz) {
            this.w = pw;
            this.x = px;
            this.y = py;
            this.z = pz;
        }
    }

    /**
     * Show when marker is redstone-powered.
     * The guide of placing marker.
     */
    public static class Laser {
        public final World w;
        final int x, y, z;
        /**
         * index 0=x , 1=y , 2=z
         */
        public final AxisAlignedBB[] lineBoxes = new AxisAlignedBB[6];
        public final Box[] boxes;

        public Laser(World w, BlockPos pos, Link link) {
            this(w, pos.getX(), pos.getY(), pos.getZ(), link);
        }

        private Laser(final World pw, final int px, final int py, final int pz, final Link l) {
            this.x = px;
            this.y = py;
            this.z = pz;
            this.w = pw;
            double b = 10d / 16d, c = 6d / 16d;
            if (l == null || l.xMin == l.xMax) {
                lineBoxes[0] = new AxisAlignedBB(px + b - MAX_SIZE, py + 0.5, pz + 0.5, px + c, py + 0.5, pz + 0.5);
                lineBoxes[3] = new AxisAlignedBB(px + b, py + 0.5, pz + 0.5, px + c + MAX_SIZE, py + 0.5, pz + 0.5);
            }
            if (l == null || l.yMin == l.yMax) {
                lineBoxes[1] = new AxisAlignedBB(px + 0.5, 0, pz + 0.5, px + 0.5, py - 0.1, pz + 0.5);
                lineBoxes[4] = new AxisAlignedBB(px + 0.5, py + b, pz + 0.5, px + 0.5, 255, pz + 0.5);
            }
            if (l == null || l.zMin == l.zMax) {
                lineBoxes[2] = new AxisAlignedBB(px + 0.5, py + 0.5, pz + b - MAX_SIZE, px + 0.5, py + 0.5, pz + c);
                lineBoxes[5] = new AxisAlignedBB(px + 0.5, py + 0.5, pz + b, px + 0.5, py + 0.5, pz + c + MAX_SIZE);
            }
            if (pw.isRemote) {
                boxes = Arrays.stream(lineBoxes).filter(nonNull)
                    .map(aabb -> Box.apply(aabb, 1d / 8d, 1d / 8d, 1d / 8d, false, false))
                    .toArray(Box[]::new);
            } else {
                //Server
                boxes = null;
            }
            destructor();
            laserList.add(this);
        }

        public void destructor() {
            laserList.remove(this);
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Laser))
                return false;
            final Laser l = (Laser) o;
            return l.x == this.x && l.y == this.y && l.z == this.z && l.w == this.w;
        }

        @Override
        public int hashCode() {
            return this.x << 21 ^ this.y << 11 ^ this.z;
        }

        @Override
        public String toString() {
            long i = Stream.of(lineBoxes).filter(nonNull).count();
            return x + " " + y + " " + z + " Lasers : " + i;
        }
    }

    /**
     * Link with other markers.
     */
    public static class Link {
        public int xMax, xMin, yMax, yMin, zMax, zMin;
        public final AxisAlignedBB[] lineBoxes = new AxisAlignedBB[12];
        public Box[] boxes;
        public final World w;

        Link(World world, BlockPos pos) {
            this(world, pos.getX(), pos.getY(), pos.getZ());
        }

        public Link(World world, BlockPos max, BlockPos min) {
            this(world, max.getX(), min.getX(), max.getY(), min.getY(), max.getZ(), min.getZ());
        }

        Link(final World pw, final int vx, final int vy, final int vz) {
            this.xMax = vx;
            this.xMin = vx;
            this.yMax = vy;
            this.yMin = vy;
            this.zMax = vz;
            this.zMin = vz;
            this.w = pw;
        }

        Link(final World pw, final int vxx, final int vxn, final int vyx, final int vyn, final int vzx, final int vzn) {
            this.xMax = vxx;
            this.xMin = vxn;
            this.yMax = vyx;
            this.yMin = vyn;
            this.zMax = vzx;
            this.zMin = vzn;
            this.w = pw;
        }

        private void connect(final TileEntity te) {
            if (te instanceof TileMarker) {
                TileMarker marker = (TileMarker) te;
                if (marker.link != null && marker.link != this) {
                    marker.link.removeConnection(false);
                }
                marker.link = this;
            }
        }

        private NonNullList<ItemStack> removeLink(final int x, final int y, final int z, final boolean bb) {
            NonNullList<ItemStack> ret = NonNullList.create();
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = this.w.getTileEntity(pos);
            IBlockState state = w.getBlockState(pos);
            if (state.getBlock() instanceof BlockMarker) {
                if (te instanceof TileMarker)
                    ((TileMarker) te).link = null;
                state.getBlock().getDrops(ret, this.w, pos, state, 0);
                if (bb)
                    this.w.setBlockToAir(pos);
            }
            return ret;
        }

        public void init() {
            final int i = linkList.indexOf(this);
            if (i >= 0)
                linkList.get(i).removeConnection(false);
            linkList.add(this);
            connect(this.w.getTileEntity(minPos()));
            connect(this.w.getTileEntity(new BlockPos(this.xMin, this.yMin, this.zMax)));
            connect(this.w.getTileEntity(new BlockPos(this.xMin, this.yMax, this.zMin)));
            connect(this.w.getTileEntity(new BlockPos(this.xMin, this.yMax, this.zMax)));
            connect(this.w.getTileEntity(new BlockPos(this.xMax, this.yMin, this.zMin)));
            connect(this.w.getTileEntity(new BlockPos(this.xMax, this.yMin, this.zMax)));
            connect(this.w.getTileEntity(new BlockPos(this.xMax, this.yMax, this.zMin)));
            connect(this.w.getTileEntity(maxPos()));
        }

        public ArrayList<ItemStack> removeConnection(final boolean bb) {
            linkList.remove(this);
            if (!this.w.isRemote) {
                PacketHandler.sendToDimension(RemoveLink.create(minPos(), maxPos(), w.provider.getDimension()), w.provider.getDimension());
            }
            deleteLaser();
            final ArrayList<ItemStack> i = new ArrayList<>();
            i.addAll(removeLink(this.xMin, this.yMin, this.zMin, bb));
            i.addAll(removeLink(this.xMin, this.yMin, this.zMax, bb));
            i.addAll(removeLink(this.xMin, this.yMax, this.zMin, bb));
            i.addAll(removeLink(this.xMin, this.yMax, this.zMax, bb));
            i.addAll(removeLink(this.xMax, this.yMin, this.zMin, bb));
            i.addAll(removeLink(this.xMax, this.yMin, this.zMax, bb));
            i.addAll(removeLink(this.xMax, this.yMax, this.zMin, bb));
            i.addAll(removeLink(this.xMax, this.yMax, this.zMax, bb));
            return i;
        }

        public void makeLaser() {
            deleteLaser();
            byte flag = 0;
            final double a = 0.5d, b = 10d / 16d, c = 6d / 16d;
            if (this.xMin != this.xMax)
                flag |= 1;
            if (this.yMin != this.yMax)
                flag |= 2;
            if (this.zMin != this.zMax)
                flag |= 4;
            if ((flag & 1) == 1) {//x
                lineBoxes[0] = new AxisAlignedBB(xMin + b, yMin + a, zMin + a, xMax + c, yMin + a, zMin + a);
            }
            if ((flag & 2) == 2) {//y
                lineBoxes[4] = new AxisAlignedBB(xMin + a, yMin + b, zMin + a, xMin + a, yMax + c, zMin + a);
            }
            if ((flag & 4) == 4) {//z
                lineBoxes[8] = new AxisAlignedBB(xMin + a, yMin + a, zMin + b, xMin + a, yMin + a, zMax + c);
            }
            if ((flag & 3) == 3) {//xy
                lineBoxes[2] = new AxisAlignedBB(xMin + b, yMax + a, zMin + a, xMax + c, yMax + a, zMin + a);
                lineBoxes[6] = new AxisAlignedBB(xMax + a, yMin + b, zMin + a, xMax + a, yMax + c, zMin + a);
            }
            if ((flag & 5) == 5) {//xz
                lineBoxes[1] = new AxisAlignedBB(xMin + b, yMin + a, zMax + a, xMax + c, yMin + a, zMax + a);
                lineBoxes[9] = new AxisAlignedBB(xMax + a, yMin + a, zMin + b, xMax + a, yMin + a, zMax + c);
            }
            if ((flag & 6) == 6) {//yz
                lineBoxes[5] = new AxisAlignedBB(xMin + a, yMin + b, zMax + a, xMin + a, yMax + c, zMax + a);
                lineBoxes[10] = new AxisAlignedBB(xMin + a, yMax + a, zMin + b, xMin + a, yMax + a, zMax + c);
            }
            if ((flag & 7) == 7) {//xyz
                lineBoxes[3] = new AxisAlignedBB(xMin + b, yMax + a, zMax + a, xMax + c, yMax + a, zMax + a);
                lineBoxes[7] = new AxisAlignedBB(xMax + a, yMin + b, zMax + a, xMax + a, yMax + c, zMax + a);
                lineBoxes[11] = new AxisAlignedBB(xMax + a, yMax + a, zMin + b, xMax + a, yMax + a, zMax + c);
            }
            if (w.isRemote) {
                boxes = Arrays.stream(lineBoxes).filter(nonNull)
                    .map(aabb -> Box.apply(aabb, 1d / 8d, 1d / 8d, 1d / 8d, false, false))
                    .toArray(Box[]::new);
            } else {
                boxes = null;
            }
        }

        public void deleteLaser() {
        }

        public BlockPos minPos() {
            return new BlockPos(xMin, yMin, zMin);
        }

        public BlockPos maxPos() {
            return new BlockPos(xMax, yMax, zMax);
        }

        @Override
        public String toString() {
            long i = Stream.of(lineBoxes).filter(nonNull).count();
            return minPos() + " to " + maxPos() + " Lasers : " + i;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Link))
                return false;
            final Link l = (Link) o;
            return l.xMin == this.xMin && l.xMax == this.xMax && l.yMin == this.yMin &&
                l.yMax == this.yMax && l.zMin == this.zMin && l.zMax == this.zMax && l.w == this.w;
        }

        @Override
        public int hashCode() {
            return (xMin << 10 + yMin << 5 + zMin) + (xMax << 10 + yMax << 5 + zMax);
        }
    }

    static {
        BiPredicate<Link, Object> p1 = (link1, r) -> {
            if (r instanceof BlockIndex) {
                BlockIndex bi = ((BlockIndex) r);
                return (bi.x == link1.xMin || bi.x == link1.xMax) && (bi.y == link1.yMin || bi.y == link1.yMax) && (bi.z == link1.zMin || bi.z == link1.zMax) && link1.w == bi.w;
            }
            return false;
        };
        BiPredicate<Link, Object> p2 = (link1, r) -> {
            if (r instanceof TileEntity) {
                final TileEntity te = (TileEntity) r;
                return (te.getPos().equals(link1.minPos()) || te.getPos().equals(link1.maxPos())) && link1.w == te.getWorld();
            }
            return false;
        };

        BiPredicate<Laser, Object> p3 = (laser1, r) -> {
            if (r instanceof BlockIndex) {
                BlockIndex bi = ((BlockIndex) r);
                return bi.x == laser1.x && bi.y == laser1.y && bi.z == laser1.z && bi.w == laser1.w;
            }
            return false;
        };
        BiPredicate<Laser, Object> p4 = (laser1, r) -> {
            if (r instanceof TileEntity) {
                final TileEntity te = (TileEntity) r;
                return te.getPos().equals(new BlockPos(laser1.x, laser1.y, laser1.z)) && laser1.w == te.getWorld();
            }
            return false;
        };

        LINK_INDEX.addPredicate(p1);
        LINK_INDEX.addPredicate(p2);

        LASER_INDEX.addPredicate(p3);
        LASER_INDEX.addPredicate(p4);
    }
}
