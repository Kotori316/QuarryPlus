package com.yogpc.qp.utils;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.SectionPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuarryChunkLoadUtilTest extends QuarryPlusTest {
    @Test
    void biasTest1() {
        var x = 20;
        var c = SectionPos.blockToSectionCoord(x);
        assertEquals(1, c);
    }
}