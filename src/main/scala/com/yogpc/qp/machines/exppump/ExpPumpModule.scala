package com.yogpc.qp.machines.exppump

import java.util.function.{IntConsumer, IntSupplier, LongPredicate}

import cats.Eval
import cats.data.Kleisli
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.PowerManager
import com.yogpc.qp.machines.base.IModule.{Done, NoAction, Result}
import com.yogpc.qp.machines.base.{APowerTile, EnergyUsage, IEnchantableTile, IModule}
import net.minecraft.entity.item.ExperienceOrbEntity

final class ExpPumpModule(useEnergy: Long => Boolean, unbreaking: Eval[Int], consumer: Option[IntConsumer]) extends IModule {
  def this(powerTile: APowerTile, consumer: Option[IntConsumer] = None) = {
    this(e => PowerManager.useEnergy(powerTile, e, EnergyUsage.PUMP_EXP),
      Eval.later(Option(powerTile)
        .collect { case ench: IEnchantableTile => ench.getEnchantments.getOrDefault(IEnchantableTile.UnbreakingID, 0).intValue() }
        .getOrElse(0)), consumer)
  }

  override val calledWhen = Set(IModule.TypeCollectItem, IModule.TypeAfterBreak)

  var xp: Int = _

  override def action(when: IModule.CalledWhen): Result = {
    val (xp, r) = when match {
      case t: IModule.CollectingItem =>
        t.entities.collect { case orb: ExperienceOrbEntity if orb.isAlive => QuarryPlus.proxy.removeEntity(orb); orb.xpValue }.sum -> Done
      case s: IModule.AfterBreak => s.xp -> NoAction
      case _ => 0 -> NoAction
    }
    addXp(xp)
    r
  }

  private def addXp(amount: Int): Unit = {
    if (amount == 0) return
    val energy = getEnergy(amount).value
    if (useEnergy(energy)) {
      this.xp += amount
      // Always true
      if (amount != 0)
        consumer.foreach(_.accept(this.xp))
    }
  }

  private val getEnergy = Kleisli((amount: Int) => if (amount == 0) ExpPumpModule.zeroL else unbreaking.map(u => 10 * amount * APowerTile.MJToMicroMJ / (1 + u)))

  override def toString = s"ExpPumpModule($xp)"

  override val id: String = ExpPumpModule.id
}

object ExpPumpModule {
  final val id = "quarryplus:module_exp"
  final val zeroL = Eval.now(0L)

  def apply(useEnergy: LongPredicate, unbreaking: IntSupplier): ExpPumpModule =
    new ExpPumpModule(l => useEnergy.test(l), Eval.always(unbreaking.getAsInt), None)

  def fromTile(powerTile: APowerTile, consumer: IntConsumer) = new ExpPumpModule(powerTile, Option(consumer))
}
