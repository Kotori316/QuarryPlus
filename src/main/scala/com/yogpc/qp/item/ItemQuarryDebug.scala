package com.yogpc.qp.item

import com.yogpc.qp.tile.{APowerTile, TileLaser, TileMarker, TileMiningWell, TilePlacer, TilePump, TileQuarry, TileWorkbench}
import com.yogpc.qp.{Config, QuarryPlus, QuarryPlusI}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.World

object ItemQuarryDebug extends Item {
    val item = this
    setUnlocalizedName(QuarryPlus.Names.debug)
    setRegistryName(QuarryPlus.modID, QuarryPlus.Names.debug)
    setMaxStackSize(1)
    setCreativeTab(QuarryPlusI.ct)

    override def onItemUseFirst(player: EntityPlayer, worldIn: World, pos: BlockPos, side: EnumFacing,
                                hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult = {
        if (worldIn.isRemote) {
            EnumActionResult.PASS
        } else {
            if (Config.content.debug) {
                val tile = worldIn.getTileEntity(pos)
                tile match {
                    case laser: TileLaser =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation("tile.laserplus.name"), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            player.sendStatusMessage(energyToString(laser), false)
                            laser.sendDebugMessage(player)
                        }
                        EnumActionResult.SUCCESS
                    case marker: TileMarker =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation("tile.markerplus.name"), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            player.sendStatusMessage(new TextComponentString("Link : " + marker.link), false)
                            player.sendStatusMessage(new TextComponentString("Laser : " + marker.laser), false)
                        }
                        EnumActionResult.SUCCESS
                    case miningwell: TileMiningWell =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(miningwell.getName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            player.sendStatusMessage(energyToString(miningwell), false)
                        }
                        EnumActionResult.SUCCESS
                    case placer: TilePlacer =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(placer.getName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                        }
                        EnumActionResult.SUCCESS
                    case pump: TilePump =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation("tile.pumpplus.name"), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            pump.sendDebugMessage(player)
                        }
                        EnumActionResult.SUCCESS
                    case quarry: TileQuarry =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(quarry.getName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            quarry.sendDebugMessage(player)
                        }
                        EnumActionResult.SUCCESS
                    case workbench: TileWorkbench =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(workbench.getName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            player.sendStatusMessage(energyToString(workbench), false)
                            player.sendStatusMessage(new TextComponentString(workbench.currentRecipe.scalaMap(_.toString).getOrElse("No recipe.")), false)
                        }
                        EnumActionResult.SUCCESS
                    case _ => super.onItemUseFirst(player, worldIn, pos, side, hitX, hitY, hitZ, hand)

                }
            } else {
                player.sendStatusMessage(new TextComponentString("QuarryPlus debug is not enabled"), true)
                super.onItemUseFirst(player, worldIn, pos, side, hitX, hitY, hitZ, hand)
            }
        }
    }


    override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
        if (Config.content.debug) super.getSubItems(tab, items)
    }

    private def tileposToString(tile: TileEntity): ITextComponent = {
        new TextComponentString(s"Tile Pos : x=${tile.getPos.getX}, y=${tile.getPos.getY}, z=${tile.getPos.getZ}")
    }

    private def energyToString(tile: APowerTile): ITextComponent = {
        if (Config.content.noEnergy) new TextComponentString("No Energy Required.")
        else new TextComponentString(tile.getStoredEnergy + " / " + tile.getMaxStored + " MJ")
    }
}
