package com.yogpc.qp.machines.exppump

import java.util.function.{IntConsumer, IntSupplier, LongPredicate}

import cats.Eval
import cats.data.Kleisli
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.base.{APowerTile, EnergyUsage, IEnchantableTile, IModule}
import net.minecraft.entity.item.EntityXPOrb

final class ExpPumpModule(useEnergy: Long => Boolean, unbreaking: Eval[Int], consumer: Option[IntConsumer]) extends IModule {
  def this(powerTile: APowerTile, consumer: Option[IntConsumer] = None) = {
    this(e => e == powerTile.useEnergy(e, e, true, EnergyUsage.PUMP_EXP),
      Eval.later(Option(powerTile)
        .collect { case ench: IEnchantableTile => ench.getEnchantments.get(IEnchantableTile.UnbreakingID).intValue() }
        .getOrElse(0)), consumer)
  }

  override val calledWhen = Set(IModule.TypeCollectItem, IModule.TypeBeforeBreak)

  var xp: Int = _

  override def action(when: IModule.CalledWhen): Unit = {
    val xp = when match {
      case t: IModule.CollectingItem =>
        t.entities.collect { case orb: EntityXPOrb if orb.isAlive => QuarryPlus.proxy.removeEntity(orb); orb.xpValue }.sum
      case s: IModule.BeforeBreak => s.xp
      case _ => 0
    }
    addXp(xp)
  }

  private def addXp(amount: Int): Unit = {
    val energy = getEnergy(amount).value
    if (useEnergy(energy)) {
      this.xp += amount
      if(amount != 0)
      consumer.foreach(_.accept(this.xp))
    }
  }

  private val getEnergy = Kleisli((amount: Int) => unbreaking.map(u => 10 * amount * APowerTile.MicroJtoMJ / (1 + u)))

  override def toString = s"ExpPumpModule($xp)"

  override val id = ExpPumpModule.id
}

object ExpPumpModule {
  final val id = "quarryplus:module_exp"

  def apply(useEnergy: LongPredicate, unbreaking: IntSupplier): ExpPumpModule =
    new ExpPumpModule(l => useEnergy.test(l), Eval.always(unbreaking.getAsInt), None)

  def fromTile(powerTile: APowerTile, consumer: IntConsumer) = new ExpPumpModule(powerTile, Option(consumer))
}
