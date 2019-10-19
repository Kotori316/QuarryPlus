/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlusI
import com.yogpc.qp.container.ContainerQuarryModule.HasModuleInventory
import com.yogpc.qp.container._
import com.yogpc.qp.gui.GuiQuarryLevel._
import com.yogpc.qp.tile._
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.inventory.Container
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object GuiHandler extends IGuiHandler {

  val instance = this

  @SideOnly(Side.CLIENT)
  override def getClientGuiElement(ID: Int, p: EntityPlayer, w: World, x: Int, y: Int, z: Int): GuiContainer = {
    val pos = new BlockPos(x, y, z)
    ID match {
      case QuarryPlusI.guiIdMover =>
        new GuiMover(p, w, x, y, z)
      case QuarryPlusI.guiIdFList =>
        new GuiEnchList(Enchantments.FORTUNE, w.getTileEntity(pos).asInstanceOf[TileBasic], p)
      case QuarryPlusI.guiIdSList =>
        new GuiEnchList(Enchantments.SILK_TOUCH, w.getTileEntity(pos).asInstanceOf[TileBasic], p)
      case QuarryPlusI.guiIdPlacer =>
        new GuiPlacer(p.inventory, w.getTileEntity(pos).asInstanceOf[TilePlacer])
      case QuarryPlusI.guiIdWorkbench =>
        new GuiWorkbench(p, w.getTileEntity(pos).asInstanceOf[TileWorkbench])
      case QuarryPlusI.guiIdAdvQuarry =>
        new GuiAdvQuarry(w.getTileEntity(pos).asInstanceOf[TileAdvQuarry], p)
      case QuarryPlusI.guiIdAdvPump =>
        new GuiAdvPump(w.getTileEntity(pos).asInstanceOf[TileAdvPump], p)
      case QuarryPlusI.guiIdMoverFromBook =>
        new GuiBookMover(w.getTileEntity(pos).asInstanceOf[TileBookMover], p)
      case QuarryPlusI.guiIdSolidQuarry =>
        new GuiSolidQuarry(w.getTileEntity(pos).asInstanceOf[TileSolidQuarry], p)
      case QuarryPlusI.guiIdQuarryYLevel =>
        new GuiQuarryLevel(w.getTileEntity(pos).asInstanceOf[TileBasic], p)
      case QuarryPlusI.guiIdQuarry2YLevel =>
        new GuiQuarryLevel(w.getTileEntity(pos).asInstanceOf[TileQuarry2], p)
      case QuarryPlusI.guiIdAdvQuarryYLevel =>
        new GuiQuarryLevel(w.getTileEntity(pos).asInstanceOf[TileAdvQuarry], p)
      case QuarryPlusI.guiIdListTemplate =>
        new GuiListTemplate(p)
      case QuarryPlusI.guiIdQuarryModule =>
        new GuiQuarryModule(w.getTileEntity(pos).asInstanceOf[HasModuleInventory], p, w.getTileEntity(pos).getDisplayName.getFormattedText)
      case QuarryPlusI.guiIdStatus =>
        new StatusGui(p, pos)
      case _ => null
    }
  }

  override def getServerGuiElement(ID: Int, p: EntityPlayer, w: World, x: Int, y: Int, z: Int): Container = {
    val pos = new BlockPos(x, y, z)
    ID match {
      case QuarryPlusI.guiIdMover =>
        new ContainerMover(p.inventory, w, x, y, z)
      case QuarryPlusI.guiIdFList =>
        new ContainerEnchList(w.getTileEntity(pos).asInstanceOf[TileBasic], p)
      case QuarryPlusI.guiIdSList =>
        new ContainerEnchList(w.getTileEntity(pos).asInstanceOf[TileBasic], p)
      case QuarryPlusI.guiIdPlacer =>
        new ContainerPlacer(p.inventory, w.getTileEntity(pos).asInstanceOf[TilePlacer])
      case QuarryPlusI.guiIdWorkbench =>
        new ContainerWorkbench(p, w.getTileEntity(pos).asInstanceOf[TileWorkbench])
      case QuarryPlusI.guiIdAdvQuarry =>
        new ContainerAdvQuarry(w.getTileEntity(pos).asInstanceOf[TileAdvQuarry], p)
      case QuarryPlusI.guiIdAdvPump =>
        new ContainerAdvPump(w.getTileEntity(pos).asInstanceOf[TileAdvPump], p)
      case QuarryPlusI.guiIdMoverFromBook =>
        new ContainerBookMover(w.getTileEntity(pos).asInstanceOf[TileBookMover], p)
      case QuarryPlusI.guiIdSolidQuarry =>
        new ContainerSolidQuarry(w.getTileEntity(pos).asInstanceOf[TileSolidQuarry], p)
      case QuarryPlusI.guiIdQuarryYLevel =>
        new ContainerQuarryLevel(w.getTileEntity(pos).asInstanceOf[TileBasic], p)
      case QuarryPlusI.guiIdAdvQuarryYLevel =>
        new ContainerQuarryLevel(w.getTileEntity(pos).asInstanceOf[TileAdvQuarry], p)
      case QuarryPlusI.guiIdQuarry2YLevel =>
        new ContainerQuarryLevel(w.getTileEntity(pos).asInstanceOf[TileQuarry2], p)
      case QuarryPlusI.guiIdListTemplate =>
        new ContainerListTemplate(p)
      case QuarryPlusI.guiIdQuarryModule =>
        new ContainerQuarryModule(w.getTileEntity(pos).asInstanceOf[HasModuleInventory], p)
      case QuarryPlusI.guiIdStatus =>
        new StatusContainer(0, p, pos)
      case _ => null
    }
  }
}
