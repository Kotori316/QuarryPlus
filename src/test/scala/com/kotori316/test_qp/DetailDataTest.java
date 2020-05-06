package com.kotori316.test_qp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.yogpc.qp.machines.base.DetailDataCollector;
import com.yogpc.qp.machines.base.EnergyUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DetailDataTest {
    private static final Field MAP_FIELD;
    DetailDataCollector collector;

    static {
        try {
            Field data = DetailDataCollector.class.getDeclaredField("data");
            data.setAccessible(true);
            MAP_FIELD = data;
        } catch (NoSuchFieldException e) {
            throw new InternalError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Long, DetailDataCollector.Data> getMap(DetailDataCollector instance) {
        try {
            return (Map<Long, DetailDataCollector.Data>) MAP_FIELD.get(instance);
        } catch (IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

    @BeforeEach
    void beforeEach() {
        AtomicLong counter = new AtomicLong(0);
        this.collector = new DetailDataCollector(counter::getAndIncrement);
    }

    @Test
    void createInstance() {
        assertNotNull(collector);
        DetailDataCollector.Data data = collector.getOrCreateData(0L);
        data.addData(new DetailDataCollector.Common(EnergyUsage.UNKNOWN, 100));
        DetailDataCollector.Data data1 = collector.getOrCreateData(0L);
        assertEquals(data, data1);
        DetailDataCollector.Data data2 = collector.get(); // Counter = 0
        assertEquals(data, data2);

        DetailDataCollector.Data count1_0 = collector.get(); // Counter = 1
        DetailDataCollector.Data count1_1 = collector.getOrCreateData(1L);
        assertEquals(count1_0, count1_1);
    }

    @Test
    void serialize() throws IOException, ClassNotFoundException {
        assertNotNull(collector);
        DetailDataCollector.Data data1 = collector.get();
        data1.addData(new DetailDataCollector.Common(EnergyUsage.UNKNOWN, 100L));
        data1.addData(new DetailDataCollector.Common(EnergyUsage.UNKNOWN, 200L));
        DetailDataCollector.Data data2 = collector.get();
        data2.addData(new DetailDataCollector.Common(EnergyUsage.UNKNOWN, 300L));
        data2.addData(new DetailDataCollector.Common(EnergyUsage.UNKNOWN, 400L));
        Map<Long, DetailDataCollector.Data> c1 = new HashMap<>(getMap(collector));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        collector.writeData(strings -> no(), stream);
        byte[] bytes = stream.toByteArray();

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        DetailDataCollector c2 = ((DetailDataCollector) inputStream.readObject());
        Map<Long, DetailDataCollector.Data> map = getMap(c2);
        assertEquals(c1, map);
    }

    void no() {

    }
}
