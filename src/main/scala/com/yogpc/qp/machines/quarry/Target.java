package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.filler.FillerAction;
import com.yogpc.qp.machines.filler.FillerTargetPosIterator;
import com.yogpc.qp.machines.filler.SkipIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Target {
    static boolean THROW_IF_INVALID_NBT = !FMLEnvironment.production;
    private final Set<BlockPos> skippedPoses = new HashSet<>();

    @Nullable
    public abstract BlockPos get(boolean goNext);

    public final BlockPos goNextAndGet() {
        get(true);  // Go next.
        return get(false);  // Fetch the next pos.
    }

    @NotNull
    public abstract Stream<BlockPos> allPoses();

    @NotNull
    protected abstract CompoundTag toNbt();

    public static CompoundTag toNbt(Target target) {
        var tag = target.toNbt();
        tag.putString("target", target.getClass().getSimpleName());
        return tag;
    }

    public void addSkipped(BlockPos pos) {
        skippedPoses.add(pos);
    }

    public boolean alreadySkipped(BlockPos pos) {
        return skippedPoses.contains(pos);
    }

    public abstract double progress();

    public static Target fromNbt(CompoundTag tag) {
        return switch (tag.getString("target")) {
            case "DigTarget" -> DigTarget.from(tag);
            case "FrameTarget" -> FrameTarget.from(tag);
            case "PosesTarget" -> PosesTarget.from(tag);
            case "FrameInsideTarget" -> FrameInsideTarget.from(tag);
            case "FillerTarget" -> FillerTarget.from(tag);
            default -> {
                if (THROW_IF_INVALID_NBT) {
                    throw new IllegalArgumentException("Invalid target nbt. " + tag);
                } else {
                    QuarryPlus.LOGGER.error("Invalid target nbt in Quarry Target. %s".formatted(tag));
                    yield null;
                }
            }
        };
    }

    public static Target newFrameTarget(Area area) {
        return new FrameTarget(area);
    }

    public static Target newDigTarget(Area area, int y) {
        return new DigTarget(area, y);
    }

    public static Target newFrameInside(Area area, int minY, int maxY) {
        return new FrameInsideTarget(area, minY, maxY);
    }

    public static Target newFillerTarget(Area area) {
        return new FillerTarget(area);
    }

    @Nullable
    public static Target nextY(@Nullable Target previous, Area area, int digMinY) {
        if (previous instanceof DigTarget digTarget) {
            int nextY = digTarget.y - 1;
            if (digMinY < nextY && nextY <= area.maxY()) {
                return newDigTarget(digTarget.area, nextY);
            } else {
                return null;
            }
        } else if (previous instanceof PosesTarget posesTarget) {
            int nextY = posesTarget.allPoses().mapToInt(BlockPos::getY).max().orElse(area.minY()) - 1;
            if (digMinY < nextY && nextY <= area.maxY()) {
                return newDigTarget(area, nextY);
            } else {
                return null;
            }
        } else {
            return newDigTarget(area, area.minY());
        }
    }

    @NotNull
    public static Target poses(List<BlockPos> pos) {
        return new PosesTarget(pos);
    }
}

final class DigTarget extends Target {
    final Area area;
    final int y;
    @Nullable
    private BlockPos.MutableBlockPos currentTarget;

    DigTarget(Area area, int y) {
        this.area = area;
        this.y = y;
        var x = y % 2 == 0 ? area.minX() + 1 : area.maxX() - 1;
        currentTarget = new BlockPos.MutableBlockPos(
                x, y, initZ(x, this.y, area.minZ() + 1, area.maxZ() - 1)
        );
    }

    @Override
    @Nullable
    public BlockPos get(boolean goNext) {
        if (currentTarget == null) return null;
        var pre = currentTarget.immutable();
        if (goNext) {
            var nextZ = pre.getX() % 2 == 0 ^ this.y % 2 == 0 ? pre.getZ() + 1 : pre.getZ() - 1;
            if (area.minZ() < nextZ && nextZ < area.maxZ()) {
                currentTarget.set(pre.getX(), pre.getY(), nextZ);
            } else {
                // change x
                var nextX = this.y % 2 == 0 ? pre.getX() + 1 : pre.getX() - 1;
                if (area.minX() < nextX && nextX < area.maxX()) {
                    currentTarget.set(nextX, pre.getY(), initZ(nextX, this.y, area.minZ() + 1, area.maxZ() - 1));
                } else {
                    // Finished this y
                    currentTarget = null;
                    return null;
                }
            }
        }
        return pre;
    }

