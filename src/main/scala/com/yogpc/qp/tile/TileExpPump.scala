package com.yogpc.qp.tile

import java.util

import com.yogpc.qp._
import com.yogpc.qp.block.{ADismCBlock, BlockExpPump, BlockPump}
import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.exppump.ExpPumpMessage
import javax.annotation.Nullable
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World

import scala.collection.JavaConverters._

class TileExpPump extends APacketTile with IEnchantableTile with IDebugSender with IAttachment {
  private[this] var mConnectTo: EnumFacing = _
  private[this] var loading = false

  private[this] var fortune = 0
  private[this] var unbreaking = 0
  private[this] var silktouch = false
  private val module: ExpPumpModule = new ExpPumpModule((_: Long) => true, () => this.unbreaking, None)

  override protected def getSymbol: Symbol = BlockExpPump.SYMBOL

  /**
    * Called after enchantment setting.
    */
  override def G_ReInit(): Unit = {
    refreshConnection()
  }

  def getModule = module

  override def onLoad(): Unit = {
    super.onLoad()
    if (loading) {
      refreshConnection()
      loading = false
    }
  }

  private def refreshConnection(): Unit = {
    if (hasWorld && !world.isRemote) {
      val entry = EnumFacing.VALUES
        .map(f => (f, getWorld.getTileEntity(getPos.offset(f))))
        .collectFirst {
          case (f: EnumFacing, t: IAttachable) if t.connect(f.getOpposite, IAttachment.Attachments.EXP_PUMP) => f -> t
        }.getOrElse((null, IAttachable.dummy))

      if (entry._1 != null && entry._2.connectAttachment(entry._1.getOpposite, IAttachment.Attachments.EXP_PUMP, false))
        setConnectTo(entry._1)
      S_sendNowPacket()
    }
  }

  override def setConnectTo(@Nullable connectTo: EnumFacing) {
    this.mConnectTo = connectTo
    if (hasWorld) {
      val state = getWorld.getBlockState(getPos)
      if (!working == state.getValue(BlockPump.CONNECTED)) {
        InvUtils.setNewState(getWorld, getPos, this, state.withProperty(BlockPump.CONNECTED, Boolean.box(working)))
      }
    }
  }

  private def S_sendNowPacket(): Unit = {
    PacketHandler.sendToAround(ExpPumpMessage.create(this), getWorld, getPos)
  }

  def working: Boolean = mConnectTo != null

  def addXp(amount: Int): Unit = {
    module.xp += amount
    if (module.xp > 0 ^ getWorld.getBlockState(getPos).getValue(ADismCBlock.ACTING)) {
      val state = getWorld.getBlockState(getPos).withProperty(ADismCBlock.ACTING, Boolean.box(module.xp > 0))
      InvUtils.setNewState(getWorld, getPos, this, state)
    }
  }

  def getEnergyUse(amount: Int): Long = {
    amount * 10 * APowerTile.MicroJtoMJ / (1 + unbreaking)
  }

  def onActivated(worldIn: World, pos: BlockPos, playerIn: EntityPlayer): Unit = {
    //on Server side
    if (module.xp > 0) {
      val xp = EntityXPOrb.getXPSplit(module.xp)
      val orb = new EntityXPOrb(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, xp)
      worldIn.spawnEntity(orb)
      addXp(-xp)
    }
  }

  def onBreak(worldIn: World): Unit = {
    if (module.xp > 0) {
      val xpOrb = new EntityXPOrb(worldIn, getPos.getX, getPos.getY, getPos.getZ, module.xp)
      worldIn.spawnEntity(xpOrb)
    }
  }

  /**
    * @return Map (Enchantment id, level)
    */
  override def getEnchantments: util.Map[Integer, Integer] = {
    Map(IEnchantableTile.FortuneID -> fortune,
      IEnchantableTile.UnbreakingID -> unbreaking,
      IEnchantableTile.SilktouchID -> silktouch.compareTo(false))
      .collect(enchantCollector).asJava
  }

  /**
    * @param id    Enchantment id
    * @param value level
    */
  override def setEnchantment(id: Short, value: Short): Unit = {
    id match {
      case IEnchantableTile.FortuneID => fortune = value
      case IEnchantableTile.UnbreakingID => unbreaking = value
      case IEnchantableTile.SilktouchID => silktouch = value > 0
      case _ =>
    }
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    compound.setByte("mConnectTo", mConnectTo.map(_.ordinal().toByte).getOrElse(-1))
    compound.setInteger("xpAmount", module.xp)
    compound.setBoolean("silktouch", this.silktouch)
    compound.setByte("fortune", this.fortune.toByte)
    compound.setByte("unbreaking", this.unbreaking.toByte)
    super.writeToNBT(compound)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    val connectID = compound.getByte("mConnectTo")
    mConnectTo = if (connectID < 0) null else EnumFacing.getFront(connectID)
    module.xp = compound.getInteger("xpAmount")
    this.silktouch = compound.getBoolean("silktouch")
    this.fortune = compound.getByte("fortune")
    this.unbreaking = compound.getByte("unbreaking")
    loading = true
  }

  override def getDebugName: String = TranslationKeys.exppump

  /**
    * For internal use only.
    *
    * @return debug info of valid machine.
    */
  override def getDebugMessages: util.List[TextComponentString] = Seq(
    "Connection -> " + mConnectTo,
    "Unbreaking -> " + unbreaking,
    "Fortune -> " + fortune,
    "Silktouch -> " + silktouch,
    "XpAmount -> " + module.xp
  ).map(toComponentString).asJava

  def writeToPacket(message: ExpPumpMessage): ExpPumpMessage = {
    message.pos = getPos
    message.dim = getWorld.provider.getDimension
    message.xpAmount = module.xp
    message.facingOrdinal = Option(mConnectTo).fold(-1)(_.ordinal())
    message
  }

  def onMessage(message: ExpPumpMessage): Unit = {
    mConnectTo = Some(message.facingOrdinal).collect {
      case i if i >= 0 => EnumFacing.getFront(i)
      case _ => null
    }.get
    module.xp = message.xpAmount
  }
}
