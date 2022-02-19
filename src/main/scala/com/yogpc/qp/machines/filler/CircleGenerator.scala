package com.yogpc.qp.machines.filler

import com.yogpc.qp.machines.TargetIterator.XZPair

import scala.jdk.javaapi.CollectionConverters

//noinspection DuplicatedCode
object CircleGenerator {
  def makeCircle(center: XZPair, diameter: Int): Seq[XZPair] = {
    val distanceGetter: XZPair => Double =
      if (diameter % 2 == 0) xz => Math.pow(xz.x() - center.x() + 0.5d, 2) + Math.pow(xz.z() - center.z() + 0.5d, 2)
      else xz => Math.pow(xz.x() - center.x(), 2) + Math.pow(xz.z() - center.z(), 2)

    val radius = diameter.toDouble / 2
    val r1 = Math.pow(radius - 1, 2)
    val r2 = Math.pow(radius, 2)
    val distancePair = for {
      theta <- angle(radius)
      xz <- adjacent(center, radius, theta)
      distance = distanceGetter(xz)
      if r1 < distance && distance <= r2
    } yield (xz, distance)
    val maxDistance = distancePair.map(_._2).max
    distancePair.collect { case (pair, d) if d != maxDistance => pair }
      .distinct
      .sortBy(xz => Math.atan2(xz.z() - center.z(), xz.x() - center.x()))
  }

  private def angle(radius: Double): Seq[Double] = {
    val delta = Math.atan2(1, radius)
    Range.BigDecimal(0d, 2 * Math.PI, delta).map(_.toDouble)
  }

  private def adjacent(center: XZPair, radius: Double, theta: Double): Seq[XZPair] = {
    val x = (radius * Math.cos(theta)).toInt
    val z = (radius * Math.sin(theta)).toInt
    for {
      xDelta <- Range.inclusive(-1, 1)
      zDelta <- Range.inclusive(-1, 1)
    } yield new XZPair(center.x() + x + xDelta, center.z() + z + zDelta)
  }

  def testAdjacent(center: XZPair, radius: Double, theta: Double): java.util.Set[XZPair] =
    CollectionConverters.asJava(adjacent(center, radius, theta).toSet)

  def testCircle(center: XZPair, diameter: Int): java.util.List[XZPair] =
    CollectionConverters.asJava(makeCircle(center, diameter))
}
