package com.yogpc.qp.machines.filler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

final class SkipIterator {
    FillerTargetPosIterator posIterator;
    List<BlockPos> skipped;
    private final Area area;

    SkipIterator(Area area, Function<Area, FillerTargetPosIterator> constructor) {
        this.area = area;
        this.posIterator = constructor.apply(area);
        this.skipped = new ArrayList<>();
    }

    @Nullable
    BlockPos peek(Predicate<BlockPos> filter) {
        var skipped = this.skipped.stream().filter(filter).findFirst();
        if (skipped.isPresent()) {
            return skipped.get();
        }
        while (this.posIterator.hasNext()) {
            var pos = this.posIterator.peek();
            if (filter.test(pos)) {
                return pos;
            } else {
                commit(pos, true);
            }
        }
        return null;
    }

    void commit(BlockPos pos, boolean skip) {
        boolean alreadySkipped = skipped.remove(pos); // If the skipped contains the pos, the pos is not from the iterator.
        if (skip) {
            this.skipped.add(pos);
        }
        if (!alreadySkipped) this.posIterator.next();
    }

    @VisibleForTesting
    @Nullable
    BlockPos next(Predicate<BlockPos> filter) {
        var pos = peek(filter);
        this.commit(pos, false);
        return pos;
    }

    CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("area", area.toNBT());
        tag.putLong("current", posIterator.peek().asLong());
        var skips = this.skipped.stream().mapToLong(BlockPos::asLong).toArray();
        tag.putLongArray("skips", skips);
        return tag;
    }

    void fromNbt(CompoundTag tag) {
        var current = BlockPos.of(tag.getLong("current"));
        this.posIterator.setCurrent(current);
        Arrays.stream(tag.getLongArray("skips"))
            .mapToObj(BlockPos::of)
            .forEach(this.skipped::add);
    }

    @Override
    public String toString() {
        return "SkipIterator{area=%s, skipped=%d}".formatted(this.area, this.skipped.size());
    }
}
