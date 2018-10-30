package com.yogpc.qp.tile;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IAttachment {
    public void setConnectTo(@Nullable EnumFacing connectTo);

    static class Attachments<T extends APacketTile> implements Predicate<TileEntity>, Function<TileEntity, T> {
        public static final Attachments<TilePump> FLUID_PUMP = new Attachments<>("FLUID_PUMP");
        public static final Attachments<TileExpPump> EXP_PUMP = new Attachments<>("EXP_PUMP");
        public static final Attachments<TileReplacer> REPLACER = new Attachments<>("REPLACER");
        public static final Set<Attachments<? extends APacketTile>> ALL;

        static {
            ALL = Arrays.stream(Attachments.class.getDeclaredFields())
                .filter(field -> field.getType() == Attachments.class)
                .map(field -> {
                    try {
                        return (Attachments<? extends APacketTile>) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet());
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
        public boolean test(TileEntity tileEntity) {
            return clazz.isInstance(tileEntity);
        }

        @Override
        public T apply(TileEntity tileEntity) {
            return clazz.cast(tileEntity);
        }
    }
}
