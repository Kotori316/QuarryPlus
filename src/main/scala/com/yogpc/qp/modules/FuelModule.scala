package com.yogpc.qp.modules

import com.yogpc.qp.tile.IModule

class FuelModule(mode: FuelModule.Mode) extends IModule {
  override def id = FuelModule.id

  override def calledWhen: Set[IModule.ModuleType] = Set(IModule.TypeTick)

  /**
    * @return false if work hasn't finished. true if work has done or did nothing.
    */
  override protected def action(when: IModule.CalledWhen): IModule.Result = when match {
    case IModule.Tick(tile) =>
      val rf = mode match {
        case FuelModule.Normal => FuelModule.RFInTick
        case FuelModule.Creative => Int.MaxValue
      }
      tile.receiveEnergy(rf, false)
      IModule.Done
    case _ => IModule.NoAction
  }
}

object FuelModule {
  final val id = "quarryplus:module_fuel"
  final val RFInTick = 10

  sealed abstract class Mode(val name: String)

  case object Normal extends Mode("normal")

  case object Creative extends Mode("creative")

}
