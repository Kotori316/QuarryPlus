package com.yogpc.qp.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnableMapTest {
    @Test
    void createInstance() {
        assertDoesNotThrow(() -> new EnableMap());
    }

    @ParameterizedTest
    @ValueSource(strings = {"quarry", "pump"})
    void getFromEmptyMap(String name) {
        var map = new EnableMap();
        assertFalse(map.enabled(name));
    }

    @Test
    void getFromMap() {
        var map = new EnableMap(Map.of(
            "quarry", () -> true
        ));
        assertTrue(map.enabled("quarry"));
        assertFalse(map.enabled("pump"));
    }

    @Test
    void setToMap() {
        var map = new EnableMap();
        assertFalse(map.enabled("quarry"));
        map.set("quarry", true);
        assertTrue(map.enabled("quarry"));
    }
}
