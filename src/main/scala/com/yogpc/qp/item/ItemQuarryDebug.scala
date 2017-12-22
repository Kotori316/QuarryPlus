package com.yogpc.qp.item

import com.yogpc.qp.tile.{APowerTile, IDebugSender, TileMarker, TilePlacer, TilePump}
import com.yogpc.qp.{Config, QuarryPlus, QuarryPlusI}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

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
            if (Config.content.debug || QuarryPlus.instance().inDev) {
                val tile = worldIn.getTileEntity(pos)
                tile match {
                    case plusMachine: APowerTile with IDebugSender =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(plusMachine.getDebugName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            player.sendStatusMessage(energyToString(plusMachine), false)
                            plusMachine.sendDebugMessage(player)
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
                    case placer: TilePlacer =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(placer.getName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                        }
                        EnumActionResult.SUCCESS
                    case pump: TilePump =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(pump.getDebugName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            pump.sendDebugMessage(player)
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


    @SideOnly(Side.CLIENT)
    override def getSubItems(itemIn: Item, tab: CreativeTabs, subItems: NonNullList[ItemStack]) = {
        if (Config.content.debug)
            super.getSubItems(itemIn, tab, subItems)
    }

    def tileposToString(tile: TileEntity) = {
        new TextComponentString(s"Tile Pos : x=${tile.getPos.getX}, y=${tile.getPos.getY}, z=${tile.getPos.getZ}")
    }

    def energyToString(tile: APowerTile) = {
        if (Config.content.noEnergy) new TextComponentString("No Energy Required.")
        else new TextComponentString(tile.getStoredEnergy + " / " + tile.getMaxStored + " MJ")
    }
}
