package com.yogpc.qp.machines.exppump

import cats.Eval
import cats.data.Kleisli
import com.yogpc.qp.machines.base.{APowerTile, EnergyUsage, IEnchantableTile, IModule}
import net.minecraft.entity.item.EntityXPOrb

final class ExpPumpModule(useEnergy: Long => Boolean, unbreaking: Eval[Int]) extends IModule {
  def this(powerTile: APowerTile) = {
    this(e => e == powerTile.useEnergy(e, e, true, EnergyUsage.PUMP_EXP),
      Eval.later(Option(powerTile)
        .collect { case ench: IEnchantableTile => ench.getEnchantments.get(IEnchantableTile.UnbreakingID).intValue() }
        .getOrElse(0)))
  }

  override type Accessor = ExpPumpModule

  override val calledWhen = Set(classOf[IModule.CollectingItem], classOf[IModule.OnBreak], classOf[IModule.DropItem])

  override val access: Option[ExpPumpModule] = Some(this)

  var xp: Int = _

  override def action(when: IModule.CalledWhen): Unit = {
    val xp = when match {
      case t: IModule.CollectingItem =>
        t.entities.collect { case orb: EntityXPOrb if orb.isAlive => orb.remove(); orb.xpValue }.sum
      case s: IModule.OnBreak => s.xp
      case u: IModule.DropItem => u.xp
      case _ => 0
    }
    addXp(xp)
  }

  private def addXp(amount: Int): Unit = {
    val energy = getEnergy(amount).value
    if (useEnergy(energy)) {
      this.xp += amount
    }
  }

  private val getEnergy = Kleisli((amount: Int) => unbreaking.map(u => 10 * amount * APowerTile.MicroJtoMJ / (1 + u)))

  override def toString = s"ExpPumpModule($xp)"
}

object ExpPumpModule {
  def apply(useEnergy: java.util.function.LongPredicate, unbreaking: java.util.function.IntSupplier): ExpPumpModule =
    new ExpPumpModule(l => useEnergy.test(l), Eval.always(unbreaking.getAsInt))

  def fromTile(powerTile: APowerTile) = new ExpPumpModule(powerTile)
}
