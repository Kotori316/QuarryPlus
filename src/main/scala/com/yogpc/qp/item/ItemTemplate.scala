package com.yogpc.qp.item

import java.util

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
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand, NonNullList}
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
    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) > 0)
      tooltip.add(I18n.format(Enchantments.FORTUNE.getName))
    else if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
      tooltip.add(I18n.format(Enchantments.SILK_TOUCH.getName))
  }

  override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    if (this.isInCreativeTab(tab)) {
      items.add(getEditorStack)
    }
  }

  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand) = {
    if (!playerIn.isSneaking && handIn == EnumHand.MAIN_HAND) {
      val stack = playerIn.getHeldItem(handIn)
      val pos = playerIn.getPosition
      playerIn.openGui(QuarryPlus.INSTANCE, QuarryPlusI.guiIdListTemplate, worldIn, pos.getX, pos.getY, pos.getZ)
      ActionResult.newResult(EnumActionResult.SUCCESS, stack)
    } else {
      super.onItemRightClick(worldIn, playerIn, handIn)
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

  final val EmPlate = Template(Nil)

  case class Template(items: List[BlockData]) extends INBTWritable {
    override def writeToNBT(nbt: NBTTagCompound) = {
      val list = items.map(_.toNBT).foldLeft(new NBTTagList) { (l, tag) => l.appendTag(tag); l }
      nbt.setTag(NBT_Template_Items, list)
      nbt
    }

    def add(data: BlockData): Template = Template(data :: items)

    def remove(data: BlockData): Template = Template(items.filterNot(_ == data))
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
    list.map(_.tagIterator.map(BlockData.readFromNBT).toList).map(Template).getOrElse(EmPlate)
  }

  def setTemplate(stack: ItemStack, template: Template): Unit = {
    if (stack.getItem == QuarryPlusI.itemTemplate) {
      val compound = stack.getOrCreateSubCompound(NBT_Template)
      template.writeToNBT(compound)
    }
  }
}
