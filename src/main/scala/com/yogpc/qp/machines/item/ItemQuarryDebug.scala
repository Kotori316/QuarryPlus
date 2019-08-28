package com.yogpc.qp.machines.item

import com.yogpc.qp.machines.base.{APowerTile, IDebugSender}
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.item.{Item, ItemGroup, ItemStack, ItemUseContext}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{ActionResultType, NonNullList}

class ItemQuarryDebug extends Item((new Item.Properties).group(Holder.tab)) {

  import ItemQuarryDebug._

  setRegistryName(QuarryPlus.modID, QuarryPlus.Names.debug)

  override def onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType = {
    val worldIn = context.getWorld
    val pos = context.getPos
    val player = context.getPlayer

    if (worldIn.isRemote) {
      ActionResultType.PASS
    } else {
      if (Config.common.debug) {
        val tile = worldIn.getTileEntity(pos)
        tile match {
          case plusMachine: APowerTile with IDebugSender =>
            if (!worldIn.isRemote) {
              if (player.isSneaking) {
                plusMachine.toggleOutputEnergyInfo()
              } else {
                player.sendStatusMessage(new TranslationTextComponent(plusMachine.getDebugName), false)
                player.sendStatusMessage(tilePosToString(tile), false)
                player.sendStatusMessage(energyToString(plusMachine), false)
                plusMachine.sendDebugMessage(player)
              }
            }
            ActionResultType.SUCCESS
          case sender: IDebugSender =>
            if (!worldIn.isRemote) {
              player.sendStatusMessage(new TranslationTextComponent(sender.getDebugName), false)
              player.sendStatusMessage(tilePosToString(sender), false)
              sender.sendDebugMessage(player)
            }
            ActionResultType.SUCCESS
          /*case placer: TilePlacer =>
            if (!worldIn.isRemote) {
              player.sendStatusMessage(new TranslationTextComponent(placer.getName), false)
              player.sendStatusMessage(tilePosToString(tile), false)
            }
            EnumActionResult.SUCCESS
          case pump: TilePump =>
            if (!worldIn.isRemote) {
              player.sendStatusMessage(new TranslationTextComponent(pump.getDebugName), false)
              player.sendStatusMessage(tilePosToString(tile), false)
              pump.sendDebugMessage(player)
            }
            EnumActionResult.SUCCESS*/
          case _ => super.onItemUseFirst(stack, context)
        }
      } else {
        player.sendStatusMessage(new StringTextComponent("QuarryPlus debug is not enabled"), true)
        super.onItemUseFirst(stack, context)
      }
    }
  }

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (Config.common.debug) super.fillItemGroup(group, items)
  }

}

object ItemQuarryDebug {

  def tilePosToString(tile: TileEntity): ITextComponent = {
    new StringTextComponent(s"Tile Pos : x=${tile.getPos.getX}, y=${tile.getPos.getY}, z=${tile.getPos.getZ}")
  }

  def energyToString(tile: APowerTile): ITextComponent = {
    if (Config.common.noEnergy.get()) new StringTextComponent("No Energy Required.")
    else new StringTextComponent(tile.getStoredEnergy / APowerTile.MicroJtoMJ + " / " + tile.getMaxStored / APowerTile.MicroJtoMJ + " MJ")
  }
}