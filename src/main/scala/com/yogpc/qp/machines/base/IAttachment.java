package com.yogpc.qp.machines.base;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.exppump.TileExpPump;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IAttachment {
    /**
     * Setter method to change field in this class.
     */
    void setConnectTo(@Nullable EnumFacing connectTo);

    class Attachments<T extends APacketTile> implements Predicate<TileEntity>, Function<TileEntity, Optional<T>> {
//        public static final Attachments<TilePump> FLUID_PUMP = new Attachments<>("FLUID_PUMP");
        public static final Attachments<TileExpPump> EXP_PUMP = new Attachments<>("EXP_PUMP");
//        public static final Attachments<TileReplacer> REPLACER = new Attachments<>("REPLACER");
        public static final Set<Attachments<? extends APacketTile>> ALL;

        static {
            ALL = Collections.unmodifiableSet(
                Arrays.stream(Attachments.class.getDeclaredFields())
                    .filter(field -> field.getType() == Attachments.class)
                    .map(field -> {
                        try {
                            return (Attachments<? extends APacketTile>) field.get(null);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toSet())
            );
            assert !ALL.isEmpty();
        }

        @Nonnull
        private final String name;
        @Nonnull
        private final Class<T> clazz;

        @SuppressWarnings("unchecked")
        private Attachments(@Nonnull String name, T... ts) {
            this.name = name;
            this.clazz = ((Class<T>) ts.getClass().getComponentType());
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Optional<T> apply(TileEntity tileEntity) {
            if (clazz.isInstance(tileEntity)) {
                return Optional.of(clazz.cast(tileEntity));
            }
            return Optional.empty();
        }

        @Override
        public boolean test(TileEntity tileEntity) {
            return clazz.isInstance(tileEntity);
        }

    }
}
