package com.yogpc.qp.machines.marker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.IDebugSender;
import com.yogpc.qp.machines.base.IMarker;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.marker.LinkMessage;
import com.yogpc.qp.packet.marker.UpdateBoxMessage;
import com.yogpc.qp.render.Box;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import scala.Symbol;

import static jp.t2v.lab.syntax.MapStreamSyntax.streamCast;

public class TileMarker extends APacketTile implements IMarker, IDebugSender {
    private static final int MAX_SIZE = 256;
    public static final Symbol SYMBOL = Symbol.apply("MarkerPlus");

    public Link link = Link.DEFAULT;
    public Laser laser;

    public TileMarker() {
        super(Holder.markerTileType());
    }

    @Override
    public boolean hasLink() {
        return link.hasXLink() && link.hasZLink();
    }

    @Override
    public BlockPos min() {
        return link.min();
    }

    @Override
    public BlockPos max() {
        return link.max();
    }

    @Override
    public List<ItemStack> removeFromWorldWithItem() {
        // TODO IMPLEMENT
        return Collections.emptyList();
    }

    @Override
    public void onLoad() {
        laser = new Laser(getWorld(), getPos(), this.link);
    }

    @Override
    public void remove() {
        removeLink();
        super.remove();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        if (laser.hasLaser() || link.hasAnyLink()) {
            return 256 * 256 * 4;
        } else {
            return super.getMaxRenderDistanceSquared();
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    public void activated() {
        Link.updateLinks(getWorld(), getPos());
        if (!world.isRemote) {
            PacketHandler.sendToClient(LinkMessage.create(this), getWorld());
        }
        QuarryPlus.LOGGER.debug(String.format("Marker signal updated. Link: %s, Laser: %s", link, laser));
    }

    public void redstoneUpdate() {
        if (!world.isRemote) {
            PacketHandler.sendToClient(UpdateBoxMessage.create(this, world.isBlockPowered(pos)), getWorld());
        }
    }

    public void setLink(Link link) {
        this.link = link;
        this.laser = new Laser(getWorld(), getPos(), this.link);
    }

    public void removeLink() {
        this.link.edges()
            .map(world::getTileEntity)
            .flatMap(streamCast(TileMarker.class))
            .forEach(m -> m.setLink(Link.DEFAULT));
    }

    @Override
    public String getDebugName() {
        return TranslationKeys.marker;
    }

    @Override
    public List<? extends ITextComponent> getDebugMessages() {
        return Arrays.asList(new TextComponentString("Link : " + link),
            new TextComponentString("Laser : " + laser));
    }

    @Override
    public Symbol getSymbol() {
        return SYMBOL;
    }

    public static class Laser {
        /**
         * index x = 0, 3 , y = 1, 4 , z = 2, 5.
         */
        final AxisAlignedBB[] lasers = new AxisAlignedBB[6];
        @Nullable // Null in server world.
        public Box[] boxes;

        public Laser(World world, BlockPos pos, @Nullable Link definedLink) {
            int px = pos.getX(), py = pos.getY(), pz = pos.getZ();
            final double b = 10d / 16d, c = 6d / 16d;
            if (definedLink == null || !definedLink.hasXLink()) {
                lasers[0] = new AxisAlignedBB(px + b - MAX_SIZE, py + 0.5, pz + 0.5, px + c, py + 0.5, pz + 0.5);
                lasers[3] = new AxisAlignedBB(px + b, py + 0.5, pz + 0.5, px + c + MAX_SIZE, py + 0.5, pz + 0.5);
            }
            if (definedLink == null || !definedLink.hasYLink()) {
                lasers[1] = new AxisAlignedBB(px + 0.5, 0, pz + 0.5, px + 0.5, py - 0.1, pz + 0.5);
                lasers[4] = new AxisAlignedBB(px + 0.5, py + b, pz + 0.5, px + 0.5, 255, pz + 0.5);
            }
            if (definedLink == null || !definedLink.hasZLink()) {
                lasers[2] = new AxisAlignedBB(px + 0.5, py + 0.5, pz + b - MAX_SIZE, px + 0.5, py + 0.5, pz + c);
                lasers[5] = new AxisAlignedBB(px + 0.5, py + 0.5, pz + b, px + 0.5, py + 0.5, pz + c + MAX_SIZE);
            }

            boxUpdate(world, world.isBlockPowered(pos));
        }

        @Override
        public String toString() {
            long i = Stream.of(lasers).filter(Objects::nonNull).count();
            return "Lasers : " + i;
        }

        public boolean hasLaser() {
            return Stream.of(lasers).anyMatch(Objects::nonNull);
        }

        public void boxUpdate(World world, boolean on) {
            if (world.isRemote && Config.client().enableRender().get() && on) {
                boxes = Stream.of(lasers)
                    .filter(Objects::nonNull)
                    .map(range -> Box.apply(range, 1d / 8d, 1d / 8d, 1d / 8d, false, false))
                    .toArray(Box[]::new);
            } else {
                boxes = null;
            }
        }
    }

    public static class Link {
        public static final Link DEFAULT = new Link(0, 0, 0, 0, 0, 0) {
            @Override
            public boolean hasAnyLink() {
                return false;
            }

            @Override
            public BlockPos min() {
                return BlockPos.ORIGIN;
            }

            @Override
            public BlockPos max() {
                return BlockPos.ORIGIN;
            }

            @Override
            public Link setWorld(World world) {
                return this;
            }

            @Override
            public Stream<BlockPos> edges() {
                return Stream.empty();
            }

            @Override
            public String toString() {
                return "Link DEFAULT";
            }
        };
        private final boolean hasX;
        private final boolean hasY;
        private final boolean hasZ;

        public Link(int x1, int x2, int y1, int y2, int z1, int z2) {
            this.xMax = Math.max(x1, x2);
            this.xMin = Math.min(x1, x2);
            this.yMax = Math.max(y1, y2);
            this.yMin = Math.min(y1, y2);
            this.zMax = Math.max(z1, z2);
            this.zMin = Math.min(z1, z2);
            hasX = xMax != xMin;
            hasY = yMax != yMin;
            hasZ = zMax != zMin;
        }

        public Link(BlockPos pos) {
            this.xMax = pos.getX();
            this.xMin = pos.getX();
            this.yMax = pos.getY();
            this.yMin = pos.getY();
            this.zMax = pos.getZ();
            this.zMin = pos.getZ();
            hasX = xMax != xMin;
            hasY = yMax != yMin;
            hasZ = zMax != zMin;
        }

        public Link(int x1, int x2, int y1, int y2, int z1, int z2, World world) {
            this(x1, x2, y1, y2, z1, z2);

            int flag = 0;
            final double a = 0.5d, b = 10d / 16d, c = 6d / 16d;
            if (hasXLink())
                flag |= 1;
            if (hasYLink())
                flag |= 2;
            if (hasZLink())
                flag |= 4;
            if ((flag & 1) == 1) {//x
                lineBoxes[0] = new AxisAlignedBB(this.xMin + b, this.yMin + a, this.zMin + a, this.xMax + c, this.yMin + a, this.zMin + a);
            }
            if ((flag & 2) == 2) {//y
                lineBoxes[4] = new AxisAlignedBB(this.xMin + a, this.yMin + b, this.zMin + a, this.xMin + a, this.yMax + c, this.zMin + a);
            }
            if ((flag & 4) == 4) {//z
                lineBoxes[8] = new AxisAlignedBB(this.xMin + a, this.yMin + a, this.zMin + b, this.xMin + a, this.yMin + a, this.zMax + c);
            }
            if ((flag & 3) == 3) {//xy
                lineBoxes[2] = new AxisAlignedBB(this.xMin + b, this.yMax + a, this.zMin + a, this.xMax + c, this.yMax + a, this.zMin + a);
                lineBoxes[6] = new AxisAlignedBB(this.xMax + a, this.yMin + b, this.zMin + a, this.xMax + a, this.yMax + c, this.zMin + a);
            }
            if ((flag & 5) == 5) {//xz
                lineBoxes[1] = new AxisAlignedBB(this.xMin + b, this.yMin + a, this.zMax + a, this.xMax + c, this.yMin + a, this.zMax + a);
                lineBoxes[9] = new AxisAlignedBB(this.xMax + a, this.yMin + a, this.zMin + b, this.xMax + a, this.yMin + a, this.zMax + c);
            }
            if ((flag & 6) == 6) {//yz
                lineBoxes[5] = new AxisAlignedBB(this.xMin + a, this.yMin + b, this.zMax + a, this.xMin + a, this.yMax + c, this.zMax + a);
                lineBoxes[10] = new AxisAlignedBB(this.xMin + a, this.yMax + a, this.zMin + b, this.xMin + a, this.yMax + a, this.zMax + c);
            }
            if ((flag & 7) == 7) {//xyz
                lineBoxes[3] = new AxisAlignedBB(this.xMin + b, this.yMax + a, this.zMax + a, this.xMax + c, this.yMax + a, this.zMax + a);
                lineBoxes[7] = new AxisAlignedBB(this.xMax + a, this.yMin + b, this.zMax + a, this.xMax + a, this.yMax + c, this.zMax + a);
                lineBoxes[11] = new AxisAlignedBB(this.xMax + a, this.yMax + a, this.zMin + b, this.xMax + a, this.yMax + a, this.zMax + c);
            }
            if (world.isRemote && Config.client().enableRender().get()) {
                boxes = Stream.of(lineBoxes).filter(Objects::nonNull)
                    .map(range -> Box.apply(range, 1d / 8d, 1d / 8d, 1d / 8d, false, false))
                    .toArray(Box[]::new);
            } else {
                boxes = null;
            }
        }

        public final int xMax, xMin, yMax, yMin, zMax, zMin;
        public final AxisAlignedBB[] lineBoxes = new AxisAlignedBB[12];
        @Nullable // Null in server world.
        public Box[] boxes;

        boolean hasXLink() {
            return hasX;
        }

        boolean hasYLink() {
            return hasY;
        }

        boolean hasZLink() {
            return hasZ;
        }

        public boolean hasAnyLink() {
            return hasX || hasY || hasZ;
        }

        public BlockPos min() {
            return new BlockPos(xMin, yMin, zMin);
        }

        public BlockPos max() {
            return new BlockPos(xMax, yMax, zMax);
        }

        @Override
        public String toString() {
            long i = BooleanUtils.toInteger(hasX) + BooleanUtils.toInteger(hasY) + BooleanUtils.toInteger(hasZ);
            return min() + " to " + max() + " Lasers : " + i * 2;
        }

        public Link setWorld(World world) {
            return new Link(xMax, xMin, yMax, yMin, zMax, zMin, world);
        }

        @SuppressWarnings("SuspiciousNameCombination")
        public Stream<BlockPos> edges() {
            return Stream.of(xMin, xMax)
                .flatMap(x -> Stream.of(Pair.of(x, yMin), Pair.of(x, yMax)))
                .flatMap(xy -> Stream.of(new BlockPos(xy.getLeft(), xy.getRight(), zMin), new BlockPos(xy.getLeft(), xy.getRight(), zMax)))
                .distinct();
        }

        public static Link of(int x1, int x2, int y1, int y2, int z1, int z2) {
            if (x1 == 0 && x2 == 0 && y1 == 0 && y2 == 0 && z1 == 0 && z2 == 0) return DEFAULT;
            else return new Link(x1, x2, y1, y2, z1, z2);
        }

        /**
         * Make new link and set it to all markers in the edges.
         */
        public static void updateLinks(World world, BlockPos originPos) {
            Link newLink = searchInternal(world, originPos, new Link(originPos)).setWorld(world);
            newLink.edges()
                .map(world::getTileEntity)
                .flatMap(streamCast(TileMarker.class))
                .forEach(m -> m.setLink(newLink));
        }

        private static Link searchInternal(World world, BlockPos originPos, Link link) {
            int xOffsetTemp = 0;
            int yOffsetTemp = 0;
            int zOffsetTemp = 0;
            int nXMax = link.xMax, nXMin = link.xMin, nYMax = link.yMax, nYMin = link.yMin, nZMax = link.zMax, nZMin = link.zMin;
            if (!link.hasXLink()) {
                // Search X
                for (int i = 1; i < MAX_SIZE; i++) {
                    TileEntity tile = world.getTileEntity(originPos.add(i, 0, 0));
                    if (tile instanceof TileMarker && !((TileMarker) tile).hasLink()) {
                        xOffsetTemp = i;
                        nXMax += i;
                        break;
                    }
                    tile = world.getTileEntity(originPos.add(-i, 0, 0));
                    if (tile instanceof TileMarker && !((TileMarker) tile).hasLink()) {
                        xOffsetTemp = -i;
                        nXMin -= i;
                        break;
                    }
                }
            }
            if (!link.hasYLink()) {
                // Search Y
                for (int i = 1; i < MAX_SIZE; i++) {
                    TileEntity tile = world.getTileEntity(originPos.add(0, i, 0));
                    if (tile instanceof TileMarker && !((TileMarker) tile).link.hasYLink()) {
                        yOffsetTemp = i;
                        nYMax += i;
                        break;
                    }
                    tile = world.getTileEntity(originPos.add(0, -i, 0));
                    if (tile instanceof TileMarker && !((TileMarker) tile).link.hasYLink()) {
                        yOffsetTemp = -i;
                        nYMin -= i;
                        break;
                    }
                }
            }
            if (!link.hasZLink()) {
                // Search Z
                for (int i = 1; i < MAX_SIZE; i++) {
                    TileEntity tile = world.getTileEntity(originPos.add(0, 0, i));
                    if (tile instanceof TileMarker && !((TileMarker) tile).hasLink()) {
                        nZMax += i;
                        zOffsetTemp = i;
                        break;
                    }
                    tile = world.getTileEntity(originPos.add(0, 0, -i));
                    if (tile instanceof TileMarker && !((TileMarker) tile).hasLink()) {
                        zOffsetTemp = -i;
                        nZMin -= i;
                        break;
                    }
                }
            }
            final int xOffset = xOffsetTemp;
            final int yOffset = yOffsetTemp;
            final int zOffset = zOffsetTemp;
            return Optional.of(of(nXMin, nXMax, nYMax, nYMin, nZMax, nZMin))
                .map(link1 -> {
                    if (!link1.hasXLink() && yOffset != 0) {
                        return searchInternal(world, originPos.add(0, yOffset, 0), link1);
                    }
                    return link1;
                })
                .map(link1 -> {
                    if (!link1.hasXLink() && zOffset != 0) {
                        return searchInternal(world, originPos.add(0, 0, zOffset), link1);
                    }
                    return link1;
                })
                .map(link1 -> {
                    if (!link1.hasYLink() && xOffset != 0) {
                        return searchInternal(world, originPos.add(xOffset, 0, 0), link1);
                    }
                    return link1;
                })
                .map(link1 -> {
                    if (!link1.hasYLink() && zOffset != 0) {
                        return searchInternal(world, originPos.add(0, 0, zOffset), link1);
                    }
                    return link1;
                })
                .map(link1 -> {
                    if (!link1.hasZLink() && xOffset != 0) {
                        return searchInternal(world, originPos.add(xOffset, 0, 0), link1);
                    }
                    return link1;
                })
                .map(link1 -> {
                    if (!link1.hasZLink() && yOffset != 0) {
                        return searchInternal(world, originPos.add(0, yOffset, 0), link1);
                    }
                    return link1;
                })
                .filter(Link::hasAnyLink)
                .orElse(DEFAULT);
        }
    }
}
