package com.yogpc.qp.tile;

import java.util.function.Predicate;

import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IAttachment {
    public void setConnectTo(@Nullable EnumFacing connectTo);

    enum Attachments implements Predicate<TileEntity> {
        FLUID_PUMP(TilePump.class),
        EXP_PUMP(TileExpPump.class);
        private final Class<? extends APacketTile> clazz;

        Attachments(Class<? extends APacketTile> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean test(TileEntity tileEntity) {
            return clazz.isInstance(tileEntity);
        }
    }
}
