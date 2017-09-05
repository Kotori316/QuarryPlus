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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import buildcraft.api.core.IAreaProvider;
import com.google.common.collect.Sets;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockMarker;
import com.yogpc.qp.entity.EntityLaser;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.marker.LinkReply;
import com.yogpc.qp.packet.marker.LinkRequest;
import com.yogpc.qp.packet.marker.LinkUpdate;
import com.yogpc.qp.packet.marker.RemoveLaser;
import com.yogpc.qp.packet.marker.RemoveLink;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "buildcraft.api.core.IAreaProvider", modid = QuarryPlus.Optionals.Buildcraft_modID)
public class TileMarker extends APacketTile implements IAreaProvider, ITickable {
    public static final ArrayList<Link> linkList = new ArrayList<>();
    public static final ArrayList<Laser> laserList = new ArrayList<>();
    public static final IndexOnlyList<Link> LINK_INDEX = new IndexOnlyList<>(linkList);
    public static final IndexOnlyList<Laser> LASER_INDEX = new IndexOnlyList<>(laserList);

    private static final int MAX_SIZE = 256;
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
            QuarryPlusI.blockMarker.dropBlockAsItem(getWorld(), getPos(), getWorld().getBlockState(getPos()), 0);
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
        List<ItemStack> ret = new LinkedList<>();
        ret.addAll(QuarryPlusI.blockMarker.getDrops(getWorld(), getPos(), getWorld().getBlockState(getPos()), 0));
        getWorld().setBlockToAir(getPos());
        return ret;
    }

    private static void S_renewConnection(final Link l, final World w, final int x, final int y, final int z) {
        int tx = 0, ty = 0, tz = 0;
        Block b;
        if (l.xx == l.xn) {
            for (tx = 1; tx <= MAX_SIZE; tx++) {
                b = w.getBlockState(new BlockPos(x + tx, y, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x + tx, y, z))) {
                    l.xx = x + tx;
                    break;
                }
                b = w.getBlockState(new BlockPos(x - tx, y, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x - tx, y, z))) {
                    tx = -tx;
                    l.xn = x + tx;
                    break;
                }
            }
            if (l.xx == l.xn)
                tx = 0;
        }
        if (l.yx == l.yn) {
            for (ty = 1; ty <= MAX_SIZE; ty++) {
                b = w.getBlockState(new BlockPos(x, y + ty, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y + ty, z))) {
                    l.yx = y + ty;
                    break;
                }
                b = w.getBlockState(new BlockPos(x, y - ty, z)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y - ty, z))) {
                    ty = -ty;
                    l.yn = y + ty;
                    break;
                }
            }
            if (l.yx == l.yn)
                ty = 0;
        }
        if (l.zx == l.zn) {
            for (tz = 1; tz <= MAX_SIZE; tz++) {
                b = w.getBlockState(new BlockPos(x, y, z + tz)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y, z + tz))) {
                    l.zx = z + tz;
                    break;
                }
                b = w.getBlockState(new BlockPos(x, y, z - tz)).getBlock();
                if (b instanceof BlockMarker && !LINK_INDEX.contains(new BlockIndex(w, x, y, z - tz))) {
                    tz = -tz;
                    l.zn = z + tz;
                    break;
                }
            }
            if (l.zx == l.zn)
                tz = 0;
        }
        if (l.xx == l.xn && ty != 0)
            S_renewConnection(l, w, x, y + ty, z);
        if (l.xx == l.xn && tz != 0)
            S_renewConnection(l, w, x, y, z + tz);
        if (l.yx == l.yn && tx != 0)
            S_renewConnection(l, w, x + tx, y, z);
        if (l.yx == l.yn && tz != 0)
            S_renewConnection(l, w, x, y, z + tz);
        if (l.zx == l.zn && tx != 0)
            S_renewConnection(l, w, x + tx, y, z);
        if (l.zx == l.zn && ty != 0)
            S_renewConnection(l, w, x, y + ty, z);

    }

    public void G_updateSignal() {
        if (this.laser != null) {
            this.laser.destructor();
            this.laser = null;
        }
        if ((getWorld().isBlockIndirectlyGettingPowered(getPos()) > 0 || getWorld().isBlockIndirectlyGettingPowered(getPos().offset(EnumFacing.UP)) > 0)
                && (this.link == null || this.link.xn == this.link.xx || this.link.yn == this.link.yx || this.link.zn == this.link.zx))
            this.laser = new Laser(this.getWorld(), getPos(), this.link);
        if (!this.getWorld().isRemote)
            PacketHandler.sendToAround(LinkUpdate.create(this), getWorld(), getPos());
    }

    public void S_tryConnection() {// onBlockActivated
        if (this.link != null)
            this.link.removeConnection(false);
        this.link = new Link(getWorld(), getPos());
        S_renewConnection(this.link, getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
        if (this.link.xx == this.link.xn && this.link.yx == this.link.yn && this.link.zx == this.link.zn) {
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

    public void forceChunkLoading(final Ticket ticket) {// ticketsLoaded
        if (this.chunkTicket == null)
            this.chunkTicket = ticket;
        final Set<ChunkPos> chunks = Sets.newHashSet();
        final ChunkPos quarryChunk = new ChunkPos(getPos());
        chunks.add(quarryChunk);
        ForgeChunkManager.forceChunk(ticket, quarryChunk);
    }

    private boolean vlF;

    @Override
    public void validate() {
        super.validate();
        this.vlF = true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getWorld().getBlockState(getPos()).getBlock() != QuarryPlusI.blockMarker)
            G_destroy();
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
        private final EntityLaser[] lasers = new EntityLaser[3];

        public Laser(World w, BlockPos pos, Link link) {
            this(w, pos.getX(), pos.getY(), pos.getZ(), link);
        }

        Laser(final World pw, final int px, final int py, final int pz, final Link l) {
            final double a = 0.5, b = 0.45, c = 0.1;
            this.x = px;
            this.y = py;
            this.z = pz;
            this.w = pw;
            //TODO check the size. c=0.1? too small?
            if (l == null || l.xn == l.xx)
                this.lasers[0] = new EntityLaser(pw, px - MAX_SIZE + a, py + b, pz + b, MAX_SIZE * 2, c, c, EntityLaser.BLUE_LASER);
            if (l == null || l.yn == l.yx)
                this.lasers[1] = new EntityLaser(pw, px + b, a, pz + b, c, 255, c, EntityLaser.BLUE_LASER);
            if (l == null || l.zn == l.zx)
                this.lasers[2] = new EntityLaser(pw, px + b, py + b, pz - MAX_SIZE + a, c, c, MAX_SIZE * 2, EntityLaser.BLUE_LASER);
            for (final EntityLaser eb : this.lasers)
                if (eb != null)
                    eb.getEntityWorld().spawnEntity(eb);
            final int i = laserList.indexOf(this);
            if (i >= 0)
                laserList.get(i).destructor();
            laserList.add(this);
        }

        public void destructor() {
            laserList.remove(this);
            if (!this.w.isRemote)
                PacketHandler.sendToDimension(RemoveLaser.create(x, y, z, w.provider.getDimension()), w.provider.getDimension());

            for (final EntityLaser eb : this.lasers)
                if (eb != null)
                    QuarryPlus.proxy.removeEntity(eb);
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
            long i = Stream.of(lasers).filter(Objects::nonNull).count();
            return x + " " + y + " " + z + " Lasers : " + i;
        }
    }

    /**
     * Link with other markers.
     */
    public static class Link {
        public int xx, xn, yx, yn, zx, zn;
        private final EntityLaser[] lasers = new EntityLaser[12];
        public final World w;

        Link(World world, BlockPos pos) {
            this(world, pos.getX(), pos.getY(), pos.getZ());
        }

        public Link(World world, BlockPos max, BlockPos min) {
            this(world, max.getX(), min.getX(), max.getY(), min.getY(), max.getZ(), min.getZ());
        }

        Link(final World pw, final int vx, final int vy, final int vz) {
            this.xx = vx;
            this.xn = vx;
            this.yx = vy;
            this.yn = vy;
            this.zx = vz;
            this.zn = vz;
            this.w = pw;
        }

        Link(final World pw, final int vxx, final int vxn, final int vyx, final int vyn, final int vzx, final int vzn) {
            this.xx = vxx;
            this.xn = vxn;
            this.yx = vyx;
            this.yn = vyn;
            this.zx = vzx;
            this.zn = vzn;
            this.w = pw;
        }

        private void connect(final TileEntity te) {
            if (te instanceof TileMarker) {
                if (((TileMarker) te).link != null && ((TileMarker) te).link != this) {
                    ((TileMarker) te).link.removeConnection(false);
                }
                ((TileMarker) te).link = this;
            }
        }

        private ArrayList<ItemStack> removeLink(final int x, final int y, final int z, final boolean bb) {
            ArrayList<ItemStack> ret = new ArrayList<>();
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity te = this.w.getTileEntity(pos);
            IBlockState state = w.getBlockState(pos);
            if (state.getBlock() instanceof BlockMarker) {
                if (te instanceof TileMarker)
                    ((TileMarker) te).link = null;
                ret.addAll(state.getBlock().getDrops(this.w, pos, state, 0));
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
            connect(this.w.getTileEntity(new BlockPos(this.xn, this.yn, this.zx)));
            connect(this.w.getTileEntity(new BlockPos(this.xn, this.yx, this.zn)));
            connect(this.w.getTileEntity(new BlockPos(this.xn, this.yx, this.zx)));
            connect(this.w.getTileEntity(new BlockPos(this.xx, this.yn, this.zn)));
            connect(this.w.getTileEntity(new BlockPos(this.xx, this.yn, this.zx)));
            connect(this.w.getTileEntity(new BlockPos(this.xx, this.yx, this.zn)));
            connect(this.w.getTileEntity(maxPos()));
        }

        public ArrayList<ItemStack> removeConnection(final boolean bb) {
            linkList.remove(this);
            if (!this.w.isRemote) {
                PacketHandler.sendToDimension(RemoveLink.create(minPos(), maxPos(), w.provider.getDimension()), w.provider.getDimension());
            }
            deleteLaser();
            final ArrayList<ItemStack> i = new ArrayList<>();
            i.addAll(removeLink(this.xn, this.yn, this.zn, bb));
            i.addAll(removeLink(this.xn, this.yn, this.zx, bb));
            i.addAll(removeLink(this.xn, this.yx, this.zn, bb));
            i.addAll(removeLink(this.xn, this.yx, this.zx, bb));
            i.addAll(removeLink(this.xx, this.yn, this.zn, bb));
            i.addAll(removeLink(this.xx, this.yn, this.zx, bb));
            i.addAll(removeLink(this.xx, this.yx, this.zn, bb));
            i.addAll(removeLink(this.xx, this.yx, this.zx, bb));
            return i;
        }

        public void makeLaser() {
            deleteLaser();
            byte flag = 0;
            final double a = 0.5, b = 0.45, c = 0.1;
            if (this.xn != this.xx)
                flag |= 1;
            if (this.yn != this.yx)
                flag |= 2;
            if (this.zn != this.zx)
                flag |= 4;
            if ((flag & 1) == 1)
                this.lasers[0] = new EntityLaser(this.w, this.xn + a, this.yn + b, this.zn + b, this.xx - this.xn, c, c, EntityLaser.RED_LASER);
            if ((flag & 2) == 2)
                this.lasers[4] = new EntityLaser(this.w, this.xn + b, this.yn + a, this.zn + b, c, this.yx - this.yn, c, EntityLaser.RED_LASER);
            if ((flag & 4) == 4)
                this.lasers[8] = new EntityLaser(this.w, this.xn + b, this.yn + b, this.zn + a, c, c, this.zx - this.zn, EntityLaser.RED_LASER);
            if ((flag & 3) == 3) {
                this.lasers[2] = new EntityLaser(this.w, this.xn + a, this.yx + b, this.zn + b, this.xx - this.xn, c, c, EntityLaser.RED_LASER);
                this.lasers[6] = new EntityLaser(this.w, this.xx + b, this.yn + a, this.zn + b, c, this.yx - this.yn, c, EntityLaser.RED_LASER);
            }
            if ((flag & 5) == 5) {
                this.lasers[1] = new EntityLaser(this.w, this.xn + a, this.yn + b, this.zx + b, this.xx - this.xn, c, c, EntityLaser.RED_LASER);
                this.lasers[9] = new EntityLaser(this.w, this.xx + b, this.yn + b, this.zn + a, c, c, this.zx - this.zn, EntityLaser.RED_LASER);
            }
            if ((flag & 6) == 6) {
                this.lasers[5] = new EntityLaser(this.w, this.xn + b, this.yn + a, this.zx + b, c, this.yx - this.yn, c, EntityLaser.RED_LASER);
                this.lasers[10] = new EntityLaser(this.w, this.xn + b, this.yx + b, this.zn + a, c, c, this.zx - this.zn, EntityLaser.RED_LASER);
            }
            if ((flag & 7) == 7) {
                this.lasers[3] = new EntityLaser(this.w, this.xn + a, this.yx + b, this.zx + b, this.xx - this.xn, c, c, EntityLaser.RED_LASER);
                this.lasers[7] = new EntityLaser(this.w, this.xx + b, this.yn + a, this.zx + b, c, this.yx - this.yn, c, EntityLaser.RED_LASER);
                this.lasers[11] = new EntityLaser(this.w, this.xx + b, this.yx + b, this.zn + a, c, c, this.zx - this.zn, EntityLaser.RED_LASER);
            }
            for (final EntityLaser eb : this.lasers)
                if (eb != null)
                    eb.getEntityWorld().spawnEntity(eb);
        }

        public void deleteLaser() {
            for (final EntityLaser eb : this.lasers)
                if (eb != null)
                    QuarryPlus.proxy.removeEntity(eb);
        }

        public BlockPos minPos() {
            return new BlockPos(xn, yn, zn);
        }

        public BlockPos maxPos() {
            return new BlockPos(xx, yx, zx);
        }

        @Override
        public String toString() {
            long i = Stream.of(lasers).filter(Objects::nonNull).count();
            return minPos() + " to " + maxPos() + " Lasers : " + i;
        }

        @Override
        public boolean equals(final Object o) {
           /* if (o instanceof BlockIndex) {
                final BlockIndex bi = (BlockIndex) o;
                return (bi.x == this.xn || bi.x == this.xx) && (bi.y == this.yn || bi.y == this.yx) && (bi.z == this.zn || bi.z == this.zx) && this.w == bi.w;
            }
            if (o instanceof TileEntity) {
                final TileEntity te = (TileEntity) o;
                return (te.getPos().equals(minPos()) || te.getPos().equals(maxPos())) && this.w == te.getWorld();
            }*/
            if (!(o instanceof Link))
                return false;
            final Link l = (Link) o;
            return l.xn == this.xn && l.xx == this.xx && l.yn == this.yn && l.yx == this.yx && l.zn == this.zn && l.zx == this.zx && l.w == this.w;
        }

        @Override
        public int hashCode() {
            return this.xn << 26 ^ this.xx << 21 ^ this.yn << 16 ^ this.yx << 11 ^ this.zn << 6 ^ this.zx;
        }
    }

    static {
        IndexOnlyList.TwoPredicate<Link> p1 = (link1, r) -> {
            if (r instanceof BlockIndex) {
                BlockIndex bi = ((BlockIndex) r);
                return (bi.x == link1.xn || bi.x == link1.xx) && (bi.y == link1.yn || bi.y == link1.yx) && (bi.z == link1.zn || bi.z == link1.zx) && link1.w == bi.w;
            }
            return false;
        };
        IndexOnlyList.TwoPredicate<Link> p2 = (link1, r) -> {
            if (r instanceof TileEntity) {
                final TileEntity te = (TileEntity) r;
                return (te.getPos().equals(link1.minPos()) || te.getPos().equals(link1.maxPos())) && link1.w == te.getWorld();
            }
            return false;
        };

        IndexOnlyList.TwoPredicate<Laser> p3 = (laser1, r) -> {
            if (r instanceof BlockIndex) {
                BlockIndex bi = ((BlockIndex) r);
                return bi.x == laser1.x && bi.y == laser1.y && bi.z == laser1.z && bi.w == laser1.w;
            }
            return false;
        };
        IndexOnlyList.TwoPredicate<Laser> p4 = (laser1, r) -> {
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
