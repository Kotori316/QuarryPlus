package com.yogpc.qp.machine.quarry;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuarryBlockTest {
    @Test
    void dummy() {
        assertEquals(QuarryBlock.NAME, QuarryBlock.NAME.toLowerCase(Locale.ROOT));
    }
}
