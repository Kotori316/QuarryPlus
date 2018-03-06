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

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{GuiButton, GuiErrorScreen, GuiScreen}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class GuiError(val parent: GuiScreen, par2Str: String, par3Str: String) extends GuiErrorScreen(par2Str, par3Str) {
    override protected def actionPerformed(par1GuiButton: GuiButton): Unit = Minecraft.getMinecraft.displayGuiScreen(this.parent)

    override def doesGuiPauseGame = false

    override def updateScreen(): Unit = {
        super.updateScreen()
        if (!this.mc.player.isEntityAlive || this.mc.player.isDead) this.mc.player.closeScreen()
    }
}
