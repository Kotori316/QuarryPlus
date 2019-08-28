package com.yogpc.qp.utils

import com.google.gson.JsonObject
import com.yogpc.qp.Config
import net.minecraft.util.JSONUtils
import net.minecraftforge.common.crafting.IConditionSerializer

class EnableCondition extends IConditionSerializer {
  final val NAME = "quarryplus:machine_enabled"

  override def parse(json: JsonObject) = {
    val s = JSONUtils.getString(json, "value")
    () => !Config.common.disabled.get(Symbol(s)).exists(_.get().booleanValue())
  }
}