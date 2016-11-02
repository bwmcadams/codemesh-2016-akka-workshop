package com.lightbend.akka.coffeehouse

import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated }
import akka.routing.FromConfig

object CoffeeHouse {

  case class CreateGuest(favoriteCoffee: Coffee, caffeineLimit: Int)
  case class ApproveCoffee(coffee: Coffee, guest: ActorRef)
  case object GetStatus
  case class Status(guestCount: Int)

  def props(caffeineLimit: Int): Props =
    Props(new CoffeeHouse(caffeineLimit))
}

class CoffeeHouse(caffeineLimit: Int) extends Actor with ActorLogging with SettingsActor {

  import CoffeeHouse._
  import settings._

  override val supervisorStrategy: SupervisorStrategy = {
    val decider: SupervisorStrategy.Decider = {
      case Guest.CaffeineException =>
        SupervisorStrategy.Stop
      case Waiter.FrustratedException(coffee, guest) =>
        barista.tell(Barista.PrepareCoffee(coffee, guest), sender())
        SupervisorStrategy.Restart
    }
    OneForOneStrategy()(decider orElse super.supervisorStrategy.decider)
  }

  private val barista = createBarista()
  private val waiter = createWaiter()

  private var guestBook = Map.empty[ActorRef, Int] withDefaultValue 0

  log.debug("CoffeeHouse Open")

  override def receive: Receive = {
    case CreateGuest(favoriteCoffee, caffeineLimit) =>
      val guest: ActorRef = createGuest(favoriteCoffee, caffeineLimit)
      guestBook += guest -> (guestBook(guest) + 0)
      log.info(s"Guest $guest added to guest book.")
      context.watch(guest)
    case ApproveCoffee(coffee, guest) if guestBook(guest) < caffeineLimit =>
      guestBook += guest -> (guestBook(guest) + 1)
      log.info(s"Guest $guest caffeine count incremented.")
      barista forward Barista.PrepareCoffee(coffee, guest)
    case ApproveCoffee(coffee, guest) =>
      log.info(s"Sorry, $guest, but you have reached your limit.")
      context.stop(guest)
    case Terminated(guest) =>
      log.info(s"Thanks, $guest, for being our guest!")
      guestBook -= guest
      log.info(s"Guest $guest caffeine count decremented.")
    case GetStatus =>
      sender() ! Status(context.children.size - 2)
  }

  protected def createBarista(): ActorRef =
    context.actorOf(FromConfig.props(Barista.props(baristaPrepareCoffeeDuration, baristaAccuracy)), "barista")

  protected def createWaiter(): ActorRef =
    context.actorOf(Waiter.props(self, barista, waiterMaxComplaintCount), "waiter")

  protected def createGuest(favoriteCoffee: Coffee, caffeineLimit: Int): ActorRef =
    context.actorOf(Guest.props(waiter, favoriteCoffee, guestFinishCoffeeDuration, caffeineLimit))
}
