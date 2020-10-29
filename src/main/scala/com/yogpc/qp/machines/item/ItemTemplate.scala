package com.yogpc.qp.machines.item

import java.util

import cats.Eval
import cats.data._
import cats.implicits._
import com.mojang.serialization.Dynamic
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base.{EnchantmentFilter, IEnchantableItem, QuarryBlackList}
import com.yogpc.qp.machines.item.ItemListEditor._
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{QuarryPlus, _}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.{Enchantment, Enchantments}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory, ServerPlayerEntity}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.{Item, ItemGroup, ItemStack, ItemUseContext}
import net.minecraft.nbt.{CompoundNBT, ListNBT, NBTDynamicOps}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, TranslationTextComponent}
import net.minecraft.util.{ActionResultType, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fml.network.NetworkHooks

import scala.jdk.CollectionConverters._

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
  override def canMove(is: ItemStack, enchantment: Enchantment): Boolean = {
    val l = is.getEnchantmentTagList
    (l == null || l.isEmpty) && ((enchantment eq Enchantments.SILK_TOUCH) || (enchantment eq Enchantments.FORTUNE))
  }

  /**
   * Called to get which items to show in JEI.
   *
   * @return stack which can be enchanted.
   */
  override def stacks(): Array[ItemStack] = Array(getEditorStack)

  override def isValidInBookMover = false

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    val enchantment = ItemTemplate.enchantmentName(stack)
    enchantment.foreach(tooltip.add)
  }

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (this.isInGroup(group)) {
      items.add(getEditorStack)
      items.add(getEditorStack.enchantmentAdded(Enchantments.FORTUNE, 1))
      items.add(getEditorStack.enchantmentAdded(Enchantments.SILK_TOUCH, 1))
    }
  }

  override def onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType = {
    val worldIn = context.getWorld
    val pos = context.getPos
    val playerIn = context.getPlayer
    EnchantmentFilter.Accessor(worldIn.getTileEntity(pos)) match {
      case Some(value) =>
        if (!worldIn.isRemote) {
          val template = ItemTemplate.getTemplate(stack)
          if (template != ItemTemplate.EmPlate) {
            blocksListSetter(stack, value).ap(template.entries.toSet.some)
            includeSetter(stack, value).ap(template.include.some).foreach(
              _ => playerIn.sendStatusMessage(new TranslationTextComponent(TranslationKeys.TOF_ADDED), false)
            )
          }
        }
        ActionResultType.SUCCESS
      case None =>
        if (!context.hasSecondaryUseForPlayer) {
          if (!worldIn.isRemote) {
            val playerPos = playerIn.getPosition
            NetworkHooks.openGui(playerIn.asInstanceOf[ServerPlayerEntity], ItemTemplate.InteractionObject, playerPos)
          }
          ActionResultType.SUCCESS
        } else {
          super.onItemUseFirst(stack, context)
        }
    }
  }
}

object ItemTemplate {
  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.template

  object InteractionObject extends INamedContainerProvider {

    override def getDisplayName = new TranslationTextComponent(QuarryPlus.Names.template)

    override def createMenu(id: Int, i: PlayerInventory, p: PlayerEntity) = new ContainerListTemplate(id, p, BlockPos.ZERO)
  }

  def getEditorStack: ItemStack = {
    val stack = new ItemStack(Holder.itemTemplate)
    val compound = new CompoundNBT
    compound.putInt("HideFlags", 1)
    stack.setTag(compound)
    stack
  }

  final val NBT_Template = "template"
  final val NBT_Template_Items = "items"
  final val NBT_Template_Entries = "entries"
  final val NBT_Include = "include"

  final val EmPlate = Template(Nil, include = true)

