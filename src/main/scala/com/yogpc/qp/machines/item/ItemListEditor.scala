package com.yogpc.qp.machines.item

import java.util

import cats._
import cats.data._
import cats.implicits._
import com.mojang.serialization.{Dynamic => SerializeDynamic}
import com.yogpc.qp.machines.base.{EnchantmentFilter, IEnchantableItem, IEnchantableTile, QuarryBlackList}
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{QuarryPlus, _}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper, Enchantments}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory, ServerPlayerEntity}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item._
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
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
    if (context.getPlayer.isCrouching && bd.exists(_.test(worldIn.getBlockState(pos), worldIn, pos))) {
      stack.getTag.remove(ItemListEditor.NAME_key)
    } else {
      EnchantmentFilter.Accessor(worldIn.getTileEntity(pos)) match {
        case Some(value) if s.value != f.value =>
          bd match {
            case Some(data) =>
              if (!worldIn.isRemote) {
                val adder = if (f.value) value.enchantmentFilter.addFortuneEntry _ else value.enchantmentFilter.addSilktouchEntry _
                value.enchantmentFilter = adder(data)
              }
              stack.getTag.remove(ItemListEditor.NAME_key)
            case None =>
              if (!worldIn.isRemote)
                NetworkHooks.openGui(context.getPlayer.asInstanceOf[ServerPlayerEntity], new ItemListEditor.InteractionObject(f.value, value.getName, pos), (b: PacketBuffer) => {
                  b.writeBlockPos(pos)
                  b.writeResourceLocation(f.map(bool => if (bool) IEnchantableTile.FortuneID else IEnchantableTile.SilktouchID).value)
                })
          }
        case Some(_) =>
          if (!worldIn.isRemote) {
            context.getPlayer.sendStatusMessage(new TranslationTextComponent("quarryplus.chat.editor_enchantment"), false)
          }
        case None =>
          if (!worldIn.isAirBlock(pos)) {
            if (!stack.hasTag) stack.setTag(new CompoundNBT)
            val tag = stack.getTag
            val name = ForgeRegistries.BLOCKS.getKey(worldIn.getBlockState(pos).getBlock)
            tag.put(ItemListEditor.NAME_key, QuarryBlackList.Name(name).toNBT)
          }
      }
    }
    ActionResultType.SUCCESS
  }

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (this.isInGroup(group)) {
      items.add(ItemListEditor.getEditorStack)
      items.add(ItemListEditor.getEditorStack.enchantmentAdded(Enchantments.FORTUNE, 1))
      items.add(ItemListEditor.getEditorStack.enchantmentAdded(Enchantments.SILK_TOUCH, 1))
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

  def hasEnchantment(enchantment: Eval[Enchantment]): Kleisli[Eval, List[Enchantment], Boolean] = Kleisli(ench => enchantment.map(ench.contains))

  def enchantmentName(enchantment: Eval[Enchantment]): Kleisli[Option, ItemStack, ITextComponent] =
    getEnchantments andThen hasEnchantment(enchantment) mapF (b => Option.when[ITextComponent](b.value)(new TranslationTextComponent(enchantment.value.getName)))

  def onlySpecificEnchantment(enchantment: Eval[Enchantment]): Kleisli[Eval, ItemStack, Boolean] = {
    val hasTheEnchantment = hasEnchantment(enchantment)
    getEnchantments andThen {
      for (e <- hasTheEnchantment;
           o <- listSizeIs1) yield e && o
    }
  }

  private final val listSizeIs1 = Kleisli((l: List[Enchantment]) => Eval.now(l.lengthIs == 1))
  private final val getTag = Kleisli((stack: ItemStack) => Option(stack.getTag))
  private final val getEntry = Kleisli((tag: CompoundNBT) =>
    Option.when(tag.contains(NAME_key, NBT.TAG_COMPOUND))(QuarryBlackList.readEntry2(new SerializeDynamic(NBTDynamicOps.INSTANCE, tag.get(NAME_key)), log = false)))
  private final val getEnchantments = Kleisli((stack: ItemStack) => Eval.now(EnchantmentHelper.getEnchantments(stack).asScala.toList.collect { case (e, level) if level > 0 => e }))

  private final val fortuneEval = Eval.later(Enchantments.FORTUNE)
  private final val silktouchEval = Eval.later(Enchantments.SILK_TOUCH)
  private final val hasFortune = hasEnchantment(fortuneEval)
  private final val hasSilktouch = hasEnchantment(silktouchEval)
  private final val getNameAsText = getTag andThen getEntry map (e => new StringTextComponent(e.toString))

  final val getBlockData: Kleisli[Option, ItemStack, QuarryBlackList.Entry] = getTag andThen getEntry
  final val isFortune: Kleisli[Eval, ItemStack, Boolean] = getEnchantments andThen hasFortune
  final val isSilktouch: Kleisli[Eval, ItemStack, Boolean] = getEnchantments andThen hasSilktouch
  final val fortuneName: Kleisli[Option, ItemStack, ITextComponent] = enchantmentName(fortuneEval)
  final val silktouchName: Kleisli[Option, ItemStack, ITextComponent] = enchantmentName(silktouchEval)
  final val information: Kleisli[List, ItemStack, ITextComponent] = Kleisli(stack => List(getNameAsText, fortuneName, silktouchName).flatMap(_ (stack)))

  final val onlySilktouch: Kleisli[Eval, ItemStack, Boolean] = onlySpecificEnchantment(silktouchEval)
  final val onlyFortune: Kleisli[Eval, ItemStack, Boolean] = onlySpecificEnchantment(fortuneEval)

}