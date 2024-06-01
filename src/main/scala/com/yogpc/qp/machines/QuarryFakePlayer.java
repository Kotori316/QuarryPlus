package com.yogpc.qp.machines;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * All things are copied from FakePlayer and FakePlayerFactory
 * Copyright (c) Forge Development LLC and contributors
 */
public class QuarryFakePlayer {
    private static final GameProfile profile = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]");
    private static final Map<ServerLevel, ServerPlayer> players = new HashMap<>();

    public static ServerPlayer get(ServerLevel serverLevel) {
        return players.computeIfAbsent(serverLevel, key -> {
            var cookie = CommonListenerCookie.createInitial(profile);
            var player = new InternalFakePlayer(key, profile, cookie.clientInformation());
            // new InternalFakePlayer.NetHandler(key.getServer(), player, cookie);
            return player;
        });
    }

    public static ServerPlayer getAndSetPosition(ServerLevel serverLevel, BlockPos pos, ItemStack stack) {
        ServerPlayer player = get(serverLevel);
        player.setPos(Vec3.atCenterOf(pos.above(2)));
        player.setXRot(90f);
        player.setYRot(90f);
        player.setYHeadRot(90f);
        if (stack != null) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        }
        return player;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void unloadLevel(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level)
            players.entrySet().removeIf(entry -> entry.getValue().level() == level);
    }

    private static final class InternalFakePlayer extends ServerPlayer {
        private InternalFakePlayer(ServerLevel level, GameProfile name, ClientInformation info) {
            super(level.getServer(), level, name, info);
        }

        @Override
        public void displayClientMessage(Component chatComponent, boolean actionBar) {
        }

        @Override
        public void awardStat(Stat<?> stat, int amount) {
        }

        @Override
        public boolean isInvulnerableTo(DamageSource source) {
            return true;
        }

        @Override
        public boolean canHarmPlayer(Player player) {
            return false;
        }

        @Override
        public void die(DamageSource source) {
        }

        @Override
        public void tick() {
        }

        @Override
        @Nullable
        public MinecraftServer getServer() {
            return ServerLifecycleHooks.getCurrentServer();
        }

        @ParametersAreNonnullByDefault
        static class NetHandler extends ServerGamePacketListenerImpl {
            // @formatter:off
            private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.SERVERBOUND);

            NetHandler(MinecraftServer server, ServerPlayer player, CommonListenerCookie cookie) {
                super(server, DUMMY_CONNECTION, player, cookie);
            }

            @Override public void tick() { }
            @Override public void resetPosition() { }
            @Override public void disconnect(Component message) { }
            @Override public void handlePlayerInput(ServerboundPlayerInputPacket packet) { }
            @Override public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) { }
            @Override public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) { }
            @Override public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) { }
            @Override public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) { }
            @Override public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) { }
            @Override public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) { }
            @Override public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) { }
            @Override public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) { }
            @Override public void handlePickItem(ServerboundPickItemPacket packet) { }
            @Override public void handleRenameItem(ServerboundRenameItemPacket packet) { }
            @Override public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) { }
            @Override public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) { }
            @Override public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) { }
            @Override public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) { }
            @Override public void handleSelectTrade(ServerboundSelectTradePacket packet) { }
            @Override public void handleEditBook(ServerboundEditBookPacket packet) { }
            @Override public void handleEntityTagQuery(ServerboundEntityTagQuery packet) { }
            @Override public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) { }
            @Override public void handleMovePlayer(ServerboundMovePlayerPacket packet) { }
            @Override public void teleport(double x, double y, double z, float yaw, float pitch) { }
            @Override public void handlePlayerAction(ServerboundPlayerActionPacket packet) { }
            @Override public void handleUseItemOn(ServerboundUseItemOnPacket packet) { }
            @Override public void handleUseItem(ServerboundUseItemPacket packet) { }
            @Override public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) { }
            @Override public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) { }
            @Override public void onDisconnect(Component message) { }
            @Override public void send(Packet<?> packet) { }
            @Override public void send(Packet<?> packet, @Nullable PacketSendListener sendListener) { }
            @Override public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) { }
            @Override public void handleChat(ServerboundChatPacket packet) { }
            @Override public void handleAnimate(ServerboundSwingPacket packet) { }
            @Override public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) { }
            @Override public void handleInteract(ServerboundInteractPacket packet) { }
            @Override public void handleClientCommand(ServerboundClientCommandPacket packet) { }
            @Override public void handleContainerClose(ServerboundContainerClosePacket packet) { }
            @Override public void handleContainerClick(ServerboundContainerClickPacket packet) { }
            @Override public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) { }
            @Override public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) { }
            @Override public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) { }
            @Override public void handleSignUpdate(ServerboundSignUpdatePacket packet) { }
            @Override public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) { }
            @Override public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) { }
            @Override public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) { }
            @Override public void teleport(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet) { }
            @Override public void ackBlockChangesUpTo(int sequence) { }
            @Override public void handleChatCommand(ServerboundChatCommandPacket packet) { }
            @Override public void handleChatAck(ServerboundChatAckPacket packet) { }
            @Override public void addPendingMessage(PlayerChatMessage message) { }
            @Override public void sendPlayerChatMessage(PlayerChatMessage message, ChatType.Bound boundChatType) { }
            @Override public void sendDisguisedChatMessage(Component content, ChatType.Bound boundChatType) { }
            @Override public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) { }
            // @formatter:on
        }
    }

}
