package com.yogpc.qp.machines.item

import java.util

import cats.data._
import cats.implicits._
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base.IEnchantableItem
import com.yogpc.qp.machines.item.ItemListEditor._
import com.yogpc.qp.machines.quarry.TileBasic
import com.yogpc.qp.machines.workbench.BlockData
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{QuarryPlus, _}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP, InventoryPlayer}
import net.minecraft.init.Enchantments
import net.minecraft.inventory.Container
import net.minecraft.item.{Item, ItemGroup, ItemStack, ItemUseContext}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.text.{ITextComponent, TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumActionResult, NonNullList}
import net.minecraft.world.{IInteractionObject, World}
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fml.network.NetworkHooks

import scala.collection.JavaConverters._

class ItemTemplate extends Item(new Item.Properties().maxStackSize(1).group(Holder.tab)) with IEnchantableItem {

  import ItemTemplate._

  setRegistryName(QuarryPlus.modID, QuarryPlus.Names.template)

  /**
    * You should not think max enchantment level in this method
    *
    * @param is          target ItemStack. It is never null.
    * @param enchantment target enchantment
    * @return that ItemStack can move enchantment on EnchantMover
    */
  override def canMove(is: ItemStack, enchantment: Enchantment) = {
    val l = is.getEnchantmentTagList
    (l == null || l.isEmpty) && ((enchantment eq Enchantments.SILK_TOUCH) || (enchantment eq Enchantments.FORTUNE))
  }

  /**
    * Called to get which items to show in JEI.
    *
    * @return stack which can be enchanted.
    */
  override def stacks() = Array(getEditorStack)

  override def isValidInBookMover = false

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    val enchantment = ItemTemplate.enchantmentName(stack)
    enchantment.foreach(tooltip.add)
  }

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (this.isInGroup(group)) {
      items.add(getEditorStack)
    }
  }

  override def onItemUseFirst(stack: ItemStack, context: ItemUseContext): EnumActionResult = {
    val worldIn = context.getWorld
    val pos = context.getPos
    val playerIn = context.getPlayer
    worldIn.getTileEntity(pos) match {
      case basic: TileBasic =>
        if (!worldIn.isRemote) {
          val template = ItemTemplate.getTemplate(stack)
          if (template != ItemTemplate.EmPlate) {
            import scala.collection.JavaConverters._
            blocksList(stack, basic).foreach(_.addAll(template.items.asJava))
            includeSetter(stack, basic).ap(template.include.some)
            playerIn.sendStatusMessage(new TextComponentTranslation(TranslationKeys.TOF_ADDED), false)
          }
        }
        EnumActionResult.SUCCESS
      case _ =>
        if (!context.isPlacerSneaking) {
          if (!worldIn.isRemote) {
            val playerPos = playerIn.getPosition
            NetworkHooks.openGui(playerIn.asInstanceOf[EntityPlayerMP], ItemTemplate.InteractionObject, playerPos)
          }
          EnumActionResult.SUCCESS
        } else {
          super.onItemUseFirst(stack, context)
        }
    }
  }
}

object ItemTemplate {
  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.template

  object InteractionObject extends IInteractionObject {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer): Container = new ContainerListTemplate(playerIn)

    override def getGuiID: String = GUI_ID

    override def getName: ITextComponent = new TextComponentString(QuarryPlus.Names.template)

    override def hasCustomName: Boolean = false

    override def getCustomName: ITextComponent = getName
  }

  def getEditorStack: ItemStack = {
    val stack = new ItemStack(Holder.itemTemplate)
    val compound = new NBTTagCompound
    compound.putInt("HideFlags", 1)
    stack.setTag(compound)
    stack
  }

  final val NBT_Template = "template"
  final val NBT_Template_Items = "items"
  final val NBT_Include = "include"

  final val EmPlate = Template(Nil, include = true)

  case class Template(items: List[BlockData], include: Boolean) {
    def writeToNBT(nbt: NBTTagCompound) = {
      val list = items.map(_.toNBT).foldLeft(new NBTTagList) { (l, tag) => l.add(tag); l }
      nbt.put(NBT_Template_Items, list)
      nbt.putBoolean(NBT_Include, include)
      nbt
    }

    def add(data: BlockData): Template = Template(data :: items, include)

    def remove(data: BlockData): Template = Template(items.filterNot(_ == data), include)

    def toggle: Template = Template(items, !include)
  }

  def getTemplate(stack: ItemStack): Template = {
    if (stack.getItem != Holder.itemTemplate) {
      EmPlate
    } else {
      val compound = Option(stack.getChildTag(NBT_Template))
      read(compound)
    }
  }

  def read(compound: Option[NBTTagCompound]): Template = {
    val opt = for (list <- compound.filter(_.contains(NBT_Template_Items)).map(_.getList(NBT_Template_Items, NBT.TAG_COMPOUND));
                   include <- compound.filter(_.contains(NBT_Include)).map(_.getBoolean(NBT_Include)).orElse(Some(true))) yield {
      val data = list.asScala.map(_.asInstanceOf[NBTTagCompound]).map(BlockData.read).toList
      Template(data, include)
    }
    opt.getOrElse(EmPlate)
  }

  def setTemplate(stack: ItemStack, template: Template): Unit = {
    if (stack.getItem == Holder.itemTemplate) {
      val compound = stack.getOrCreateChildTag(NBT_Template)
      template.writeToNBT(compound)
    }
  }

  val enchantmentName = Kleisli((stack: ItemStack) => List(silktouchName, fortuneName).flatMap(_.apply(stack)))

  private[this] val silkList = onlySilktouch.first[TileBasic].mapF(b => if (b.value._1) b.value._2.silktouchList.some else None)
  private[this] val fList = onlyFortune.first[TileBasic].mapF(b => if (b.value._1) b.value._2.fortuneList.some else None)
  private[this] val silkIncSet = onlySilktouch.first[TileBasic].mapF(b => if (b.value._1) ((bool: Boolean) => b.value._2.silktouchInclude = bool).some else None)
  private[this] val fIncSet = onlyFortune.first[TileBasic].mapF(b => if (b.value._1) ((bool: Boolean) => b.value._2.fortuneInclude = bool).some else None)
  val blocksList = Kleisli((t: (ItemStack, TileBasic)) => {
    val (stack: ItemStack, basic: TileBasic) = t
    silkList(stack, basic) orElse fList(stack, basic)
  })
  val includeSetter = Kleisli((t: (ItemStack, TileBasic)) => {
    val (stack: ItemStack, basic: TileBasic) = t
    silkIncSet(stack, basic) orElse fIncSet(stack, basic)
  })
}
