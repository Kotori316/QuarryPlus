package com.yogpc.qp.machines.item

import java.util

import cats._
import cats.data._
import cats.implicits._
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.base.{IEnchantableItem, IEnchantableTile}
import com.yogpc.qp.machines.quarry.TileBasic
import com.yogpc.qp.machines.workbench.BlockData
import com.yogpc.qp.utils.Holder
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper, Enchantments}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory, ServerPlayerEntity}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item._
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{ActionResultType, NonNullList, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fml.network.NetworkHooks
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._

class ItemListEditor extends Item((new Item.Properties).group(Holder.tab)) with IEnchantableItem {
  setRegistryName(QuarryPlus.modID, QuarryPlus.Names.listeditor)

  /**
   * You should not think max enchantment level in this method
   *
   * @param is          target ItemStack. It is never null.
   * @param enchantment target enchantment
   * @return that ItemStack can move enchantment on EnchantMover
   */
  override def canMove(is: ItemStack, enchantment: Enchantment): Boolean = {
    val l = is.getEnchantmentTagList
    (l == null || l.isEmpty) && ((enchantment == Enchantments.SILK_TOUCH) || (enchantment == Enchantments.FORTUNE))
  }

  /**
   * Called to get which items to show in JEI.
   *
   * @return stack which can be enchanted.
   */
  override def stacks(): Array[ItemStack] = Array(ItemListEditor.getEditorStack)

  override def isValidInBookMover = false

  override def onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType = {
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
                val adder = if (f.value) tb.enchantmentFilter.addFortune _ else tb.enchantmentFilter.addSilktouch _
                tb.enchantmentFilter = adder(value.name)
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
    val enchantmentName: ResourceLocation = if (f) IEnchantableTile.FortuneID else IEnchantableTile.SilktouchID

    override def getDisplayName: ITextComponent = text

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

  def hasEnchantment(enchantment: Eval[Enchantment]): Kleisli[Eval, List[Enchantment], Boolean] = Kleisli((ench: List[Enchantment]) => enchantment.map(ench.contains))

  def enchantmentName(enchantment: Eval[Enchantment]): Kleisli[Option, ItemStack, ITextComponent] =
    getEnchantments andThen hasEnchantment(enchantment) mapF (b => Option.when(b.value)(new TranslationTextComponent(enchantment.value.getName)))

  def onlySpecificEnchantment(enchantment: Eval[Enchantment]): Kleisli[Eval, ItemStack, Boolean] = {
    implicit val bool: Monoid[Boolean] = new Monoid[Boolean] {
      override def empty: Boolean = false

      override def combine(x: Boolean, y: Boolean): Boolean = x | y
    }
    val hasTheEnchantment = hasEnchantment(enchantment)
    val others = enchantment.map(e => ForgeRegistries.ENCHANTMENTS.asScala.view.filterNot(_ == e).map(Eval.now).map(hasEnchantment).toList)
    getEnchantments andThen {
      for (e <- hasTheEnchantment;
           o <- Monoid[Kleisli[Eval, List[Enchantment], Boolean]].combineAll(others.value)) yield e & !o
    }
  }

  private[this] val getTag = Kleisli((stack: ItemStack) => Option(stack.getTag))
  private[this] val getName = Kleisli((tag: CompoundNBT) => Option.when(tag.contains(NAME_key, NBT.TAG_STRING))(tag.getString(NAME_key)))
  private[this] val getEnchantments = Kleisli((stack: ItemStack) => Eval.now(EnchantmentHelper.getEnchantments(stack).asScala.toList.collect { case (e, level) if level > 0 => e }))

  private[this] val fortuneEval = Eval.later(Enchantments.FORTUNE)
  private[this] val silktouchEval = Eval.later(Enchantments.SILK_TOUCH)
  private[this] val hasFortune = hasEnchantment(fortuneEval)
  private[this] val hasSilktouch = hasEnchantment(silktouchEval)
  private[this] val getNameAsText = getTag andThen getName map (new StringTextComponent(_))

  val getBlockData: Kleisli[Option, ItemStack, BlockData] = getTag andThen getName map (new BlockData(_))
  val isFortune: Kleisli[Eval, ItemStack, Boolean] = getEnchantments andThen hasFortune
  val isSilktouch: Kleisli[Eval, ItemStack, Boolean] = getEnchantments andThen hasSilktouch
  val fortuneName: Kleisli[Option, ItemStack, ITextComponent] = enchantmentName(fortuneEval)
  val silktouchName: Kleisli[Option, ItemStack, ITextComponent] = enchantmentName(silktouchEval)
  val information: Kleisli[List, ItemStack, ITextComponent] = Kleisli(stack => List(getNameAsText, fortuneName, silktouchName).flatMap(_ (stack)))

  val onlySilktouch: Kleisli[Eval, ItemStack, Boolean] = onlySpecificEnchantment(silktouchEval)
  val onlyFortune: Kleisli[Eval, ItemStack, Boolean] = onlySpecificEnchantment(fortuneEval)

}