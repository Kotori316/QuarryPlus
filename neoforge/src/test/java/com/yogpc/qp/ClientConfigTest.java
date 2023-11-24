package com.yogpc.qp;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(QuarryPlusTest.class)
class ClientConfigTest {
    @Nested
    class GetAllTest {
        @Test
        void chunkDestroyerSetting() {
            var map = assertDoesNotThrow(() -> QuarryPlus.clientConfig.chunkDestroyerSetting.getAll());
            assertFalse(map.isEmpty());
        }

        @Test
        void all() {
            var c = QuarryPlus.clientConfig;
            var map = assertDoesNotThrow(c::getAll);
            var fieldCount = ClientConfig.class.getFields().length;
            assertEquals(fieldCount, map.size());
            var fieldNames = Stream.of(ClientConfig.class.getFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
            assertEquals(fieldNames, map.keySet());
        }
    }
}