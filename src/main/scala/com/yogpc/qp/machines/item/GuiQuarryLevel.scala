package com.yogpc.qp.machines.item

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.advquarry.TileAdvQuarry
import com.yogpc.qp.machines.base.{IDebugSender, IHandleButton, ScreenUtil}
import com.yogpc.qp.machines.quarry.{TileBasic, TileQuarry, TileQuarry2}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvLevelMessage
import com.yogpc.qp.packet.quarry.LevelMessage
import com.yogpc.qp.packet.quarry2.Level2Message
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.{INameable, ResourceLocation}

class GuiQuarryLevel(c: ContainerQuarryLevel, inv: PlayerInventory, t: ITextComponent)
  extends ContainerScreen[ContainerQuarryLevel](c, inv, t)
    with IHandleButton {

  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")
  val tile: TileEntity = c.tile
  val func: TileEntity => _ <: LevelMessage = c.messageFunc
  val lA: GuiQuarryLevel.YLevel[TileEntity] = GuiQuarryLevel.YLevel.get(tile)
  val tp = 15
  val tileName: String = tile match {
    case n: INameable => n.getName.getString
    case d: IDebugSender => d.getDebugName
    case _ => "YSetter"
  }

  override def func_230430_a_(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = { // render
    this.func_230446_a_(matrixStack) // back ground
    super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks) // super.render
    this.func_230459_a_(matrixStack, mouseX, mouseY) // render tooltip
  }

  override def func_230450_a_(matrix: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    ScreenUtil.color4f()
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.func_238474_b_(matrix, guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  override def func_231160_c_(): Unit = {
    super.func_231160_c_()
    val width = 40
    func_230480_a_(new IHandleButton.Button(0, guiLeft + this.xSize / 2 - width / 2, guiTop + tp, width, 20, "+", this))
    func_230480_a_(new IHandleButton.Button(1, guiLeft + this.xSize / 2 - width / 2, guiTop + tp + 33, width, 20, "-", this))
  }

  override def func_230451_b_(matrix: MatrixStack, mouseX: Int, mouseY: Int): Unit = {
    super.func_230451_b_(matrix, mouseX, mouseY)

    this.field_230712_o_.func_238421_b_(matrix, lA.getYLevel(tile).toString, (this.xSize / 2 - this.field_230712_o_.getStringWidth(lA.getYLevel(tile).toString) / 2).toFloat, tp.toFloat + 23, 0x404040)
  }

  override def actionPerformed(button: IHandleButton.Button): Unit = {
    val di = (if (button.id % 2 == 0) 1 else -1) * (if (Screen.func_231172_r_) 10 else 1) // ctrl
    val yMin = tile match {
      case quarry: TileQuarry => quarry.yMin
      //      case quarry2: TileQuarry2 => quarry2.area.yMin
      case _ => tile.getPos.getY
    }
    if (yMin > lA.getYLevel(tile) + di && lA.getYLevel(tile) + di >= GuiQuarryLevel.y_min) {
      lA.add(tile, di)
      PacketHandler.sendToServer(func(tile))
    }
  }

  override def func_231164_f_(): Unit = {
    super.func_231164_f_()
    PacketHandler.sendToServer(func(tile))
  }
}

object GuiQuarryLevel {
  final val y_min = 0

  trait YLevel[-T] {
    def setYLevel(t: T, yLevel: Int): Unit

    def getYLevel(t: T): Int

    def add(t: T, di: Int): Unit = {
      setYLevel(t, getYLevel(t) + di)
    }
  }

  object YLevel {
    def get(tile: TileEntity): YLevel[TileEntity] = {
      tile match {
        case _: TileBasic => implicitly[YLevel[TileBasic]]
        case _: TileAdvQuarry => implicitly[YLevel[TileAdvQuarry]]
        case _: TileQuarry2 => implicitly[YLevel[TileQuarry2]]
      }
    }.asInstanceOf[YLevel[TileEntity]]
  }

  implicit val BasicY: YLevel[TileBasic] = new YLevel[TileBasic] {
    override def setYLevel(t: TileBasic, yLevel: Int): Unit = t.yLevel = yLevel

    override def getYLevel(t: TileBasic): Int = t.yLevel
  }

  implicit val AdvY: YLevel[TileAdvQuarry] = new YLevel[TileAdvQuarry] {
    override def setYLevel(t: TileAdvQuarry, yLevel: Int): Unit = t.yLevel = yLevel

    override def getYLevel(t: TileAdvQuarry): Int = t.yLevel
  }
  implicit val NQuarryY: YLevel[TileQuarry2] = new YLevel[TileQuarry2] {
    override def setYLevel(t: TileQuarry2, yLevel: Int): Unit = t.yLevel = yLevel

    override def getYLevel(t: TileQuarry2): Int = t.yLevel
  }

  implicit val basicMessage: TileBasic => LevelMessage = LevelMessage.create
  implicit val advMessage: TileAdvQuarry => AdvLevelMessage = AdvLevelMessage.create
  implicit val quarryMessage: TileQuarry2 => Level2Message = Level2Message.create
}
