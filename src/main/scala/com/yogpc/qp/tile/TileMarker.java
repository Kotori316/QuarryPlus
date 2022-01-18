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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.lib.misc.PositionUtil;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.marker.LinkReply;
import com.yogpc.qp.packet.marker.LinkUpdate;
import com.yogpc.qp.render.Box;
import javax.annotation.Nullable;
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Symbol;

@net.minecraftforge.fml.common.Optional.Interface(iface = "buildcraft.api.tiles.ITileAreaProvider", modid = QuarryPlus.Optionals.Buildcraft_tiles)
public class TileMarker extends APacketTile implements ITileAreaProvider, IChunkLoadTile, IDebugSender, IMarker {
    private static final int MAX_SIZE = 256;
    public static final Symbol SYMBOL = Symbol.apply("MarkerPlus");
    private final boolean bcLoaded;

    public TileMarker() {
        // TODO change to net.minecraftforge.fml.common.ModAPIManager
        this.bcLoaded = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_tiles);
    }

    @Override
    protected Symbol getSymbol() {
        return SYMBOL;
    }

    @Nullable
    public Link link;
    @Nullable
    public Laser laser;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (this.laser != null)
            compound.setLong("laser", this.laser.getLaserPos().toLong());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("laser") && this.world != null) {
            this.laser = new Laser(BlockPos.fromLong(compound.getLong("laser")), this.link, this.world.isRemote);
        }
    }

    @Override
    public boolean hasLink() {
        return link != null && link.xMin != link.xMax && link.zMin != link.zMax;
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
            this.getBlockType().dropBlockAsItem(getWorld(), getPos(), getWorld().getBlockState(getPos()), 0);
            getWorld().setBlockToAir(getPos());
            return;
        }
        final List<ItemStack> al = this.link.removeAndGetItem(getWorld());
        for (final ItemStack is : al) {
            InventoryHelper.spawnItemStack(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), is);
        }
    }

    @Override
    public List<ItemStack> removeFromWorldWithItem() {
        if (this.link != null)
            return this.link.removeAndGetItem(getWorld());
        NonNullList<ItemStack> ret = NonNullList.create();
        QuarryPlusI.blockMarker().getDrops(ret, getWorld(), getPos(), getWorld().getBlockState(getPos()), 0);
        getWorld().setBlockToAir(getPos());
        return ret;
    }

    private static Optional<Link> S_renewConnection(World world, BlockPos basePos, boolean first) {
        @Nullable
        TileMarker xMarker = IntStream.range(1, MAX_SIZE).flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> basePos.add(i, 0, 0))
            .map(world::getTileEntity)
            .flatMap(MapStreamSyntax.streamCast(TileMarker.class))
            .findFirst().orElse(null);
        @Nullable
        TileMarker yMarker = IntStream.range(1, MAX_SIZE).flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> basePos.add(0, i, 0))
            .filter(world::isValid)
            .map(world::getTileEntity)
            .flatMap(MapStreamSyntax.streamCast(TileMarker.class))
            .findFirst().orElse(null);
        @Nullable
        TileMarker zMarker = IntStream.range(1, MAX_SIZE).flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> basePos.add(0, 0, i))
            .map(world::getTileEntity)
            .flatMap(MapStreamSyntax.streamCast(TileMarker.class))
            .findFirst().orElse(null);
        if (xMarker != null && zMarker != null) {
            // Create link!
            if (yMarker != null) {
                // Consider Y
                int yMin = Math.min(xMarker.getPos().getY(), yMarker.getPos().getY());
                int yMax = Math.max(xMarker.getPos().getY(), yMarker.getPos().getY());
                BlockPos xPos = new BlockPos(xMarker.getPos().getX(), yMin, xMarker.getPos().getZ());
                BlockPos zPos = new BlockPos(zMarker.getPos().getX(), yMax, zMarker.getPos().getZ());
                return Optional.of(new Link(xPos, zPos, true));
            } else {
                return Optional.of(new Link(xMarker.getPos(), zMarker.getPos(), true));
            }
        } else if (first) {
            // Check x
            Optional<Link> x = Optional.ofNullable(xMarker).flatMap(m -> S_renewConnection(world, m.getPos(), false));
            if (x.isPresent()) return x;
            // Check z
            return Optional.ofNullable(zMarker).flatMap(m -> S_renewConnection(world, m.getPos(), false));
        } else {
            return Optional.empty();
        }
    }

    public void G_updateSignal() {
        if (this.laser != null) {
            this.laser = null;
        }
        if (!this.getWorld().isRemote && !Config.content().disableMapJ().get(TileMarker.SYMBOL)) {
            if (getWorld().isBlockPowered(getPos()) && (this.link == null ||
                this.link.xMin == this.link.xMax || this.link.yMin == this.link.yMax || this.link.zMin == this.link.zMax)) {
                //create
                this.laser = new Laser(getPos(), this.link, false);
                PacketHandler.sendToAround(LinkUpdate.create(this, true), getWorld(), getPos());
            } else {
                //remove
                PacketHandler.sendToAround(LinkUpdate.create(this, false), getWorld(), getPos());
            }
        }
    }

    public void S_tryConnection() { // onBlockActivated
        if (this.link != null) {
            // Remove markers' link related to this one.
            this.link.removeLink(getWorld());
        }
        S_renewConnection(getWorld(), getPos(), true).ifPresent(this::setLink);
    }

    void setLink(Link parentLink) {
        this.link = parentLink;
        Link childLink = link.noRender();
        link.edgePoses()
            .filter(Predicate.isEqual(getPos()).negate())
            .map(getWorld()::getTileEntity)
            .flatMap(MapStreamSyntax.streamCast(TileMarker.class))
            .forEach(marker -> marker.link = childLink);
        PacketHandler.sendToAround(LinkReply.create(getPos(), parentLink), getWorld(), getPos());
        G_updateSignal();
    }

    void G_destroy() {
        if (world != null && !world.isRemote) {
            if (this.link != null)
                this.link.removeLink(getWorld());
        }
        if (this.laser != null)
            this.laser = null;
        ForgeChunkManager.releaseTicket(this.chunkTicket);
    }

    private Ticket chunkTicket;

    @Override
    public void requestTicket() {// onPostBlockPlaced
        if (this.chunkTicket != null)
            return;
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, this.getWorld(), Type.NORMAL);
        setTileData(this.chunkTicket, getPos());
    }

    @Override
    public void forceChunkLoading(final Ticket ticket) {// ticketsLoaded
        if (this.chunkTicket == null)
            this.chunkTicket = ticket;
        ForgeChunkManager.forceChunk(ticket, new ChunkPos(getPos()));
    }

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
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getWorld().getBlockState(getPos()).getBlock() != QuarryPlusI.blockMarker())
            G_destroy();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
    }

    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = QuarryPlus.Optionals.BuildCraft_core)
    public boolean isValidFromLocation(BlockPos pos) {
        if (link != null) {
            boolean xFlag = link.xMin <= pos.getX() && link.xMax <= pos.getX();
            boolean yFlag = link.yMin <= pos.getY() && link.yMax <= pos.getY();
            boolean zFlag = link.zMin <= pos.getZ() && link.zMax <= pos.getZ();
            if (xFlag && yFlag && zFlag) {
                return false;
            }
            Predicate<BlockPos> predicate = p -> PositionUtil.isNextTo(p, pos);
            return PositionUtil.getCorners(min(), max()).stream().anyMatch(predicate);
        }
        return false;
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.marker;
    }

    @Override
    public List<? extends ITextComponent> getDebugMessages() {
        return Arrays.asList(new TextComponentString("Link : " + link),
            new TextComponentString("Laser : " + laser),
            new TextComponentString("0x" + Integer.toHexString(this.hashCode())));
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
        final int x, y, z;
        /**
         * index 0=x , 1=y , 2=z
         */
        public final AxisAlignedBB[] lineBoxes = new AxisAlignedBB[6];
        @Nullable
        public final Box[] boxes;

        public Laser(BlockPos pos, Link link, boolean isRemote) {
            this(pos.getX(), pos.getY(), pos.getZ(), link, isRemote);
        }

        private Laser(final int px, final int py, final int pz, final @Nullable Link l, boolean isRemote) {
            this.x = px;
            this.y = py;
            this.z = pz;
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
            if (isRemote && !Config.content().disableRendering()) {
                boxes = Arrays.stream(lineBoxes).filter(nonNull)
                    .map(range -> Box.apply(range, 1d / 8d, 1d / 8d, 1d / 8d, false, false))
                    .toArray(Box[]::new);
            } else {
                //Server
                boxes = null;
            }
        }

        @Override
        public String toString() {
            long i = Stream.of(lineBoxes).filter(nonNull).count();
            return x + " " + y + " " + z + " Lasers : " + i;
        }

        BlockPos getLaserPos() {
            return new BlockPos(x, y, z);
        }
    }

    /**
     * Link with other markers.
     */
    public static class Link {
        public final int xMax, xMin, yMax, yMin, zMax, zMin;
        @Nullable // Null in server world.
        public Box[] boxes;
        public final boolean shouldBeRendered;

        public Link(int xMax, int xMin, int yMax, int yMin, int zMax, int zMin, boolean shouldBeRendered) {
            this.xMax = xMax;
            this.xMin = xMin;
            this.yMax = yMax;
            this.yMin = yMin;
            this.zMax = zMax;
            this.zMin = zMin;
            this.shouldBeRendered = shouldBeRendered;
        }

        public Link(BlockPos pos1, BlockPos pos2, boolean shouldBeRendered) {
            this.xMin = Math.min(pos1.getX(), pos2.getX());
            this.yMin = Math.min(pos1.getY(), pos2.getY());
            this.zMin = Math.min(pos1.getZ(), pos2.getZ());
            this.xMax = Math.max(pos1.getX(), pos2.getX());
            this.yMax = Math.max(pos1.getY(), pos2.getY());
            this.zMax = Math.max(pos1.getZ(), pos2.getZ());
            this.shouldBeRendered = shouldBeRendered;
        }

        public Link(NBTTagCompound compound) {
            BlockPos max = BlockPos.fromLong(compound.getLong("maxPos"));
            BlockPos min = BlockPos.fromLong(compound.getLong("minPos"));
            this.shouldBeRendered = compound.getBoolean("shouldBeRendered");
            this.xMax = max.getX();
            this.yMax = max.getY();
            this.zMax = max.getZ();
            this.xMin = min.getX();
            this.yMin = min.getY();
            this.zMin = min.getZ();
        }

        public void makeLaser(boolean isRemote) {
            if (!isRemote || !shouldBeRendered) return;
            byte flag = 0;
            final double a = 0.5d, b = 10d / 16d, c = 6d / 16d;
            AxisAlignedBB[] lineBoxes = new AxisAlignedBB[12];
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
            boxes = Arrays.stream(lineBoxes).filter(nonNull)
                .map(range -> Box.apply(range, 1d / 8d, 1d / 8d, 1d / 8d, false, false))
                .toArray(Box[]::new);
        }

        public BlockPos minPos() {
            return new BlockPos(xMin, yMin, zMin);
        }

        public BlockPos maxPos() {
            return new BlockPos(xMax, yMax, zMax);
        }

        Stream<BlockPos> edgePoses() {
            return Stream.of(minPos(),
                new BlockPos(xMin, yMin, zMax),
                new BlockPos(xMin, yMax, zMin),
                new BlockPos(xMin, yMax, zMax),
                new BlockPos(xMax, yMin, zMin),
                new BlockPos(xMax, yMin, zMax),
                new BlockPos(xMax, yMax, zMin),
                maxPos());
        }

        Link noRender() {
            return new Link(xMax, xMin, yMax, yMin, zMax, zMin, false);
        }

        /**
         * Remove all link related to this one.
         * Must be called only in server.
         *
         * @param world the world where this link exists.
         */
        public void removeLink(World world) {
            edgePoses().map(world::getTileEntity)
                .flatMap(MapStreamSyntax.streamCast(TileMarker.class))
                .forEach(marker -> {
                    marker.link = null;
                    PacketHandler.sendToAround(LinkReply.create(marker.getPos(), null), marker.getWorld(), marker.getPos());
                });
        }

        List<ItemStack> removeAndGetItem(World world) {
            List<TileMarker> markers = edgePoses().distinct().map(world::getTileEntity)
                .flatMap(MapStreamSyntax.streamCast(TileMarker.class))
                .collect(Collectors.toList());
            NonNullList<ItemStack> stacks = NonNullList.create();
            markers.forEach(m -> m.getBlockType().getDrops(stacks, world, m.getPos(), world.getBlockState(m.getPos()), 0));
            markers.stream().map(TileMarker::getPos).forEach(world::setBlockToAir);
            return stacks;
        }

        public NBTTagCompound toNbt() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong("maxPos", maxPos().toLong());
            compound.setLong("minPos", minPos().toLong());
            compound.setBoolean("shouldBeRendered", shouldBeRendered);
            return compound;
        }

        @Override
        public String toString() {
            return String.format("Link{(%d, %d, %d) -> (%d, %d, %d), shouldBeRendered=%b}",
                xMin, yMin, zMin, xMax, yMax, zMax, shouldBeRendered);
        }
    }

}
