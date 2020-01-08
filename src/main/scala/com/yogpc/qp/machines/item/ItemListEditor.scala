package com.yogpc.qp.machines.item

import java.util

import cats._
import cats.data._
import cats.implicits._
import com.yogpc.qp.machines.base.{IEnchantableItem, IEnchantableTile}
import com.yogpc.qp.machines.quarry.TileBasic
import com.yogpc.qp.machines.workbench.BlockData
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper, Enchantments}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory, ServerPlayerEntity}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item._
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{ActionResultType, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fml.network.NetworkHooks
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._
import scala.util.Try

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

    val s = ItemListEditor.onlySilktouch(stack)
    val f = ItemListEditor.onlyFortune(stack)
    val bd = ItemListEditor.getBlockData(stack)
    if (context.getPlayer.isCrouching && bd.contains(new BlockData(worldIn.getBlockState(pos)))) {
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
                NetworkHooks.openGui(context.getPlayer.asInstanceOf[ServerPlayerEntity], new ItemListEditor.InteractionObject(f.value, tb.getName, pos), (b: PacketBuffer) => {
                  b.writeBlockPos(pos)
                  b.writeResourceLocation(f.map(bool => if (bool) IEnchantableTile.FortuneID else IEnchantableTile.SilktouchID).value)
                })
          }
        case _ =>
          if (!worldIn.isAirBlock(pos)) {
            if (!stack.hasTag) stack.setTag(new CompoundNBT)
            val tag = stack.getTag
            val name = ForgeRegistries.BLOCKS.getKey(worldIn.getBlockState(pos).getBlock).toString
            tag.putString(ItemListEditor.NAME_key, name)
          }
      }
    }
    ActionResultType.SUCCESS
  }

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (this.isInGroup(group)) {
      items.add(ItemListEditor.getEditorStack)
      if (Try(Config.common.debug).getOrElse(false)) {
        val stack = new ItemStack(Items.DIAMOND_PICKAXE)
        stack.addEnchantment(Enchantments.EFFICIENCY, 5)
        stack.addEnchantment(Enchantments.UNBREAKING, 3)
        locally {
          val stack1 = stack.copy
          stack1.addEnchantment(Enchantments.FORTUNE, 3)
          items.add(stack1)
        }
        locally {
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

  class InteractionObject(f: Boolean, text: ITextComponent, pos: BlockPos) extends INamedContainerProvider {
    val enchantmentName = f.pure[Id].map(bool => if (bool) IEnchantableTile.FortuneID else IEnchantableTile.SilktouchID)

    override def getDisplayName = text

    override def createMenu(id: Int, i: PlayerInventory, playerIn: PlayerEntity) = new ContainerEnchList(id, playerIn, pos, enchantmentName)

  }

  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.listeditor
  final val NAME_key = "name"

  def getEditorStack: ItemStack = {
    val stack = new ItemStack(Holder.itemListEditor, 1)
    val compound = new CompoundNBT
    compound.putInt("HideFlags", 1)
    stack.setTag(compound)
    stack
  }

  def hasEnchantment(enchantment: Eval[Enchantment]) = Kleisli((ench: List[Enchantment]) => enchantment.map(ench.contains))

  def enchantmentName(enchantment: Eval[Enchantment]) =
    getEnchantments andThen hasEnchantment(enchantment) mapF (b => if (b.value) new TranslationTextComponent(enchantment.value.getName).some else None)

  def onlySpecificEnchantment(enchantment: Eval[Enchantment]) = {
    implicit val bool: Semigroup[Boolean] = (x: Boolean, y: Boolean) => x | y
    val hasTheEnchantment = hasEnchantment(enchantment)
    val others = enchantment.map(e => ForgeRegistries.ENCHANTMENTS.asScala.filterNot(_ == e).map(Eval.now).map(hasEnchantment).toList)
    getEnchantments andThen {
      for (e <- hasTheEnchantment;
           o <- Semigroup[Kleisli[Eval, List[Enchantment], Boolean]].combineAllOption(others.value).get) yield e & !o
    }
  }

  private[this] val getTag = Kleisli((stack: ItemStack) => Option(stack.getTag))
  private[this] val getName = Kleisli((tag: CompoundNBT) => if (tag.contains(NAME_key, NBT.TAG_STRING)) tag.getString(NAME_key).some else None)
  private[this] val getEnchantments = Kleisli((stack: ItemStack) => Eval.now(EnchantmentHelper.getEnchantments(stack).asScala.toList.collect { case (e, level) if level > 0 => e }))

  private[this] val fortuneEval = Eval.later(Enchantments.FORTUNE)
  private[this] val silktouchEval = Eval.later(Enchantments.SILK_TOUCH)
  private[this] val hasFortune = hasEnchantment(fortuneEval)
  private[this] val hasSilktouch = hasEnchantment(silktouchEval)
  private[this] val getNameAsText = getTag andThen getName map (new StringTextComponent(_))

  val getBlockData = getTag andThen getName map (new BlockData(_))
  val isFortune = getEnchantments andThen hasFortune
  val isSilktouch = getEnchantments andThen hasSilktouch
  val fortuneName = enchantmentName(fortuneEval)
  val silktouchName = enchantmentName(silktouchEval)
  val information: Kleisli[List, ItemStack, ITextComponent] = Kleisli(stack => List(getNameAsText, fortuneName, silktouchName).flatMap(_ (stack)))

  val onlySilktouch = onlySpecificEnchantment(silktouchEval)
  val onlyFortune = onlySpecificEnchantment(fortuneEval)

}