package com.lightbend.akka

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

package object coffeehouse {

  type Traversable[+A] = scala.collection.immutable.Traversable[A]

  type Iterable[+A] = scala.collection.immutable.Iterable[A]

  type Seq[+A] = scala.collection.immutable.Seq[A]

  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  /**
   * Adjust this factor to make the [[com.typesafe.training.coffeehouse.busy]] method kind of accurate.
   * The value `800` works well for a 2014 MacBook Pro with a 2.3 GHz i7 processor and Java 8.
   * Use the [[com.typesafe.training.coffee.gauge]] method to determin a good value for your machine.
   */
  val busyScalingFactor: Int =
    800

  /**
   * Keeps the CPU busy for the given appoximate duration.
   */
  def busy(duration: FiniteDuration): Unit =
    pi(duration.toMillis * busyScalingFactor)

  /**
   * Determines a reasonable value for the [[com.typesafe.training.coffeehouse.busyScalingFactor]].
   */
  def gauge(): Int = {
    val duration = 2.seconds
    println(s"Please wait for $duration ...")
    val startTime = System.currentTimeMillis()
    busy(duration)
    val endTime = System.currentTimeMillis()
    val gaugedFactor = busyScalingFactor.toDouble / (endTime - startTime) * duration.toMillis
    gaugedFactor.toInt
  }

  private def pi(m: Long) = {
    def gregoryLeibnitz(n: Long) = 4.0 * (1 - (n % 2) * 2) / (n * 2 + 1)
    var n = 0
    var acc = BigDecimal(0.0)
    while (n < m) {
      acc += gregoryLeibnitz(n)
      n += 1
    }
    acc
  }
}
