package com.yogpc.qp.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class QuarryConfigTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getDefault(boolean debug) {
        var loaded = QuarryConfig.defaultConfig(debug);
        assertNotNull(loaded);

        assertEquals(debug, loaded.debug());
    }

    @Test
    void defaultDebug() {
        var defaultConfig = TestQuarryConfig.defaults();
        assertFalse(defaultConfig.debug());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setDebug(boolean debug) {
        var defaultConfig = TestQuarryConfig.defaults().withDebug(debug);
        assertEquals(debug, defaultConfig.debug());
    }
}
