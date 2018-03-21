package com.yogpc.qp.item

import java.util

import com.yogpc.qp.tile.{APowerTile, IDebugSender, TileMarker, TilePlacer, TilePump}
import com.yogpc.qp.{Config, QuarryPlus, QuarryPlusI}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

class ItemQuarryDebug extends Item {

    import ItemQuarryDebug._

    setUnlocalizedName(QuarryPlus.Names.debug)
    setRegistryName(QuarryPlus.modID, QuarryPlus.Names.debug)
    setMaxStackSize(1)
    setCreativeTab(QuarryPlusI.creativeTab)

    def onItemUseFirst(player: EntityPlayer, worldIn: World, pos: BlockPos, side: EnumFacing,
                       hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult = {
        if (worldIn.isRemote) {
            EnumActionResult.PASS
        } else {
            if (Config.content.debug || QuarryPlus.instance().inDev) {
                val tile = worldIn.getTileEntity(pos)
                tile match {
                    case plusMachine: APowerTile with IDebugSender =>
                        if (!worldIn.isRemote) {
                            if (player.isSneaking) {
                                plusMachine.toggleOutputEnergyInfo()
                            } else {
                                player.sendStatusMessage(new TextComponentTranslation(plusMachine.getDebugName), false)
                                player.sendStatusMessage(tileposToString(tile), false)
                                player.sendStatusMessage(energyToString(plusMachine), false)
                                plusMachine.sendDebugMessage(player)
                            }
                        }
                        EnumActionResult.SUCCESS
                    case marker: TileMarker =>
                        if (!worldIn.isRemote) {
                            player.sendStatusMessage(new TextComponentTranslation(marker.getDebugName), false)
                            player.sendStatusMessage(tileposToString(tile), false)
                            marker.sendDebugMessage(player)
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
                    case _ => super.onItemUseFirst(player.getHeldItem(hand), player, worldIn, pos, side, hitX, hitY, hitZ, hand)
                }
            } else {
                player.sendStatusMessage(new TextComponentString("QuarryPlus debug is not enabled"), true)
                super.onItemUseFirst(player.getHeldItem(hand), player, worldIn, pos, side, hitX, hitY, hitZ, hand)
            }
        }
    }

    override def onItemUseFirst(stack: ItemStack, player: EntityPlayer, world: World, pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand): EnumActionResult = {
        onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand)
    }

    @SideOnly(Side.CLIENT)
    override def getSubItems(itemIn: Item, tab: CreativeTabs, subItems: util.List[ItemStack]) = {
        if (Config.content.debug)
            super.getSubItems(itemIn, tab, subItems)
    }

}

object ItemQuarryDebug {

    def tileposToString(tile: TileEntity) = {
        new TextComponentString(s"Tile Pos : x=${tile.getPos.getX}, y=${tile.getPos.getY}, z=${tile.getPos.getZ}")
    }

    def energyToString(tile: APowerTile) = {
        if (Config.content.noEnergy) new TextComponentString("No Energy Required.")
        else new TextComponentString(tile.getStoredEnergy + " / " + tile.getMaxStored + " MJ")
    }

    private implicit class PH(val player: EntityPlayer) extends AnyVal {
        def sendStatusMessage(t: ITextComponent, actionBar: Boolean): Unit = {
            player.addChatComponentMessage(t)
        }
    }

}
