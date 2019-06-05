package com.yogpc.qp.machines.item

import java.util

import cats._
import cats.data._
import cats.implicits._
import com.yogpc.qp.machines.base.IEnchantableItem
import com.yogpc.qp.machines.quarry.TileBasic
import com.yogpc.qp.machines.workbench.BlockData
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP, InventoryPlayer}
import net.minecraft.init.{Enchantments, Items}
import net.minecraft.item.{Item, ItemGroup, ItemStack, ItemUseContext}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.{ITextComponent, TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumActionResult, NonNullList}
import net.minecraft.world.{IInteractionObject, World}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fml.network.NetworkHooks
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

    val s = ItemListEditor.isSilktouch(stack)
    val f = ItemListEditor.isFortune(stack)
    val bd = ItemListEditor.getBlockData(stack)
    if (context.getPlayer.isSneaking && bd.contains(new BlockData(worldIn.getBlockState(pos)))) {
      stack.getTag.remove(ItemListEditor.NAME_key)
    } else {
      worldIn.getTileEntity(pos) match {
        case tb: TileBasic if s.value != f.value =>
          bd match {
            case Some(value) =>
              if (!worldIn.isRemote) {
                val data = if (f.value) tb.fortuneList else tb.silktouchList
                data.add(value)
              }
              stack.getTag.remove(ItemListEditor.NAME_key)
            case None =>
              if (!worldIn.isRemote)
                NetworkHooks.openGui(context.getPlayer.asInstanceOf[EntityPlayerMP], new ItemListEditor.InteractionObject(f.value, tb), pos)
          }
        case _ =>
          if (!worldIn.isAirBlock(pos)) {
            if (!stack.hasTag) stack.setTag(new NBTTagCompound)
            val tag = stack.getTag
            val name = ForgeRegistries.BLOCKS.getKey(worldIn.getBlockState(pos).getBlock).toString
            tag.putString(ItemListEditor.NAME_key, name)
          }
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
    val data = ItemListEditor.information(stack)
    data.foreach(tooltip.add)
  }
}

object ItemListEditor {

  class InteractionObject(fortune: Boolean, tileBasic: TileBasic) extends IInteractionObject {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = new ContainerEnchList(tileBasic, playerIn)

    override def getGuiID = if (fortune) GUI_ID_Fortune else GUI_ID_Silktouch

    override def getName = tileBasic.getName

    override def hasCustomName = tileBasic.hasCustomName

    override def getCustomName = tileBasic.getCustomName
  }

  final val GUI_ID_Fortune = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.listeditor + "_fortune"
  final val GUI_ID_Silktouch = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.listeditor + "_silktouch"
  final val NAME_key = "name"

  def getEditorStack: ItemStack = {
    val stack = new ItemStack(Holder.itemListEditor, 1)
    val compound = new NBTTagCompound
    compound.putInt("HideFlags", 1)
    stack.setTag(compound)
    stack
  }

  private[this] val getTag = Kleisli((stack: ItemStack) => Option(stack.getTag))
  private[this] val getName = Kleisli((tag: NBTTagCompound) => if (tag.contains(NAME_key, NBT.TAG_STRING)) tag.getString(NAME_key).some else None)
  private[this] val getEnchantments = Kleisli((stack: ItemStack) => Eval.now(EnchantmentHelper.getEnchantments(stack).asScala.collect { case (e, level) if level > 0 => e }.toList))
  private[this] val hasFortune = Kleisli((ench: List[Enchantment]) => Eval.later(ench.contains(Enchantments.FORTUNE)))
  private[this] val hasSilktouch = Kleisli((ench: List[Enchantment]) => Eval.later(ench.contains(Enchantments.SILK_TOUCH)))
  private[this] val getNameAsText = getTag andThen getName map (new TextComponentString(_))

  val getBlockData = getTag andThen getName map (new BlockData(_))
  val isFortune = getEnchantments andThen hasFortune
  val isSilktouch = getEnchantments andThen hasSilktouch
  val fortuneName = isFortune mapF (b => if (b.value) new TextComponentTranslation(Enchantments.FORTUNE.getName).some else None)
  val silktouchName = isSilktouch mapF (b => if (b.value) new TextComponentTranslation(Enchantments.SILK_TOUCH.getName).some else None)
  val information: Kleisli[List, ItemStack, ITextComponent] = Kleisli(stack => List(getNameAsText, fortuneName, silktouchName).flatMap(_ (stack)))
  implicit val bool: Semigroup[Boolean] = (x: Boolean, y: Boolean) => x | y
  val onlySilktouch = getEnchantments andThen {
    for (s <- hasSilktouch;
         f <- NonEmptyList.one(hasFortune).reduce
    ) yield s & !f
  }
  val onlyFortune = getEnchantments andThen {
    for (s <- NonEmptyList.one(hasSilktouch).reduce;
         f <- hasFortune
    ) yield !s & f
  }
}