    @Override
    @NotNull
    public Stream<BlockPos> allPoses() {
        return BlockPos.betweenClosedStream(area.minX(), y, area.minZ(), area.maxX(), y, area.maxZ());
    }

    @Override
    @NotNull
    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("area", area.toNBT());
        tag.putInt("y", y);
        if (currentTarget != null) tag.putLong("currentTarget", currentTarget.asLong());
        return tag;
    }

    @Override
    public double progress() {
        if (currentTarget == null) return 1d;
        double xUnit = 1d / (area.maxX() - area.minX() - 1);
        double xProgress;
        if (y % 2 == 0) {
            // move to plus
            xProgress = xUnit * (currentTarget.getX() - area.minX() - 1);
        } else {
            xProgress = xUnit * (area.maxX() - currentTarget.getX() - 1);
        }
        return xProgress;
    }

    static DigTarget from(CompoundTag tag) {
        var target = new DigTarget(Area.fromNBT(tag.getCompound("area")).orElseThrow(), tag.getInt("y"));
        if (tag.contains("currentTarget")) {
            assert target.currentTarget != null;
            target.currentTarget.set(tag.getLong("currentTarget"));
        } else {
            target.currentTarget = null;
        }
        return target;
    }

    static int initZ(int x, int y, int minZ, int maxZ) {
        return (x % 2 == 0 ^ y % 2 == 0) ? minZ : maxZ;
    }

    @Override
    public String toString() {
        return "DigTarget{" +
                "area=" + area +
                ", y=" + y +
                ", currentTarget=" + currentTarget +
                '}';
    }
}

final class FrameTarget extends Target {
    final Area area;
    private final Iterator<BlockPos> iterator;
    @Nullable
    private BlockPos currentTarget;

    FrameTarget(Area area) {
        this.area = area;
        this.iterator = Area.getFramePosStream(area).iterator();
        this.currentTarget = iterator.hasNext() ? iterator.next() : null;
    }

    FrameTarget(Area area, BlockPos pre) {
        this.area = area;
        this.iterator = Area.getFramePosStream(area)
                .dropWhile(p -> !p.equals(pre)).iterator();
        this.currentTarget = iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    @Nullable
    public BlockPos get(boolean goNext) {
        var pre = currentTarget;
        if (goNext) {
            if (iterator.hasNext()) {
                currentTarget = iterator.next();
            } else {
                currentTarget = null;
            }
        }
        return pre;
    }

    @Override
    @NotNull
    public Stream<BlockPos> allPoses() {
        return Area.getFramePosStream(area);
    }

    @Override
    @NotNull
    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("area", area.toNBT());
        tag.putLong("currentTarget", Objects.requireNonNullElse(currentTarget, new BlockPos(area.minX(), area.maxY(), area.minZ() + 1)).asLong());

        return tag;
    }

    @Override
    public double progress() {
        if (currentTarget == null) return 1d;
        var list = Area.getFramePosStream(area).toList();
        var index = list.indexOf(currentTarget);
        return (double) index / list.size();
    }

    static FrameTarget from(CompoundTag tag) {
        return new FrameTarget(Area.fromNBT(tag.getCompound("area")).orElseThrow(), BlockPos.of(tag.getLong("currentTarget")));
    }

    @Override
    public String toString() {
        return "FrameTarget{" +
                "area=" + area +
                ", currentTarget=" + currentTarget +
                ", hasNext=" + iterator.hasNext() +
                '}';
    }
}

final class PosesTarget extends Target {
    private final List<BlockPos> posList;
    private final Iterator<BlockPos> iterator;
    @Nullable
    private BlockPos currentTarget;

    PosesTarget(List<BlockPos> posList) {
        this.posList = posList;
        iterator = posList.iterator();
        if (iterator.hasNext())
            currentTarget = iterator.next();
    }

    static PosesTarget from(CompoundTag tag) {
        var poses = Arrays.stream(tag.getLongArray("poses")).mapToObj(BlockPos::of).toList();
        return new PosesTarget(poses);
    }

