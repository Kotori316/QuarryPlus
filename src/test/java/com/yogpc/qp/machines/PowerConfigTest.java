package com.yogpc.qp.machines;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.advquarry.BlockAdvQuarry;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.SFQuarryBlock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerConfigTest extends QuarryPlusTest {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @Test
    void fileExists() {
        assertNotNull(getClass().getResource("/power_default.json"));
    }

    static JsonObject getJson() {
        var stream = PowerConfigTest.class.getResourceAsStream("/power_default.json");
        assert stream != null;
        return GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
    }

    @Test
    void isValidJson() {
        assertDoesNotThrow(PowerConfigTest::getJson);
    }

    @ParameterizedTest
    @ValueSource(strings = {QuarryBlock.NAME, SFQuarryBlock.NAME, BlockAdvQuarry.NAME})
    void containAll(String machineName) {
        var json = getJson();
        var config = json.getAsJsonObject(machineName);

        assertAll(
            Arrays.stream(PowerConfig.class.getMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::getName)
                .filter(name -> Character.isLowerCase(name.charAt(0)))
                .map(name -> () -> assertTrue(config.has(name), "%s doesn't exist in %s".formatted(name, config.keySet())))
        );
    }
}
