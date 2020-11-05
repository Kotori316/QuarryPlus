package com.yogpc.qp.machines.quarry;

import java.net.SocketAddress;
import java.util.Set;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import javax.annotation.Nullable;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.FakePlayer;

/**
 * A dummy class for FakePlayers. Implemented to prevent crashing due to {@link NullPointerException} of connection.
 * Copied from {@link cofh.core.entity.NetServerHandlerFake}.
 */
public class FakeHandler extends ServerPlayNetHandler {

    public FakeHandler(FakePlayer fakePlayer) {
        super(fakePlayer.server, new FakeNetworkManager(), fakePlayer);
    }

    /**
     * Copied from {@link cofh.core.entity.NetServerHandlerFake.NetworkManagerFake}.
     */
    private static class FakeNetworkManager extends NetworkManager {

        public FakeNetworkManager() {
            super(PacketDirection.CLIENTBOUND);
        }

        @Override
        public void channelActive(ChannelHandlerContext p_channelActive_1_) {
        }

        @Override
        public void setConnectionState(ProtocolType newState) {
        }

        @Override
        public void channelInactive(ChannelHandlerContext p_channelInactive_1_) {
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) {
        }

        @Override
        public void setNetHandler(INetHandler handler) {
        }

        @Override
        public void sendPacket(IPacket<?> packetIn) {
        }

        @Override
        public void sendPacket(IPacket<?> packetIn, @Nullable GenericFutureListener<? extends Future<? super Void>> p_201058_2_) {

        }

        @Override
        public SocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public boolean isLocalChannel() {
            return false;
        }

        @Override
        public boolean isChannelOpen() {
            return false;
        }

        @Override
        public INetHandler getNetHandler() {
            return null;
        }

        @Override
        public ITextComponent getExitMessage() {
            return null;
        }

        @Override
        public void setCompressionThreshold(int threshold) {
        }

        @Override
        public void disableAutoRead() {
        }

        @Override
        public void handleDisconnection() {
        }

        @Override
        public Channel channel() {
            return null;
        }
    }

    @Override
    public void tick() {
    }

    @Override
    public void disconnect(final ITextComponent textComponent) {
    }

    @Override
    public void processInput(CInputPacket packetIn) {
    }

    @Override
    public void processVehicleMove(CMoveVehiclePacket packetIn) {
    }

    @Override
    public void processConfirmTeleport(CConfirmTeleportPacket packetIn) {
    }

    @Override
    public void processPlayer(CPlayerPacket packetIn) {
    }

    @Override
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPlayerPositionLookPacket.Flags> relativeSet) {
        this.player.setPositionAndRotation(x, y, z, yaw, pitch);
    }

    @Override
    public void processPlayerDigging(CPlayerDiggingPacket packetIn) {
    }

    @Override
    public void processTryUseItemOnBlock(CPlayerTryUseItemOnBlockPacket packetIn) {
    }

    @Override
    public void processTryUseItem(CPlayerTryUseItemPacket packetIn) {
    }

    @Override
    public void handleSpectate(CSpectatePacket packetIn) {
    }

    @Override
    public void handleResourcePackStatus(CResourcePackStatusPacket packetIn) {
    }

    @Override
    public void processSteerBoat(CSteerBoatPacket packetIn) {
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
    }

    @Override
    public void sendPacket(final IPacket<?> packetIn) {
    }

    @Override
    public void processHeldItemChange(CHeldItemChangePacket packetIn) {
    }

    @Override
    public void processChatMessage(CChatMessagePacket packetIn) {
    }

    @Override
    public void handleAnimation(CAnimateHandPacket packetIn) {
    }

    @Override
    public void processEntityAction(CEntityActionPacket packetIn) {
    }

    @Override
    public void processUseEntity(CUseEntityPacket packetIn) {
    }

    @Override
    public void processClientStatus(CClientStatusPacket packetIn) {
    }

    @Override
    public void processCloseWindow(CCloseWindowPacket packetIn) {
    }

    @Override
    public void processClickWindow(CClickWindowPacket packetIn) {
    }

    @Override
    public void processEnchantItem(CEnchantItemPacket packetIn) {
    }

    @Override
    public void processCreativeInventoryAction(CCreativeInventoryActionPacket packetIn) {
    }

    @Override
    public void processConfirmTransaction(CConfirmTransactionPacket packetIn) {
    }

    @Override
    public void processUpdateSign(CUpdateSignPacket packetIn) {
    }

    @Override
    public void processKeepAlive(CKeepAlivePacket packetIn) {
    }

    @Override
    public void processPlayerAbilities(CPlayerAbilitiesPacket packetIn) {
    }

    @Override
    public void processTabComplete(CTabCompletePacket packetIn) {
    }

    @Override
    public void processClientSettings(CClientSettingsPacket packetIn) {
    }

    @Override
    public void processCustomPayload(CCustomPayloadPacket packetIn) {
    }


}
