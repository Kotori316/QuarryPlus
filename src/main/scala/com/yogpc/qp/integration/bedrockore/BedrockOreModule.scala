package com.yogpc.qp.integration.bedrockore

import com.yogpc.qp.{Config, PowerManager, QuarryPlus}
import com.yogpc.qp.tile.{APowerTile, HasStorage, IModule, TileAdvQuarry, TileBasic, TileQuarry2}
import li.cil.bedrockores.common.tileentity.TileEntityBedrockOre
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

import scala.collection.JavaConverters._

class BedrockOreModule(machine: APowerTile, mode: () => Int, u: () => Int) extends IModule {
  override def id: String = BedrockOreModule.id

  override def calledWhen: Set[IModule.ModuleType] = Set(IModule.TypeBeforeBreak, IModule.TypeAfterBreak)

  override protected def action(when: IModule.CalledWhen): IModule.Result = {
    when match {
      case IModule.BeforeBreak(_, world, pos) =>
        val state = world.getBlockState(pos)
        if (BlockHolder.isBedrockOre(state.getBlock)) {
          val tile = world.getTileEntity(pos)
          tile match {
            case oreTile: TileEntityBedrockOre =>
              val ore = oreTile.getOreBlockState
              machine match {
                case s: HasStorage =>
                  val storage = s.getStorage
                  var first = true // flag. Machine has consumed energy once for the block.
                  while (oreTile.getAmount > 0) {
                    if (first || PowerManager.useEnergyBreak(this.machine, ore.getBlockHardness(world, pos), mode.apply(), u(), false)) {
                      first = false
                      if (mode.apply() == -1) {
                        oreTile.extract().asScala.foreach(storage.insertItem)
                      } else {
                        oreTile.extract()
                        val list = NonNullList.create[ItemStack]()
                        TileBasic.getDrops(world, pos, ore, ore.getBlock, mode.apply(), list)
                        list.asScala.foreach(storage.insertItem)
                      }
                    } else {
                      return IModule.NotFinished
                    }
                  }
                  // The block was replaced to bedrock.
                  if (machine.isInstanceOf[TileAdvQuarry] && Config.content.removeBedrock) {
                    IModule.NotFinished // Break again to remove bedrock.
                  } else {
                    IModule.Done
                  }
                case _ => IModule.NoAction // Skip
              }
            case _ => IModule.NoAction
          }
        } else {
          IModule.NoAction
        }
      case IModule.AfterBreak(_, _, before, _) =>
        if (BlockHolder.isBedrockOre(before.getBlock)) {
          IModule.Done // Implies that "replaced to bedrock".
        } else {
          IModule.NoAction
        }
      case _ => IModule.NoAction
    }
  }

  override def toString = "BedrockOreModule"
}

object BedrockOreModule {
  final val id = QuarryPlus.modID + ":" + "module_bedrock_ore"
  final val bedrock_mod_id = BlockHolder.MOD_ID_BEDROCK_ORES

  def from(tile: APowerTile): BedrockOreModule = {
    tile match {
      case q2: TileQuarry2 => new BedrockOreModule(q2, () => TileQuarry2.enchantmentMode(q2.enchantments), () => q2.enchantments.unbreaking)
      case advQuarry: TileAdvQuarry => new BedrockOreModule(advQuarry, () => advQuarry.ench.mode, () => advQuarry.ench.unbreaking)
      case _ => new BedrockOreModule(tile, () => 0, () => 0)
    }
  }
}
