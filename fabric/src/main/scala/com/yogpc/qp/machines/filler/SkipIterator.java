package com.yogpc.qp.machines.filler;

import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

final class SkipIterator {
    FillerTargetPosIterator posIterator;
    List<BlockPos> skipped;
    private final Area area;
    boolean checkSkipped = false;

    SkipIterator(Area area, Function<Area, FillerTargetPosIterator> constructor) {
        this.area = area;
        this.posIterator = constructor.apply(area);
        this.skipped = new ArrayList<>();
    }

    @Nullable
    BlockPos peek(Predicate<BlockPos> filter) {
        if (checkSkipped) {
            var skipped = this.skipped.stream().filter(filter).findFirst();
            if (skipped.isPresent()) {
                return skipped.get();
            }
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
        tag.putString("type", this.posIterator.type().name());
        tag.put("area", area.toNBT());
        tag.putLong("current", posIterator.peek().asLong());
        var skips = this.skipped.stream().mapToLong(BlockPos::asLong).toArray();
        tag.putLongArray("skips", skips);
        tag.putBoolean("checkSkipped", this.checkSkipped);
        return tag;
    }

    static SkipIterator fromNbt(CompoundTag tag) {
        var area = Area.fromNBT(tag.getCompound("area")).orElseThrow(() ->
            new IllegalArgumentException("Invalid tag for SkipIterator. %s".formatted(tag)));
        var action = FillerEntity.Action.valueOf(tag.getString("type"));
        var checkSkipped = tag.getBoolean("checkSkipped"); // default is false
        var skipIterator = new SkipIterator(area, action.iteratorProvider);
        var current = BlockPos.of(tag.getLong("current"));
        skipIterator.posIterator.setCurrent(current);
        Arrays.stream(tag.getLongArray("skips"))
            .mapToObj(BlockPos::of)
            .forEach(skipIterator.skipped::add);
        skipIterator.checkSkipped = checkSkipped;
        return skipIterator;
    }
}
