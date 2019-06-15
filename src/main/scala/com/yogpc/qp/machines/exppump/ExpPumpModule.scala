package com.yogpc.qp.machines.exppump

import com.yogpc.qp.machines.base.{APowerTile, EnergyUsage, IEnchantableTile, IModule}
import net.minecraft.entity.item.EntityXPOrb

class ExpPumpModule(useEnergy: Long => Boolean) extends IModule {
  def this(powerTile: APowerTile) = {
    this(e => e == powerTile.useEnergy(e, e, true, EnergyUsage.PUMP_EXP))
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
    val energy = getEnergy(amount)
    if (useEnergy(energy)) {
      this.xp += amount
    }
  }

  private def getEnergy(amount: Int) = {
    val unbreaking = Option(this)
      .collect { case ench: IEnchantableTile => ench.getEnchantments.get(IEnchantableTile.UnbreakingID).intValue() }
      .getOrElse(0)
    10 * amount * APowerTile.MicroJtoMJ / (1 + unbreaking)
  }
}
