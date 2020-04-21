package com.yogpc.qp.tile;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

public class DetailDataCollector implements Serializable {
    private transient final LongSupplier time;
    private final Map<Long, Data> data;

    @Deprecated
    public DetailDataCollector() {
        this(() -> 0);
    }

    private DetailDataCollector(LongSupplier time) {
        this.time = time;
        data = new HashMap<>();
    }

    public static DetailDataCollector getInstance(APowerTile tile) {
        if (Config.content().debug())
            return new DetailDataCollector(() -> Objects.requireNonNull(tile.getWorld()).getTotalWorldTime());
        else return new Dummy();
    }

    private static class Dummy extends DetailDataCollector {
        private static final Data DATA = new DummyData();

        public Dummy() {
            super(() -> 0);
        }

        private static class DummyData extends DetailDataCollector.Data {
            @Override
            public String toString() {
                return "DummyData";
            }

            @Override
            public void setEnergy(long energy) {
            }

            @Override
            public void addData(EnergyDetail... arg) {
            }
        }

        @Override
        public Data get() {
            return DATA;
        }

        @Override
        public Data getOrCreateData(long tick) {
            return DATA;
        }

        @Override
        public void finish() {
        }
    }

    public Data get() {
        return getOrCreateData(time.getAsLong());
    }

    public Data getOrCreateData(long tick) {
        return data.computeIfAbsent(tick, aLong -> new Data());
    }

    public void finish() {
        Path parent = Paths.get("debug", "quarryplus");
        if (Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                QuarryPlus.LOGGER.error("Errored creating directories.", e);
                return;
            }
        }
        LocalDateTime time = LocalDateTime.now();
        String name = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(':', '-');

        Optional<Long> min = data.keySet().stream().min(Long::compareTo);
        List<String> collect = data.entrySet().stream()
            .map(e -> Pair.of(e.getKey() - min.orElse(0L), e.getValue()))
            .sorted(Map.Entry.comparingByKey())
            .map(p -> p.getKey() + "," + ((double) p.getValue().energy / APowerTile.MJToMicroMJ) + "," + p.getValue())
            .collect(Collectors.toList());
        collect.add(0, "Tick,Energy,Detail");
        try {
            Files.write(parent.resolve(name + ".csv"), collect);
            try (ObjectOutputStream s = new ObjectOutputStream(Files.newOutputStream(parent.resolve(name + ".dat")))) {
                s.writeObject(this);
            }
        } catch (IOException e) {
            QuarryPlus.LOGGER.error("Errored writing file.", e);
        }
        data.clear();
    }

    public static class Data implements Serializable {
        long energy = 0;
        private final List<EnergyDetail> others = new ArrayList<>();

        @Override
        public String toString() {
            return "Data{" + others.stream().map(Objects::toString).collect(Collectors.joining(" ")) +
                '}';
        }

        public void setEnergy(long energy) {
            this.energy = energy;
        }

        public void addData(EnergyDetail... arg) {
            others.addAll(Arrays.asList(arg));
            long sum = Stream.of(arg).mapToLong(EnergyDetail::getEnergy).sum();
            setEnergy(energy + sum);
        }
    }

    public interface EnergyDetail {
        long getEnergy();
    }

    public static class Break implements EnergyDetail, Serializable {
        private final String name;
        private final float hardness;
        private final long e;

        public Break(IBlockState block, float hardness, long e) {
            name = Objects.toString(block);
            this.hardness = hardness;
            this.e = e;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Break aBreak = (Break) o;
            return Float.compare(aBreak.hardness, hardness) == 0 &&
                aBreak.e == e &&
                name.equals(aBreak.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, hardness, e);
        }

        @Override
        public String toString() {
            return "Break{" +
                "name='" + name + '\'' +
                " hardness=" + hardness +
                " e=" + (double) e / APowerTile.MJToMicroMJ +
                '}';
        }

        @Override
        public long getEnergy() {
            return e;
        }
    }

    public static class Pump implements EnergyDetail, Serializable {
        private final long amount;
        private final int u;
        private final long frame;
        private final long energy;

        public Pump(long amount, int u, long frame, long energy) {
            this.amount = amount;
            this.u = u;
            this.frame = frame;
            this.energy = energy;
        }

        @Override
        public String toString() {
            return "Pump{" +
                "amount=" + amount +
                " Un-breaking=" + u +
                " frame=" + frame +
                " e=" + (double) energy / APowerTile.MJToMicroMJ +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pump pump = (Pump) o;
            return amount == pump.amount &&
                u == pump.u &&
                frame == pump.frame;
        }

        @Override
        public int hashCode() {
            return Objects.hash(amount, u, frame);
        }

        @Override
        public long getEnergy() {
            return energy;
        }
    }

    public static final class Common implements EnergyDetail, Serializable {
        private final EnergyUsage usage;
        private final long energy;

        public Common(EnergyUsage usage, long e) {
            this.usage = usage;
            this.energy = e;
        }

        @Override
        public String toString() {
            return "Common{" +
                "usage=" + usage +
                " energy=" + (double) energy / APowerTile.MJToMicroMJ +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Common common = (Common) o;
            return Double.compare(common.energy, energy) == 0 &&
                usage == common.usage;
        }

        @Override
        public int hashCode() {
            return Objects.hash(usage, energy);
        }

        @Override
        public long getEnergy() {
            return energy;
        }
    }
}
