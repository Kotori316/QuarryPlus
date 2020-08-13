package com.yogpc.qp.utils;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;

public class ProxyProvider {

    public static AbstractProxy getInstance() {
        switch (FMLEnvironment.dist) {
            case CLIENT:
                return new ClientSupplier().get();
            case DEDICATED_SERVER:
                return new ServerSupplier().get();
            default:
                throw new IllegalArgumentException("UN SIDED?");
        }
    }

    public abstract static class AbstractProxy {
        public abstract Optional<PlayerEntity> getPacketPlayer(final NetworkEvent.Context context);

        public abstract Optional<World> getPacketWorld(NetworkEvent.Context context);

        public abstract void removeEntity(final Entity e);

        @Nullable
        public abstract World getClientWorld();

        public abstract void setDummyTexture(String textureName);

        public static int toInt(long l) {
            if (l > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            else if (l < Integer.MIN_VALUE) return Integer.MIN_VALUE;
            else return (int) l;
        }
    }

    private static class ClientSupplier implements Supplier<AbstractProxy> {
        @Override
        @OnlyIn(Dist.CLIENT)
        public AbstractProxy get() {
            return new ProxyClient();
        }
    }

    private static class ServerSupplier implements Supplier<AbstractProxy> {
        @Override
        public AbstractProxy get() {
            return new ProxyCommon();
        }
    }
}
