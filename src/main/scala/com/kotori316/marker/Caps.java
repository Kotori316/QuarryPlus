package com.kotori316.marker;

import java.util.Optional;

import com.yogpc.qp.machines.base.IMarker;
import com.yogpc.qp.machines.base.IRemotePowerOn;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class Caps {
    public static final class QuarryPlus {
        @CapabilityInject(IMarker.class)
        public static final Capability<IMarker> MARKER_CAPABILITY = null;
        @CapabilityInject(IRemotePowerOn.class)
        public static final Capability<IRemotePowerOn> REMOTE_POWER_ON_CAPABILITY = null;
    }

    public static boolean isQuarryModLoaded() {
        return ModList.get().isLoaded("quarryplus");
    }

    @SuppressWarnings("ConstantConditions")
    public static Optional<Capability<IMarker>> markerCapability() {
        if (isQuarryModLoaded())
            return Optional.ofNullable(QuarryPlus.MARKER_CAPABILITY);
        else
            return Optional.empty();
    }

    @SuppressWarnings("ConstantConditions")
    public static Optional<Capability<IRemotePowerOn>> remotePowerOnCapability() {
        if (isQuarryModLoaded())
            return Optional.ofNullable(QuarryPlus.REMOTE_POWER_ON_CAPABILITY);
        else
            return Optional.empty();
    }

    public static final class Event {
        @SubscribeEvent
        public static void attachCapability(AttachCapabilitiesEvent<TileEntity> event) {
            if (Caps.isQuarryModLoaded() && (event.getObject() instanceof Tile16Marker || event.getObject() instanceof TileFlexMarker)) {
                event.addCapability(new ResourceLocation(Marker.modID, "capability_marker"), new Provider(event.getObject()));
            }
        }

        private static final class Provider implements ICapabilityProvider {
            private final IMarker marker;

            private Provider(TileEntity tileEntity) {
                if (tileEntity instanceof TileFlexMarker) {
                    TileFlexMarker marker = (TileFlexMarker) tileEntity;
                    this.marker = new FlexMarkerInstance(marker);
                } else if (tileEntity instanceof Tile16Marker) {
                    Tile16Marker marker = (Tile16Marker) tileEntity;
                    this.marker = new ChunkMarkerInstance(marker);
                } else {
                    this.marker = IMarker.EMPTY_MARKER;
                }
            }

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return Caps.markerCapability().map(c -> c.orEmpty(cap, LazyOptional.of(() -> marker))).orElse(LazyOptional.empty());
            }
        }
    }
}
