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
import com.yogpc.qp.container.{ContainerAdvPump, ContainerAdvQuarry, ContainerEnchList, ContainerMover, ContainerPlacer, ContainerWorkbench}
import com.yogpc.qp.tile.{TileAdvPump, TileAdvQuarry, TileBasic, TilePlacer, TileWorkbench}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object GuiHandler extends IGuiHandler {

    val instance = this

    @SideOnly(Side.CLIENT)
    override def getClientGuiElement(ID: Int, p: EntityPlayer, w: World, x: Int, y: Int, z: Int) = {
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
                new GuiWorkbench(p.inventory, w.getTileEntity(pos).asInstanceOf[TileWorkbench])
            case QuarryPlusI.guiIdAdvQuarry =>
                new GuiAdvQuarry(w.getTileEntity(pos).asInstanceOf[TileAdvQuarry], p)
            case QuarryPlusI.guiIdAdvPump =>
                new GuiAdvPump(w.getTileEntity(pos).asInstanceOf[TileAdvPump], p)
            case _ => null
        }
    }

    override def getServerGuiElement(ID: Int, p: EntityPlayer, w: World, x: Int, y: Int, z: Int) = {
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
                new ContainerWorkbench(p.inventory, w.getTileEntity(pos).asInstanceOf[TileWorkbench])
            case QuarryPlusI.guiIdAdvQuarry =>
                new ContainerAdvQuarry(w.getTileEntity(pos).asInstanceOf[TileAdvQuarry], p)
            case QuarryPlusI.guiIdAdvPump =>
                new ContainerAdvPump(w.getTileEntity(pos).asInstanceOf[TileAdvPump], p)
            case _ => null
        }
    }
}
