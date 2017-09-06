package com.yogpc.qp.item

import com.yogpc.qp.tile.{APowerTile, TileLaser, TileMarker, TileMiningWell, TilePlacer, TilePump, TileQuarry, TileWorkbench}
import com.yogpc.qp.{Config, QuarryPlus, QuarryPlusI}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World

class ItemQuarryDebug extends Item {
    setUnlocalizedName(QuarryPlus.Names.debug)
    setRegistryName(QuarryPlus.modID, QuarryPlus.Names.debug)
    setMaxStackSize(1)
    setCreativeTab(QuarryPlusI.ct)

    override def onItemUse(player: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand,
                           facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
        if (Config.content.debug) {
            val tile = worldIn.getTileEntity(pos)
            tile match {
                case laser: TileLaser =>
                    if (!worldIn.isRemote) {
                        player.sendStatusMessage(new TextComponentTranslation("tile.laserplus.name"), false)
                        player.sendStatusMessage(tileposToString(tile), false)
                        player.sendStatusMessage(energyToString(laser), false)
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
                case _ => super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ)

            }
        } else {
            player.sendStatusMessage(new TextComponentString("QuarryPlus debug is not enabled"), true)
            super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ)
        }
    }

    private def tileposToString(tile: TileEntity): ITextComponent = {
        new TextComponentString(s"Tile Pos : x=${tile.getPos.getX}, y=${tile.getPos.getY}, z=${tile.getPos.getZ}")
    }

    private def energyToString(tile: APowerTile): ITextComponent = {
        new TextComponentString(tile.getStoredEnergy + " / " + tile.getMaxStored + " MJ")
    }
}
