package com.yogpc.qp.utils;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.SectionPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(QuarryPlusTest.class)
class QuarryChunkLoadUtilTest {
    @Test
    void biasTest1() {
        var x = 20;
        var c = SectionPos.blockToSectionCoord(x);
        assertEquals(1, c);
    }
}