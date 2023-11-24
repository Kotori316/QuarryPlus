package com.yogpc.qp.machines;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.advquarry.BlockAdvQuarry;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.SFQuarryBlock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class PowerConfigTest {
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
            PowerConfig.getAllMethods()
                .map(Method::getName)
                .map(name -> () -> assertTrue(config.has(name), "%s doesn't exist in %s".formatted(name, config.keySet())))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {QuarryBlock.NAME, SFQuarryBlock.NAME, BlockAdvQuarry.NAME})
    void containAllBefore(String machineName) {
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

    @Test
    void getAllMethods() {
        var viaRawReflection = Arrays.stream(PowerConfig.class.getMethods())
            .filter(m -> !Modifier.isStatic(m.getModifiers()))
            .map(Method::getName)
            .filter(name -> Character.isLowerCase(name.charAt(0)))
            .collect(Collectors.toSet());
        var usedInConfig = Arrays.stream(PowerConfig.class.getMethods())
            .filter(m -> Character.isLowerCase(m.getName().charAt(0)))
            .filter(m -> m.getReturnType() == Long.TYPE || m.getReturnType() == Double.TYPE)
            .collect(Collectors.toSet());
        var viaUtilMethod = PowerConfig.getAllMethods().collect(Collectors.toSet());
        assertEquals(viaUtilMethod.stream().map(Method::getName).collect(Collectors.toSet()), viaRawReflection);
        assertEquals(viaUtilMethod, usedInConfig);
    }

    @ParameterizedTest
    @MethodSource("com.yogpc.qp.machines.PowerConfig#getAllMethods")
    void accessible(Method method) {
        assertDoesNotThrow(() -> method.invoke(PowerConfig.DEFAULT));
    }

    @Test
    void getRealConfig() {
        var config = PowerConfig.getMachineConfig(QuarryBlock.NAME);
        assertTrue(config instanceof MachinePowerConfig);
    }
}
