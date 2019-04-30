package com.yogpc.qp.machines.quarry

import java.io.File
import java.util.UUID

import com.mojang.authlib.GameProfile
import net.minecraft.advancements.{Advancement, AdvancementProgress, PlayerAdvancements}
import net.minecraft.entity.IMerchant
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.tileentity.{TileEntityCommandBlock, TileEntitySign}
import net.minecraft.util.EnumHand
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.{IInteractionObject, WorldServer}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class QuarryFakePlayer private(worldServer: WorldServer) extends FakePlayer(worldServer, QuarryFakePlayer.profile) {
  this.connection = new FakeHandler(this)

  private[this] val advancements = new PlayerAdvancements(
    worldServer.getServer,
    new File(new File(worldServer.getServer.getWorld(DimensionType.OVERWORLD).getSaveHandler.getWorldDirectory, "advancements"), getUniqueID + ".json"),
    this) {
    override def getProgress(advancementIn: Advancement): AdvancementProgress = {
      new AdvancementProgress() {
        override def isDone: Boolean = true
      }
    }
  }

  override def openSignEditor(signTile: TileEntitySign): Unit = ()

  override def openHorseInventory(horse: AbstractHorse, inventoryIn: IInventory): Unit = ()

  override def openCommandBlock(commandBlock: TileEntityCommandBlock): Unit = ()

  override def displayGUIChest(chestInventory: IInventory): Unit = ()

  override def displayGui(guiOwner: IInteractionObject): Unit = ()

  override def displayVillagerTradeGui(villager: IMerchant): Unit = ()

  override def openBook(stack: ItemStack, hand: EnumHand): Unit = ()

  override def playEquipSound(stack: ItemStack): Unit = ()

  override def isSilent: Boolean = true

  override def getAdvancements: PlayerAdvancements = advancements

  override def isPotionApplicable(effect: PotionEffect): Boolean = false
}

object QuarryFakePlayer {
  val profile = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]")
  private var players = Map.empty[GameProfile, QuarryFakePlayer]
  MinecraftForge.EVENT_BUS.register(this)

  def get(server: WorldServer): QuarryFakePlayer = {
    players.getOrElse(profile, new QuarryFakePlayer(server).tap(p => players = players updated(profile, p)))
  }

  @SubscribeEvent
  def onUnload(event: WorldEvent.Unload): Unit = {
    if (event.getWorld.isInstanceOf[WorldServer]) {
      players = players.filter { case (_, p) => p.world != event.getWorld }
    }
  }
}
