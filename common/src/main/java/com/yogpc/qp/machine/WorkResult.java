package com.yogpc.qp.machine;

public enum WorkResult {
    SUCCESS,
    SKIPPED,
    NOT_ENOUGH_ENERGY,
    INVALID_CONDITION,
    FAIL_EVENT,
    ;

    public boolean isSuccess() {
        return this == SUCCESS || this == SKIPPED || this == FAIL_EVENT;
    }
}
