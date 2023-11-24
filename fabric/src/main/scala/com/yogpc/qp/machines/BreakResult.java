package com.yogpc.qp.machines;

public enum BreakResult {
    SUCCESS,
    SKIPPED,
    NOT_ENOUGH_ENERGY,
    INVALID_CONDITION,
    FAIL_UNKNOWN_REASON;

    public boolean isSuccess() {
        return this == SUCCESS || this == SKIPPED;
    }
}
