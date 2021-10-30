package com.yogpc.qp.machines;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.yogpc.qp.QuarryPlus;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public abstract class EnergyCounter {
    private static final Logger LOGGER = QuarryPlus.LOGGER;
    private static final Marker MARKER_TICK = MarkerManager.getMarker("TickLog");
    private static final Marker MARKER_FINAL = MarkerManager.getMarker("Total");
    final String name;
    final long logInterval;

    public EnergyCounter(String name) {
        this.name = name;
        logInterval = 20 * 5;
    }

    public static EnergyCounter createInstance(boolean isDebug, String name) {
        if (isDebug) return new Debug(name);
        else return new Production();
    }

    public abstract void logOutput(long time);

    public abstract void logUsageMap();

    public abstract void useEnergy(long time, long amount, PowerTile.Reason reason);

    public abstract void getEnergy(long time, long amount);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", logInterval=" + logInterval +
            '}';
    }

    private static class Debug extends EnergyCounter {
        private long lastLogTick;
        private final Map<Long, Long> useCounter = new HashMap<>();
        private final Map<Long, Long> getCounter = new HashMap<>();
        private final Map<PowerTile.Reason, Long> usageMap = new EnumMap<>(PowerTile.Reason.class);

        public Debug(String name) {
            super(name);
        }

        @Override
        public void logOutput(long time) {
            if (time - lastLogTick >= logInterval) {
                lastLogTick = time;
                var use = useCounter.values().stream().collect(Collectors.summarizingLong(Long::longValue));
                var get = getCounter.values().stream().collect(Collectors.summarizingLong(Long::longValue));
                if (use.getSum() != 0 && get.getSum() != 0)
                    LOGGER.info(MARKER_TICK, "{}: Used {} FE in {} ticks({} FE/t). Got {} FE in {} ticks({} FE/t).", name,
                        use.getSum() / PowerTile.ONE_FE, use.getCount(), use.getAverage() / PowerTile.ONE_FE,
                        get.getSum() / PowerTile.ONE_FE, get.getCount(), get.getAverage() / PowerTile.ONE_FE);
                useCounter.clear();
                getCounter.clear();
            }
        }

        @Override
        public void logUsageMap() {
            usageMap.entrySet().stream()
                .map(e -> "%s -> %f".formatted(e.getKey(), (double) e.getValue() / PowerTile.ONE_FE))
                .forEach(s -> LOGGER.info(MARKER_FINAL, s));
            usageMap.clear();
        }

        private void checkTime(long time, String name) {
            if (lastLogTick == 0) {
                lastLogTick = time;
            } else if (time - lastLogTick > logInterval) {
                LOGGER.warn(MARKER_TICK, "The last log time reset? Last: {}, Now({}): {}", lastLogTick, name, time);
            }
        }

        @Override
        public void useEnergy(long time, long amount, PowerTile.Reason reason) {
            checkTime(time, "USE");
            useCounter.merge(time, amount, Long::sum);
            usageMap.merge(reason, amount, Long::sum);
        }

        @Override
        public void getEnergy(long time, long amount) {
            checkTime(time, "GET");
            getCounter.merge(time, amount, Long::sum);
        }
    }

    private static class Production extends EnergyCounter {

        public Production() {
            super("Production");
        }

        @Override
        public void logOutput(long time) {
        }

        @Override
        public void logUsageMap() {
        }

        @Override
        public void useEnergy(long time, long amount, PowerTile.Reason reason) {
        }

        @Override
        public void getEnergy(long time, long amount) {
        }
    }
}
