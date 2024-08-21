package com.yogpc.qp.machine.misc;

import java.util.OptionalInt;

/**
 * @param canceled {@code true} if event is cancelled. {@code false} if event is successfully accepted.
 * @param exp      the experience point to collect. Empty if point is not determined.
 */
public record BlockBreakEventResult(boolean canceled, OptionalInt exp) {
    public static final BlockBreakEventResult CANCELED = new BlockBreakEventResult(true, OptionalInt.empty());
    public static final BlockBreakEventResult EMPTY = new BlockBreakEventResult(false, OptionalInt.empty());
}
