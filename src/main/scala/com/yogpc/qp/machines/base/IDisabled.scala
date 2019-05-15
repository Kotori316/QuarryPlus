package com.yogpc.qp.machines.base

import com.yogpc.qp.Config

trait IDisabled {

  def getSymbol: Symbol

  def enabled: Boolean = Config.common.disabled.get(getSymbol).exists(!_.get().booleanValue())

  def defaultDisableMachine = false
}
