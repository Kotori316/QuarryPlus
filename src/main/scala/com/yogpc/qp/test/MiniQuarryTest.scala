package com.yogpc.qp.test

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import scala.jdk.javaapi.CollectionConverters

class MiniQuarryTest {
  @Test
  def efficiencyCheck(): Unit = {
    val up = Range(0, 7)
    val skipped = up.drop(1)

    val tests: Seq[Executable] = for ((l, u) <- up.dropRight(1) zip skipped) yield {
      () => assertTrue(l < u, f"$l is lower than $u.")
    }
    assertAll(CollectionConverters.asJava(tests))
  }
}
