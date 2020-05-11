package com.yogpc.qp.machines.modules

import com.yogpc.qp.machines.base.IModule

class FuelModule(mode: FuelModule.Mode, count: Int) extends IModule {
  private[this] final val energy = mode match {
    case FuelModule.Normal => FuelModule.RFInTick * count
    case FuelModule.Creative => Int.MaxValue
  }

  override def id: String = FuelModule.id

  override def calledWhen = Set(IModule.TypeTick)

  /**
   * @return false if work hasn't finished. true if work has done or did nothing.
   */
  override protected def action(when: IModule.CalledWhen): IModule.Result = when match {
    case IModule.Tick(tile) =>
      val rf = energy
      tile.receiveEnergy(rf, false)
      IModule.Done
    case _ => IModule.NoAction
  }


  override def toString: String = {
    mode match {
      case FuelModule.Normal => s"FuelModule($energy RF/t)"
      case FuelModule.Creative => "FuelModule(Creative)"
    }
  }
}

object FuelModule {
  final val id = "quarryplus:module_fuel"
  final val RFInTick = 40

  sealed abstract class Mode(val name: String)

  case object Normal extends Mode("normal")

  case object Creative extends Mode("creative")

}
