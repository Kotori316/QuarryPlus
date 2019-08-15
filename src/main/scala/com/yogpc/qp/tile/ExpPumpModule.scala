package com.yogpc.qp.tile

import java.util.function.{IntConsumer, IntSupplier, LongPredicate}

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.tile.IModule.{Done, Result}
import net.minecraft.entity.item.EntityXPOrb

final class ExpPumpModule(useEnergy: Long => Boolean, unbreaking: () => Int, consumer: Option[IntConsumer]) extends IModule {
  def this(powerTile: APowerTile, consumer: Option[IntConsumer] = None) = {
    this(e => e == powerTile.useEnergy(e, e, true, EnergyUsage.PUMP_EXP),
      () => Option(powerTile)
        .collect { case ench: IEnchantableTile => ench.getEnchantments }
        .flatMap(m => Option(m.get(IEnchantableTile.UnbreakingID)))
        .map(_.intValue())
        .getOrElse(0), consumer)
  }

  override val calledWhen: Set[IModule.ModuleType] = Set(IModule.TypeCollectItem, IModule.TypeBeforeBreak)

  var xp: Int = _

  override def action(when: IModule.CalledWhen): Result = {
    val xp = when match {
      case t: IModule.CollectingItem =>
        t.entities.collect { case orb: EntityXPOrb if orb.isEntityAlive => QuarryPlus.proxy.removeEntity(orb); orb.xpValue }.sum
      case s: IModule.BeforeBreak => s.xp
      case _ => 0
    }
    addXp(xp)
    Done
  }

  private def addXp(amount: Int): Unit = {
    if (amount == 0) return
    val energy = getEnergy(amount)
    if (useEnergy(energy)) {
      this.xp += amount
      if (amount != 0) // Always true
        consumer.foreach(_.accept(this.xp))
    }
  }

  private val getEnergy = (amount: Int) => if (amount == 0) 0L else 10 * amount * APowerTile.MJToMicroMJ / (1 + unbreaking.apply())

  override def toString = s"ExpPumpModule($xp)"

  override val id = ExpPumpModule.id
}

object ExpPumpModule {
  final val id = "quarryplus:module_exp"

  def apply(useEnergy: LongPredicate, unbreaking: IntSupplier): ExpPumpModule =
    new ExpPumpModule(l => useEnergy.test(l), () => unbreaking.getAsInt, None)

  def fromTile(powerTile: APowerTile, consumer: IntConsumer) = new ExpPumpModule(powerTile, Option(consumer))
}
