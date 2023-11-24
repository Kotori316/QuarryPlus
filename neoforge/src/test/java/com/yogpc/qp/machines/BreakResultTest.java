package com.yogpc.qp.machines;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BreakResultTest {
    @Test
    void isSuccess() {
        assertAll(
            () -> assertFalse(BreakResult.INVALID_CONDITION.isSuccess()),
            () -> assertTrue(BreakResult.FAIL_EVENT.isSuccess()),
            () -> assertFalse(BreakResult.NOT_ENOUGH_ENERGY.isSuccess()),
            () -> assertTrue(BreakResult.SUCCESS.isSuccess()),
            () -> assertTrue(BreakResult.SKIPPED.isSuccess())
        );
    }
}