package com.lightbend.akka.coffeehouse

import akka.actor.{ Actor, ExtendedActorSystem, Extension, ExtensionKey }
import akka.util.Timeout
import scala.concurrent.duration.{ Duration, FiniteDuration, MILLISECONDS => Millis }

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {

  val caffeineLimit: Int =
    system.settings.config.getInt("coffee-house.caffeine-limit")

  val statusTimeout: Timeout =
    Duration(system.settings.config.getDuration("coffee-house.status-timeout", Millis), Millis)

  val baristaPrepareCoffeeDuration: FiniteDuration =
    Duration(system.settings.config.getDuration("coffee-house.barista.prepare-coffee-duration", Millis), Millis)

  val baristaAccuracy: Int =
    system.settings.config.getInt("coffee-house.barista.accuracy")

  val waiterMaxComplaintCount: Int =
    system.settings.config.getInt("coffee-house.waiter.max-complaint-count")

  val guestFinishCoffeeDuration: FiniteDuration =
    Duration(system.settings.config.getDuration("coffee-house.guest.finish-coffee-duration", Millis), Millis)
}

trait SettingsActor {
  this: Actor =>

  val settings: Settings =
    Settings(context.system)
}
