package com.yogpc.qp.machines.mini_quarry

import java.util

import cats.implicits._
import com.mojang.datafixers.Dynamic
import com.yogpc.qp._
import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.modules.{IModuleItem, ItemFuelModule}
import com.yogpc.qp.machines.quarry.QuarryFakePlayer
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.utils.Holder
import net.minecraft.block.Block
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.container.{Container, INamedContainerProvider}
import net.minecraft.inventory.{IInventory, InventoryHelper, ItemStackHelper}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, StringTextComponent}
import net.minecraft.util.{Hand, NonNullList, ResourceLocation}
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.util.Constants.NBT

import scala.jdk.CollectionConverters._

class MiniQuarryTile extends APowerTile(Holder.miniQuarryType)
  with EnchantmentHolder.EnchantmentProvider
  with IEnchantableTile
  with IRemotePowerOn
  with IChunkLoadTile
  with INamedContainerProvider
  with IDebugSender {
  private final var enchantments = EnchantmentHolder.noEnch
  private final var area = Area.zeroArea
  private final var targets: List[BlockPos] = List.empty
  private final val tools = NonNullList.withSize(5, ItemStack.EMPTY)
  private final var blackList: Set[QuarryBlackList.Entry] = Set(QuarryBlackList.Air)
  final var rs = false
  private[this] final val dropItem = (item: ItemStack) => {
    val rest = InvUtils.injectToNearTile(world, pos, item)
    InventoryHelper.spawnItemStack(world, pos.getX + 0.5, pos.getY + 1, pos.getZ + 0.5, rest)
  }

  override protected def workInTick(): Unit = {
    if (world.getGameTime % MiniQuarryTile.interval(enchantments.efficiency) == 0 &&
      PowerManager.useEnergy(this, MiniQuarryTile.e(enchantments.unbreaking), EnergyUsage.MINI_QUARRY)) {
      // Work
      @scala.annotation.tailrec
      def work(poses: List[BlockPos]): List[BlockPos] = {
        poses match {
          case head :: tail =>
            val world = getDiggingWorld
            val state = world.getBlockState(head)
            if (blackList.exists(_.test(state, world, head)) ||
              state.getBlockHardness(world, head) < 0) {
              // Unbreakable
              work(tail)
            } else {
              // Check if block harvest-able.
              val fakePlayer = QuarryFakePlayer.get(world, head)
              val canHarvest = tools.asScala.filterNot(_.getItem.isInstanceOf[IModuleItem]).exists { tool =>
                fakePlayer.setHeldItem(Hand.MAIN_HAND, tool)
                ForgeHooks.canHarvestBlock(state, fakePlayer, world, head) || ForgeHooks.isToolEffective(world, head, tool)
              }
              // Use effective tool
              tools.asScala.filterNot(_.getItem.isInstanceOf[IModuleItem]).find(tool => ForgeHooks.isToolEffective(world, head, tool))
                .foreach(fakePlayer.setHeldItem(Hand.MAIN_HAND, _))
              if (canHarvest) {
                // Remove block
                val drops = Block.getDrops(state, world, head, world.getTileEntity(head), fakePlayer, fakePlayer.getHeldItemMainhand)
                drops.asScala.foreach(dropItem)
                val count = if (ForgeHooks.isToolEffective(world, head, fakePlayer.getHeldItemMainhand)) 1 else 4
                for (_ <- 0 until count)
                  fakePlayer.getHeldItemMainhand.onBlockDestroyed(world, state, head, fakePlayer)
                world.removeBlock(head, false)
                tail
              } else {
                // Skip this block
                tail
              }
            }
          case Nil => Nil
        }
      }

      targets = work(targets)
      if (targets.isEmpty)
        updateWorkingState()
    }
  }

  override protected def getEnergyInTick(): Unit = {
    tools.asScala.map(i => (i.getItem, i)).collectFirst { case (f: ItemFuelModule, s) => f.getFuelModule(s) }.foreach {
      f => f.invoke(IModule.Tick(this))
    }
  }

  def getDiggingWorld: ServerWorld = {
    if (!super.getWorld.isRemote) {
      this.area.getWorld(super.getWorld.asInstanceOf[ServerWorld])
    } else {
      throw new IllegalStateException("Tried to get server world in client.")
    }
  }

  def gotRSPulse(): Unit = {
    if (isWorking) {
      startWaiting()
    } else {
      startWorking()
    }
  }

  def getInv: IInventory = Inv

  override protected def isWorking: Boolean = targets.nonEmpty

  override def getEnchantmentHolder: EnchantmentHolder = enchantments

  override def G_ReInit(): Unit = {
    PowerManager.configureQuarryWork(this, 0, 0, 0)
  }

  override def getEnchantments: util.Map[ResourceLocation, Integer] =
    EnchantmentHolder.getEnchantmentMap(enchantments).collect(enchantCollector).asJava

  override def setEnchantment(id: ResourceLocation, level: Short): Unit = enchantments =
    EnchantmentHolder.updateEnchantment(enchantments, id, level)

  override def setArea(area: Area): Unit = this.area = area

  override def startWorking(): Unit = {
    val facing = world.getBlockState(pos).get(BlockStateProperties.FACING)
    val maybeMarkers = Area.getMarkersOnDirection(List(facing.getOpposite, facing.rotateY(), facing.rotateYCCW()), world, pos)
    val areas = maybeMarkers.map(m => Area.posToArea(m.min(), m.max(), world.getDimension.getType) -> m)
      .collectFirst(t => t)
    areas match {
      case Some((newArea, m)) =>
        area = newArea
        m.removeFromWorldWithItem().asScala.foreach(dropItem)
      case _ =>
    }
    if (area == Area.zeroArea) {
      targets = List.empty
    } else {
      val poses = for {
        y <- area.yMax to(area.yMin, -1)
        x <- area.xMin to area.xMax
        z <- area.zMin to area.zMax
      } yield new BlockPos(x, y, z)
      targets = poses.toList
    }
    updateWorkingState()
  }

  override def startWaiting(): Unit = {
    targets = List.empty
    updateWorkingState()
  }

  override def createMenu(id: Int, i: PlayerInventory, player: PlayerEntity): Container = new MiniQuarryContainer(id, player, pos)

  override def getDebugName: String = TranslationKeys.mini_quarry

  override def getDebugMessages: util.List[_ <: ITextComponent] = Seq(
    s"Area: $area}",
    s"TargetSize: ${targets.size}",
  ).map(new StringTextComponent(_)).asJava

  override def getDisplayName: ITextComponent = super.getDisplayName

  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def read(nbt: CompoundNBT): Unit = {
    super.read(nbt)
    this.area = Area.areaLoad(nbt.getCompound("area"))
    this.enchantments = EnchantmentHolder.enchantmentHolderLoad(nbt, "enchantments")
    val working = nbt.contains("head")
    if (working) {
      startWorking()
      val head = BlockPos.fromLong(nbt.getLong("head"))
      this.targets = targets.dropWhile(_ == head)
    }
    ItemStackHelper.loadAllItems(nbt.getCompound("tools"), tools)
    this.blackList = nbt.getList("blackList", NBT.TAG_COMPOUND).asScala
      .map(n => QuarryBlackList.readEntry(new Dynamic(NBTDynamicOps.INSTANCE, n))).toSet
    this.rs = nbt.getBoolean("rs")
  }

  override def write(nbt: CompoundNBT): CompoundNBT = {
    nbt.put("area", area.toNBT)
    nbt.put("enchantments", enchantments.toNBT)
    targets.headOption.foreach(p => nbt.putLong("head", p.toLong))
    nbt.put("tools", ItemStackHelper.saveAllItems(new CompoundNBT(), tools))
    nbt.put("blackList", NBTDynamicOps.INSTANCE.createList(blackList.asJava.stream().map(QuarryBlackList.writeEntry(_, NBTDynamicOps.INSTANCE))))
    nbt.putBoolean("rs", rs)
    super.write(nbt)
  }

  override protected def enabledByRS = true

  private object Inv extends IInventory {
    override def getSizeInventory: Int = tools.size()

    override def isEmpty: Boolean = tools.stream().allMatch(_.isEmpty)

    override def getStackInSlot(index: Int): ItemStack = tools.get(index)

    override def decrStackSize(index: Int, count: Int): ItemStack = ItemStackHelper.getAndSplit(tools, index, count)

    override def removeStackFromSlot(index: Int): ItemStack = ItemStackHelper.getAndRemove(tools, index)

    override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = tools.set(index, stack)

    override def markDirty(): Unit = MiniQuarryTile.this.markDirty()

    override def isUsableByPlayer(player: PlayerEntity): Boolean = true

    override def clear(): Unit = tools.clear()
  }

}

object MiniQuarryTile {
  final val SYMBOL = Symbol("MiniQuarry")

  def e(unbreaking: Int): Long = APowerTile.FEtoMicroJ * 20 / (unbreaking + 1)

  def interval(efficiency: Int): Int = {
    if (efficiency < 0) return 100
    efficiency match {
      case 0 => 40
      case 1 => 30
      case 2 => 20
      case 3 => 10
      case 4 => 5
      case 5 => 2
      case _ => 1
    }
  }

}
