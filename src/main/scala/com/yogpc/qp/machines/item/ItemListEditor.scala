package com.yogpc.qp.machines.item

import java.util

import com.yogpc.qp.machines.base.{IEnchantableItem, IEnchantableTile}
import com.yogpc.qp.machines.quarry.TileBasic
import com.yogpc.qp.machines.workbench.BlockData
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.init.{Enchantments, Items}
import net.minecraft.item.{Item, ItemGroup, ItemStack, ItemUseContext}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.{ITextComponent, TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumActionResult, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.registries.ForgeRegistries

import scala.collection.JavaConverters._

class ItemListEditor extends Item((new Item.Properties).group(Holder.tab)) with IEnchantableItem {
  setRegistryName(QuarryPlus.modID, QuarryPlus.Names.listeditor)

  /**
    * You should not think max enchantment level in this method
    *
    * @param is          target ItemStack. It is never null.
    * @param enchantment target enchantment
    * @return that ItemStack can move enchantment on EnchantMover
    */
  override def canMove(is: ItemStack, enchantment: Enchantment) = {
    val l = is.getEnchantmentTagList
    (l == null || l.isEmpty) && ((enchantment == Enchantments.SILK_TOUCH) || (enchantment == Enchantments.FORTUNE))
  }

  /**
    * Called to get which items to show in JEI.
    *
    * @return stack which can be enchanted.
    */
  override def stacks() = Array(ItemListEditor.getEditorStack)

  override def isValidInBookMover = false

  override def onItemUseFirst(stack: ItemStack, context: ItemUseContext) = {
    val worldIn = context.getWorld
    val pos = context.getPos

    val enchantSet = EnchantmentHelper.getEnchantments(stack).asScala.keySet.map(_.getRegistryName)
    val s = enchantSet.contains(IEnchantableTile.SilktouchID)
    val f = enchantSet.contains(IEnchantableTile.FortuneID)
    var stackTag = stack.getTag
    val state = worldIn.getBlockState(pos)
    var bd: BlockData = null
    if (stackTag != null && stackTag.hasKey(ItemListEditor.NAME_key)) {
      bd = new BlockData(stackTag.getString(ItemListEditor.NAME_key))
      if (context.getPlayer.isSneaking && bd == new BlockData(state)) {
        stackTag.removeTag(ItemListEditor.NAME_key)
      }
    } else if (!state.getBlock.isAir(state, worldIn, pos)) {
      if (stackTag == null) {
        stackTag = new NBTTagCompound
        stack.setTag(stackTag)
      }
      val key = ForgeRegistries.BLOCKS.getKey(state.getBlock)
      require(key != null, "The item must be registered.")
      val name = key.toString
      stackTag.setString(ItemListEditor.NAME_key, name)

    } else {
      worldIn.getTileEntity(pos) match {
        case tb: TileBasic if s != f =>
          if (stackTag != null && bd != null) {
            if (!worldIn.isRemote) {
              val data = if (f) tb.fortuneList else tb.silktouchList
              data.add(bd)
            }
            stackTag.removeTag(ItemListEditor.NAME_key)
          } else {
            //            player.openGui(QuarryPlus.INSTANCE, if (f) QuarryPlusI.guiIdFList else QuarryPlusI.guiIdSList, worldIn, pos.getX, pos.getY, pos.getZ)
          }
        case _ =>
      }

    }
    EnumActionResult.SUCCESS
  }

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (this.isInGroup(group)) {
      items.add(ItemListEditor.getEditorStack)
      if (Config.common.debug) {
        val stack = new ItemStack(Items.DIAMOND_PICKAXE)
        stack.addEnchantment(Enchantments.EFFICIENCY, 5)
        stack.addEnchantment(Enchantments.UNBREAKING, 3)

        {
          val stack1 = stack.copy
          stack1.addEnchantment(Enchantments.FORTUNE, 3)
          items.add(stack1)
        }
        {
          val stack1 = stack.copy
          stack1.addEnchantment(Enchantments.SILK_TOUCH, 1)
          items.add(stack1)
        }
      }
    }
  }

  @OnlyIn(Dist.CLIENT)
  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    val tag = stack.getTag
    if (tag != null) {
      if (tag.hasKey(ItemListEditor.NAME_key)) tooltip.add(new TextComponentString(tag.getString(ItemListEditor.NAME_key)))
      val enchantments = EnchantmentHelper.getEnchantments(stack).asScala.mapValues(Int.unbox)
      if (enchantments.getOrElse(Enchantments.FORTUNE, 0) > 0)
        tooltip.add(new TextComponentTranslation(Enchantments.FORTUNE.getName))
      else if (enchantments.getOrElse(Enchantments.SILK_TOUCH, 0) > 0)
        tooltip.add(new TextComponentTranslation(Enchantments.SILK_TOUCH.getName))
    }
  }
}

object ItemListEditor {

  final val NAME_key = "name"

  def getEditorStack: ItemStack = {
    val stack = new ItemStack(Holder.itemListEditor, 1)
    val compound = new NBTTagCompound
    compound.setInt("HideFlags", 1)
    stack.setTag(compound)
    stack
  }
}