package com.yogpc.qp.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import scala.collection.Seq;
import scala.jdk.javaapi.CollectionConverters;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SerializeUtilsTest {

    @Test
    void toEmptyJsonArray() {
        Seq<String> emptyStringList = CollectionConverters.asScala(Collections.emptyList());
        assertIterableEquals(new JsonArray(), SerializeUtils.toJsonArray(emptyStringList, JsonPrimitive::new));
    }

    @Test
    void toJsonArray() {
        List<String> list = Arrays.asList("Ago", "Be", "Cool", "Disappointed");
        Seq<String> strings = CollectionConverters.asScala(list);

        JsonArray expected = new JsonArray();
        list.forEach(expected::add);

        assertIterableEquals(expected, SerializeUtils.toJsonArray(strings, JsonPrimitive::new));
    }
}