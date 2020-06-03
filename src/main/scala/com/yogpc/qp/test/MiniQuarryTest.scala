package com.yogpc.qp.test

import com.yogpc.qp.machines.base.Area
import com.yogpc.qp.machines.mini_quarry.MiniQuarryTile
import net.minecraft.util.Direction
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import scala.jdk.javaapi.CollectionConverters

class MiniQuarryTest {
  private val area = Area(0, 0, 0, 4, 5, 6, Some(0))

  @Test
  def rangeCheck(): Unit = {
    val up = Range(0, 7)
    val skipped = up.drop(1)

    val tests: Seq[Executable] = for ((l, u) <- up.dropRight(1) zip skipped) yield {
      () => assertTrue(l < u, f"$l is lower than $u.")
    }
    assertAll(CollectionConverters.asJava(tests))
  }

  @Test
  def efficiencyCheck(): Unit = {
    val up = Range(0, 7).map(MiniQuarryTile.interval)
    val tests: Seq[Executable] = for ((l, u) <- up.dropRight(1) zip up.drop(1)) yield {
      () => assertTrue(l > u, f"$u is lower than $l.")
    }
    assertAll(CollectionConverters.asJava(tests))
  }

  @Test
  def makeAreaNorth(): Unit = {
    val targets = MiniQuarryTile.makeTargetsXZ(area, Direction.NORTH).toList
    assertAll(
      () => assertEquals((4, 0), targets.head),
      () => assertEquals((4, 1), targets.tail.head),
      () => assertEquals((0, 6), targets.last),
    )
  }

  @Test
  def makeAreaSouth(): Unit = {
    val targets = MiniQuarryTile.makeTargetsXZ(area, Direction.SOUTH).toList
    assertAll(
      () => assertEquals((0, 6), targets.head),
      () => assertEquals((0, 5), targets.tail.head),
      () => assertEquals((4, 0), targets.last),
    )
  }

  @Test
  def makeAreaEastWest(): Unit = {
    val area = Area(0, 0, 0, 4, 5, 6, Some(0))
    val targetsWest = MiniQuarryTile.makeTargetsXZ(area, Direction.WEST).toList

    val targetsEast = MiniQuarryTile.makeTargetsXZ(area, Direction.EAST).toList

    assertAll(
      () => assertEquals((0, 0), targetsWest.head),
      () => assertEquals((1, 0), targetsWest.tail.head),
      () => assertEquals((4, 6), targetsWest.last),
      () => assertTrue(true), // Dummy to divide test
      () => assertEquals((4, 6), targetsEast.head),
      () => assertEquals((3, 6), targetsEast.tail.head),
      () => assertEquals((0, 0), targetsEast.last),
      // ----------
      () => assertTrue(targetsWest.reverse == targetsEast, "West.reverted == East")
    )
  }
}
