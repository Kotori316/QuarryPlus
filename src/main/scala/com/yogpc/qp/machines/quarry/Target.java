package com.yogpc.qp.machines.quarry;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.machines.Area;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Target {
    @Nullable
    public abstract BlockPos get(boolean goNext);

    public final BlockPos goNextAndGet() {
        get(true);  // Go next.
        return get(false);  // Fetch the next pos.
    }

    @NotNull
    public abstract Stream<BlockPos> allPoses();

    @NotNull
    public abstract NbtCompound toNbt();

    public static Target fromNbt(NbtCompound tag) {
        return switch (tag.getString("target")) {
            case "DigTarget" -> DigTarget.from(tag);
            case "FrameTarget" -> FrameTarget.from(tag);
            case "PosesTarget" -> PosesTarget.from(tag);
            default -> throw new IllegalArgumentException("Invalid target nbt. " + tag);
        };
    }

    public static Target newFrameTarget(Area area) {
        return new FrameTarget(area);
    }

    public static Target newDigTarget(Area area, int y) {
        return new DigTarget(area, y);
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
    private BlockPos.Mutable currentTarget;

    DigTarget(Area area, int y) {
        this.area = area;
        this.y = y;
        var x = y % 2 == 0 ? area.minX() + 1 : area.maxX() - 1;
        currentTarget = new BlockPos.Mutable(
            x, y, initZ(x, this.y, area.minZ() + 1, area.maxZ() - 1)
        );
    }

    @Override
    @Nullable
    public BlockPos get(boolean goNext) {
        if (currentTarget == null) return null;
        var pre = currentTarget.toImmutable();
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
    public @NotNull Stream<BlockPos> allPoses() {
        return BlockPos.stream(area.minX(), y, area.minZ(), area.maxX(), y, area.maxZ());
    }

    @Override
    @NotNull
    public NbtCompound toNbt() {
        var tag = new NbtCompound();
        tag.putString("target", getClass().getSimpleName());
        tag.put("area", area.toNBT());
        tag.putInt("y", y);
        if (currentTarget != null) tag.putLong("currentTarget", currentTarget.asLong());
        return tag;
    }

    static DigTarget from(NbtCompound tag) {
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
        this.iterator = getPosStream(area).iterator();
        this.currentTarget = iterator.hasNext() ? iterator.next() : null;
    }

    FrameTarget(Area area, BlockPos pre) {
        this.area = area;
        this.iterator = getPosStream(area)
            .dropWhile(p -> !p.equals(pre)).iterator();
        this.currentTarget = iterator.hasNext() ? iterator.next() : null;
    }

    @NotNull
    private Stream<BlockPos> getPosStream(Area area) {
        return Stream.of(
            makeSquare(area, area.minY()),
            makePole(area, area.minY() + 1, area.maxY()),
            makeSquare(area, area.maxY())
        ).flatMap(Function.identity());
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
        return getPosStream(area);
    }

    @Override
    @NotNull
    public NbtCompound toNbt() {
        var tag = new NbtCompound();
        tag.putString("target", getClass().getSimpleName());
        tag.put("area", area.toNBT());
        tag.putLong("currentTarget", Objects.requireNonNullElse(currentTarget, new BlockPos(area.minX(), area.maxY(), area.minZ() + 1)).asLong());

        return tag;
    }

    static FrameTarget from(NbtCompound tag) {
        return new FrameTarget(Area.fromNBT(tag.getCompound("area")).orElseThrow(), BlockPos.fromLong(tag.getLong("currentTarget")));
    }

    static Stream<BlockPos> makeSquare(Area area, int y) {
        return Stream.of(
            IntStream.rangeClosed(area.minX(), area.maxX()).mapToObj(x -> new BlockPos(x, y, area.minZ())), // minX -> maxX, minZ
            IntStream.rangeClosed(area.minZ(), area.maxZ()).mapToObj(z -> new BlockPos(area.maxX(), y, z)), // maxX, minZ -> maxZ
            IntStream.iterate(area.maxX(), x -> x >= area.minX(), x -> x - 1).mapToObj(x -> new BlockPos(x, y, area.maxZ())), // maxX -> minX, maxZ
            IntStream.iterate(area.maxZ(), z -> z >= area.minZ(), z -> z - 1).mapToObj(z -> new BlockPos(area.minX(), y, z)) // minX, maxZ -> minZ
        ).flatMap(Function.identity());
    }

    static Stream<BlockPos> makePole(Area area, int yMin, int yMax) {
        return IntStream.rangeClosed(yMin, yMax).boxed().flatMap(y ->
            Stream.of(
                new BlockPos(area.minX(), y, area.minZ()),
                new BlockPos(area.maxX(), y, area.minZ()),
                new BlockPos(area.maxX(), y, area.maxZ()),
                new BlockPos(area.minX(), y, area.maxZ())));
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

    public static PosesTarget from(NbtCompound tag) {
        var poses = Arrays.stream(tag.getLongArray("poses")).mapToObj(BlockPos::fromLong).toList();
        return new PosesTarget(poses);
    }

    @Override
    public @Nullable BlockPos get(boolean goNext) {
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
    public @NotNull Stream<BlockPos> allPoses() {
        return posList.stream();
    }

    @Override
    public @NotNull NbtCompound toNbt() {
        var tag = new NbtCompound();
        tag.putString("target", getClass().getSimpleName());
        var list = new NbtLongArray(allPoses().mapToLong(BlockPos::asLong).toArray());
        tag.put("poses", list);
        return tag;
    }
}
