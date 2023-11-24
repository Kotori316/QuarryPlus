package com.yogpc.qp;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(QuarryPlusTest.class)
class ServerConfigTest {
    @Nested
    class GetAllTest {
        @Test
        void machineWork() {
            var map = assertDoesNotThrow(() -> QuarryPlus.serverConfig.machineWork.getAll());
            assertFalse(map.isEmpty());
        }

        @Test
        void all() {
            var c = QuarryPlus.serverConfig;
            var map = assertDoesNotThrow(c::getAll);
            var fieldCount = ServerConfig.class.getFields().length;
            assertEquals(fieldCount, map.size());
            var fieldNames = Stream.of(ServerConfig.class.getFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
            assertEquals(fieldNames, map.keySet());
        }
    }
}