package com.yogpc.qp.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QuarryConfigTest {
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