  case class Template(entries: List[QuarryBlackList.Entry], include: Boolean) {
    def writeToNBT(nbt: CompoundNBT): CompoundNBT = {
      val list = entries.map(_.toNBT).foldLeft(new ListNBT) { (l, tag) => l.add(tag); l }
      nbt.put(NBT_Template_Entries, list)
      nbt.putBoolean(NBT_Include, include)
      nbt
    }

    def add(data: QuarryBlackList.Entry): Template = Template(data :: entries, include)

    def remove(data: QuarryBlackList.Entry): Template = Template(entries.filterNot(_ == data), include)

    def toggle: Template = Template(entries, !include)
  }

  def getTemplate(stack: ItemStack): Template = {
    if (stack.getItem != Holder.itemTemplate) {
      EmPlate
    } else {
      val compound = Option(stack.getChildTag(NBT_Template))
      read(compound)
    }
  }

  def read(compound: Option[CompoundNBT]): Template = {
    val maybeTemplate = compound.filter(_.contains(NBT_Template_Entries)).map(_.getList(NBT_Template_Entries, NBT.TAG_COMPOUND))
    val mustBeBoolean = compound.filter(_.contains(NBT_Include)).map(_.getBoolean(NBT_Include)).orElse(Some(true))
    val opt = (maybeTemplate, mustBeBoolean).mapN {
      case (list, include) =>
        val data = list.asScala.map(n => QuarryBlackList.readEntry2(new Dynamic(NBTDynamicOps.INSTANCE, n), log = false)).toList
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

  val enchantmentName: Kleisli[List, ItemStack, ITextComponent] = Kleisli((stack: ItemStack) => List(silktouchName, fortuneName).flatMap(_.apply(stack)))

  private[this] val setter = ((t: EnchantmentFilter.Accessor, f: EnchantmentFilter) => t.enchantmentFilter = f).tupled

  def createSetter[A](enchantmentSelector: Kleisli[Eval, ItemStack, Boolean], createInstance: (A, EnchantmentFilter.Accessor) => EnchantmentFilter):
  Kleisli[Option, (ItemStack, EnchantmentFilter.Accessor), A => Unit] = {
    enchantmentSelector.first[EnchantmentFilter.Accessor]
      .mapF(e => Option.when(e.value._1)(e.value._2))
      .map(t => (a: A) => (t, createInstance(a, t)))
      .map(_ andThen setter)
  }

  private[this] val silkList = createSetter(onlySilktouch, (s: Set[QuarryBlackList.Entry], t) => t.enchantmentFilter.copy(silktouchList = t.enchantmentFilter.silktouchList union s))
  private[this] val fList = createSetter(onlyFortune, (s: Set[QuarryBlackList.Entry], t) => t.enchantmentFilter.copy(fortuneList = t.enchantmentFilter.fortuneList union s))
  private[this] val silkIncSet = createSetter(onlySilktouch, (bool: Boolean, t) => t.enchantmentFilter.copy(silktouchInclude = bool))
  private[this] val fIncSet = createSetter(onlyFortune, (bool: Boolean, t) => t.enchantmentFilter.copy(fortuneInclude = bool))

  private def orElseCompute[A](silktouch: Kleisli[Option, (ItemStack, EnchantmentFilter.Accessor), A], fortune: Kleisli[Option, (ItemStack, EnchantmentFilter.Accessor), A]):
  Kleisli[Option, (ItemStack, EnchantmentFilter.Accessor), A] = Kleisli { (t: (ItemStack, EnchantmentFilter.Accessor)) =>
    val (stack: ItemStack, basic: EnchantmentFilter.Accessor) = t
    (silktouch orElse fortune).run(stack, basic)
  }

  val blocksListSetter: Kleisli[Option, (ItemStack, EnchantmentFilter.Accessor), Set[QuarryBlackList.Entry] => Unit] = orElseCompute(silkList, fList)
  val includeSetter: Kleisli[Option, (ItemStack, EnchantmentFilter.Accessor), Boolean => Unit] = orElseCompute(silkIncSet, fIncSet)
}
