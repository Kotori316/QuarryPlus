package com.yogpc.qp.machines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class EnergyCounter {
    private static final Logger LOGGER = LogManager.getLogger(EnergyCounter.class);
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
                LongSummaryStatistics use = useCounter.values().stream().collect(Collectors.summarizingLong(Long::longValue));
                LongSummaryStatistics get = getCounter.values().stream().collect(Collectors.summarizingLong(Long::longValue));
                if (use.getSum() != 0 && get.getSum() != 0) {
                    var useAverage = String.format("%.2f", use.getAverage() / PowerTile.ONE_FE);
                    var getAverage = String.format("%.2f", get.getAverage() / PowerTile.ONE_FE);
                    LOGGER.info("{}: Used {} FE in {} ticks({} FE/t). Got {} FE in {} ticks({} FE/t).", name,
                        use.getSum() / PowerTile.ONE_FE, use.getCount(), useAverage,
                        get.getSum() / PowerTile.ONE_FE, get.getCount(), getAverage);
                }
                useCounter.clear();
                getCounter.clear();
            }
        }

        @Override
        public void logUsageMap() {
            usageMap.entrySet().stream()
                .map(e -> "%s -> %d".formatted(e.getKey(), e.getValue()))
                .forEach(LOGGER::info);
        }

        private void checkTime(long time, String name) {
            if (lastLogTick == 0) {
                lastLogTick = time;
            } else if (time - lastLogTick > logInterval) {
                LOGGER.warn("The last log time reset? Last: {}, Now({}): {}", lastLogTick, name, time);
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
