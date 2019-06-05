package com.yogpc.qp.machines

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.advpump.{GuiAdvPump, TileAdvPump}
import com.yogpc.qp.machines.advquarry.{GuiAdvQuarry, TileAdvQuarry}
import com.yogpc.qp.machines.bookmover.{BlockBookMover, GuiBookMover, TileBookMover}
import com.yogpc.qp.machines.item.GuiQuarryLevel._
import com.yogpc.qp.machines.item.{GuiEnchList, GuiListTemplate, GuiQuarryLevel, ItemListEditor, ItemTemplate, YSetterInteractionObject}
import com.yogpc.qp.machines.mover.{BlockMover, GuiMover}
import com.yogpc.qp.machines.quarry.{GuiSolidQuarry, TileBasic, TileSolidQuarry}
import com.yogpc.qp.machines.workbench.{GuiWorkbench, TileWorkbench}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.init.Enchantments
import net.minecraftforge.fml.network.FMLPlayMessages

object GuiHandler {
  def getGui(context: FMLPlayMessages.OpenContainer): GuiScreen = {
    val pos = context.getAdditionalData.readBlockPos()
    val tile = Option(Minecraft.getInstance().world.getTileEntity(pos))
    val player = Minecraft.getInstance().player

    val screen = context.getId.toString match {
      case TileWorkbench.GUI_ID => tile.collect { case workbench: TileWorkbench => new GuiWorkbench(player, workbench) }
      case BlockMover.GUI_ID => Some(new GuiMover(player, player.world, pos))
      case YSetterInteractionObject.GUI_ID => tile.collect {
        case basic: TileBasic => new GuiQuarryLevel(basic, player)
        case quarry: TileAdvQuarry => new GuiQuarryLevel(quarry, player)
      }
      case ItemListEditor.GUI_ID_Fortune => tile.collect { case basic: TileBasic => new GuiEnchList(Enchantments.FORTUNE, basic, player) }
      case ItemListEditor.GUI_ID_Silktouch => tile.collect { case basic: TileBasic => new GuiEnchList(Enchantments.SILK_TOUCH, basic, player) }
      case TileSolidQuarry.GUI_ID => tile.collect { case solidQuarry: TileSolidQuarry => new GuiSolidQuarry(solidQuarry, player) }
      case BlockBookMover.GUI_ID => tile.collect { case bookMover: TileBookMover => new GuiBookMover(bookMover, player) }
      case TileAdvQuarry.GUI_ID => tile.collect { case quarry: TileAdvQuarry => new GuiAdvQuarry(quarry, player) }
      case TileAdvPump.GUI_ID => tile.collect { case pump: TileAdvPump => new GuiAdvPump(pump, player) }
      case ItemTemplate.GUI_ID => Some(new GuiListTemplate(player))
      case _ => QuarryPlus.LOGGER.error(s"Unknown GUI ID ${context.getId}."); None
    }
    screen.orNull
  }

}