    @Override
    @Nullable
    public BlockPos get(boolean goNext) {
        var pre = currentTarget;
        if (goNext) {
            if (iterator.hasNext()) {
                currentTarget = iterator.next();
            } else {
                currentTarget = null;
            }
        }
        return pre;
    }

    @Override
    @NotNull
    public Stream<BlockPos> allPoses() {
        return posList.stream();
    }

    @Override
    public @NotNull
    CompoundTag toNbt() {
        var tag = new CompoundTag();
        var list = new LongArrayTag(allPoses().mapToLong(BlockPos::asLong).toArray());
        tag.put("poses", list);
        return tag;
    }

    @Override
    public double progress() {
        if (currentTarget == null) return 1d;
        return (double) posList.indexOf(currentTarget) / posList.size();
    }

    @Override
    public String toString() {
        return "PosesTarget{" +
                "currentTarget=" + currentTarget +
                ", size=" + posList.size() +
                '}';
    }
}

final class FrameInsideTarget extends Target {
    private final Area area;
    private final int minY;
    private final int maxY;
    private int index = 0;

    FrameInsideTarget(Area area, int minY, int maxY) {
        this.area = area;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    @Nullable
    public BlockPos get(boolean goNext) {
        int xSize = area.maxX() - area.minX() - 1;
        int zSize = area.maxZ() - area.minZ() - 1;
        var areaSize = xSize * zSize;
        int y = maxY - index / areaSize;
        if (y < minY) return null;
        int xz = index % areaSize;
        int x = area.minX() + 1 + xz / zSize;
        int z = area.minZ() + 1 + xz % zSize;
        if (goNext) index++;
        return new BlockPos(x, y, z);
    }

    @Override
    @NotNull
    public Stream<BlockPos> allPoses() {
        return BlockPos.betweenClosedStream(area.minX() + 1, minY, area.minZ() + 1,
                area.maxX() - 1, maxY, area.maxZ() - 1);
    }

    @Override
    @NotNull
    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("area", area.toNBT());
        tag.putInt("minY", minY);
        tag.putInt("maxY", maxY);
        tag.putInt("index", index);
        return tag;
    }

    @Override
    public double progress() {
        int xSize = area.maxX() - area.minX() - 1;
        int zSize = area.maxZ() - area.minZ() - 1;
        var areaSize = xSize * zSize;
        int xz = index % areaSize;
        return (double) xz / areaSize;
    }

    public static FrameInsideTarget from(CompoundTag tag) {
        var area = Area.fromNBT(tag.getCompound("area")).orElseThrow();
        var minY = tag.getInt("minY");
        var maxY = tag.getInt("maxY");
        var index = tag.getInt("index");
        var t = new FrameInsideTarget(area, minY, maxY);
        t.index = index;
        return t;
    }

    @Override
    public String toString() {
        return "FrameInsideTarget{" +
                "area=" + area +
                ", minY=" + minY +
                ", maxY=" + maxY +
                ", index=" + index +
                '}';
    }
}

final class FillerTarget extends Target {
    private final Area area;
    final FillerAction fillerAction;

    FillerTarget(Area area) {
        this.area = area;
        this.fillerAction = new FillerAction();
        this.fillerAction.setIterator(new SkipIterator(area, FillerTargetPosIterator::box));
    }

    public static FillerTarget from(CompoundTag tag) {
        var area = Area.fromNBT(tag.getCompound("area")).orElseThrow();
        var target = new FillerTarget(area);
        target.fillerAction.fromNbt(tag.getCompound("fillerAction"));
        return target;
    }

    /**
     * Dummy method. Don't use.
     *
     * @deprecated
     */
    @Override
    @Deprecated
    public @Nullable BlockPos get(boolean goNext) {
        return null;
    }

    @Override
    public @NotNull Stream<BlockPos> allPoses() {
        var itr = FillerTargetPosIterator.box(area);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(itr, 0), false);
    }

    @Override
    protected @NotNull CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("area", area.toNBT());
        tag.put("fillerAction", fillerAction.toNbt());
        return tag;
    }

    @Override
    public double progress() {
        return 0;
    }

    @Override
    public String toString() {
        return "FillerTarget{" +
            "area=" + area +
            '}';
    }
}
