package com.yogpc.qp.tile

import java.util.UUID

import com.mojang.authlib.GameProfile
import net.minecraft.entity.IMerchant
//import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.{TileEntityCommandBlock, TileEntitySign}
import net.minecraft.util.EnumHand
import net.minecraft.world.{IInteractionObject, WorldServer}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class QuarryFakePlayer private(worldServer: WorldServer) extends FakePlayer(worldServer, QuarryFakePlayer.profile) {
    //    override def openGuiHorseInventory(horse: AbstractHorse, inventoryIn: IInventory): Unit = ()

    override def displayGUIChest(chestInventory: IInventory): Unit = ()

    override def displayGui(guiOwner: IInteractionObject): Unit = ()

    override def displayVillagerTradeGui(villager: IMerchant): Unit = ()

    override def displayGuiCommandBlock(commandBlock: TileEntityCommandBlock): Unit = ()

    override def openBook(stack: ItemStack, hand: EnumHand): Unit = ()

    override def openEditSign(signTile: TileEntitySign): Unit = ()

    override def playEquipSound(stack: ItemStack): Unit = ()

    override def isSilent: Boolean = true
}

object QuarryFakePlayer {
    val profile = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]")
    private var players = Map.empty[GameProfile, QuarryFakePlayer]
    MinecraftForge.EVENT_BUS.register(this)

    def get(server: WorldServer): QuarryFakePlayer = {
        players.getOrElse(profile, {
            val newPlayer = new QuarryFakePlayer(server)
            players = players updated(profile, newPlayer)
            newPlayer
        })
    }

    @SubscribeEvent
    def onUnload(event: WorldEvent.Unload): Unit = {
        if (event.getWorld.isInstanceOf[WorldServer]) {
            players = players.filter { case (_, p) => p.worldObj != event.getWorld }
        }
    }
}
