package com.yogpc.qp.item

import java.util

import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.tile.{IEnchantableTile, TileBasic}
import com.yogpc.qp.utils.{BlockData, INBTWritable}
import com.yogpc.qp.{QuarryPlus, QuarryPlusI, _}
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT

class ItemTemplate extends Item with IEnchantableItem {

  import ItemTemplate._

  setMaxStackSize(1)
  setCreativeTab(QuarryPlusI.creativeTab)
  setUnlocalizedName(QuarryPlus.Names.template)
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
    (l == null || l.tagCount == 0) && ((enchantment eq Enchantments.SILK_TOUCH) || (enchantment eq Enchantments.FORTUNE))
  }

  /**
    * Called to get which items to show in JEI.
    *
    * @return stack which can be enchanted.
    */
  override def stacks() = Array(getEditorStack)

  override def isValidInBookMover = false

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag): Unit = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
      tooltip.add(I18n.format(Enchantments.SILK_TOUCH.getName))
    else if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) > 0)
      tooltip.add(I18n.format(Enchantments.FORTUNE.getName))
  }

  override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    if (this.isInCreativeTab(tab)) {
      items.add(getEditorStack)
    }
  }

  override def onItemUseFirst(playerIn: EntityPlayer, worldIn: World, pos: BlockPos, side: EnumFacing,
                              hitX: Float, hitY: Float, hitZ: Float, handIn: EnumHand): EnumActionResult = {
    val stack = playerIn.getHeldItem(handIn)
    worldIn.getTileEntity(pos) match {
      case basic: TileBasic =>
        if (!worldIn.isRemote) {
          val enchantSet = stack.getEnchantmentTagList.tagIterator.map(_.getShort("id").toInt).toSet
          val s = enchantSet.contains(IEnchantableTile.SilktouchID)
          val f = enchantSet.contains(IEnchantableTile.FortuneID)
          val template = ItemTemplate.getTemplate(stack)
          if (s != f && template != ItemTemplate.EmPlate) {
            import scala.collection.JavaConverters._
            if (f) {
              basic.fortuneInclude = template.include
              basic.fortuneList.addAll(template.items.asJava)
            } else {
              basic.silktouchInclude = template.include
              basic.silktouchList.addAll(template.items.asJava)
            }
            playerIn.sendStatusMessage(new TextComponentTranslation(TranslationKeys.TOF_ADDED), false)
          }
        }
        EnumActionResult.SUCCESS
      case _ =>
        if (!playerIn.isSneaking && handIn == EnumHand.MAIN_HAND) {
          val playerPos = playerIn.getPosition
          playerIn.openGui(QuarryPlus.INSTANCE, QuarryPlusI.guiIdListTemplate, worldIn, playerPos.getX, playerPos.getY, playerPos.getZ)
          EnumActionResult.SUCCESS
        } else {
          super.onItemUseFirst(playerIn, worldIn, pos, side, hitX, hitY, hitZ, handIn)
        }
    }
  }
}

object ItemTemplate {
  def getEditorStack: ItemStack = {
    val stack = new ItemStack(QuarryPlusI.itemTemplate)
    val compound = new NBTTagCompound
    compound.setInteger("HideFlags", 1)
    stack.setTagCompound(compound)
    stack
  }

  final val NBT_Template = "template"
  final val NBT_Template_Items = "items"
  final val NBT_Include = "include"

  final val EmPlate = Template(Nil, include = true)

  case class Template(items: List[BlockData], include: Boolean) extends INBTWritable {
    override def writeToNBT(nbt: NBTTagCompound) = {
      val list = items.map(_.toNBT).foldLeft(new NBTTagList) { (l, tag) => l.appendTag(tag); l }
      nbt.setTag(NBT_Template_Items, list)
      nbt.setBoolean(NBT_Include, include)
      nbt
    }

    def add(data: BlockData): Template = Template(data :: items, include)

    def remove(data: BlockData): Template = Template(items.filterNot(_ == data), include)

    def toggle: Template = Template(items, !include)
  }

  def getTemplate(stack: ItemStack): Template = {
    if (stack.getItem != QuarryPlusI.itemTemplate) {
      EmPlate
    } else {
      val compound = Option(stack.getSubCompound(NBT_Template))
      read(compound)
    }
  }

  def read(compound: Option[NBTTagCompound]): Template = {
    val list = compound.map(_.getTagList(NBT_Template_Items, NBT.TAG_COMPOUND))
    val include = compound.filter(_.hasKey(NBT_Include)).fold(true)(_.getBoolean(NBT_Include))
    list.map(t => t.tagIterator.map(BlockData.readFromNBT).toList -> include).fold(EmPlate)(Template.tupled)
  }

  def setTemplate(stack: ItemStack, template: Template): Unit = {
    if (stack.getItem == QuarryPlusI.itemTemplate) {
      val compound = stack.getOrCreateSubCompound(NBT_Template)
      template.writeToNBT(compound)
    }
  }
}
