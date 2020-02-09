package com.yogpc.qp.machines.quarry

import java.io.File
import java.util.{OptionalInt, UUID}

import com.mojang.authlib.GameProfile
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.advancements.{Advancement, AdvancementProgress, PlayerAdvancements}
import net.minecraft.entity.passive.horse.AbstractHorseEntity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.potion.EffectInstance
import net.minecraft.tileentity.{CommandBlockTileEntity, SignTileEntity}
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.apache.logging.log4j.MarkerManager

class QuarryFakePlayer private(worldServer: ServerWorld) extends FakePlayer(worldServer, QuarryFakePlayer.profile) {
  this.connection = new FakeHandler(this)

  private[this] val advancements = new PlayerAdvancements(
    worldServer.getServer,
    new File(new File(worldServer.getServer.getWorld(DimensionType.OVERWORLD).getSaveHandler.getWorldDirectory, "advancements"), s"$getUniqueID.json"),
    this) {
    override def getProgress(advancementIn: Advancement): AdvancementProgress = {
      new AdvancementProgress() {
        override def isDone: Boolean = true
      }
    }
  }

  override def openSignEditor(signTile: SignTileEntity): Unit = ()

  override def openHorseInventory(horse: AbstractHorseEntity, inventoryIn: IInventory): Unit = ()

  override def openCommandBlock(commandBlock: CommandBlockTileEntity): Unit = ()

  override def openContainer(p_213829_1_ : INamedContainerProvider) = OptionalInt.empty()

  override def openBook(stack: ItemStack, hand: Hand): Unit = ()

  override def playEquipSound(stack: ItemStack): Unit = ()

  override def isSilent: Boolean = true

  override def getAdvancements: PlayerAdvancements = advancements

  override def isPotionApplicable(effect: EffectInstance): Boolean = false

  override def sendStatusMessage(chatComponent: ITextComponent, actionBar: Boolean): Unit = if (Config.common.debug) {
    QuarryPlus.LOGGER.info(QuarryFakePlayer.MARKER, chatComponent.getFormattedText)
  }

  override def sendMessage(chatComponent: ITextComponent): Unit = if (Config.common.debug) {
    QuarryPlus.LOGGER.info(QuarryFakePlayer.MARKER, chatComponent.getFormattedText)
  }
}

object QuarryFakePlayer {
  val profile = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]")
  final val MARKER = MarkerManager.getMarker("QUARRY_FAKE_PLAYER")
  private var players = Map.empty[GameProfile, QuarryFakePlayer]
  MinecraftForge.EVENT_BUS.register(this)

  @scala.annotation.tailrec
  def get(server: ServerWorld, pos: BlockPos): QuarryFakePlayer = {
    players.get(profile) match {
      case Some(player) =>
        player.teleport(server, pos.getX, pos.getY, pos.getZ, player.getYaw(1.0f), player.getPitch(1.0f))
        player
      case None => players = players.updated(profile, new QuarryFakePlayer(server)); get(server, pos)
    }
  }

  @SubscribeEvent
  def onUnload(event: WorldEvent.Unload): Unit = {
    if (event.getWorld.isInstanceOf[ServerWorld]) {
      players = players.filter { case (_, p) => p.world != event.getWorld }
    }
  }
}
