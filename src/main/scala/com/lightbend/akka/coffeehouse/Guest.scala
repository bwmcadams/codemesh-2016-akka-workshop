package com.lightbend.akka.coffeehouse

import akka.actor.{ Actor, ActorLogging, ActorRef, FSM, Props }
import scala.concurrent.duration.FiniteDuration

object Guest {

  case object CaffeineException extends IllegalStateException("Too much caffeine!")

  sealed trait State

  object State {
    case object Drinking extends State
    case object Waiting extends State
  }

  def props(waiter: ActorRef, favoriteCoffee: Coffee, finishCoffeeDuration: FiniteDuration, caffeineLimit: Int): Props =
    Props(new Guest(waiter, favoriteCoffee, finishCoffeeDuration, caffeineLimit))
}

class Guest(waiter: ActorRef, favoriteCoffee: Coffee, finishCoffeeDuration: FiniteDuration, caffeineLimit: Int)
  extends Actor with ActorLogging with FSM[Guest.State, Int] {

  import Guest._

  startWith(State.Waiting, 0)

  when(State.Waiting) {
    case Event(Waiter.CoffeeServed(`favoriteCoffee`), coffeeCount) =>
      goto(State.Drinking) using (coffeeCount + 1)
    case Event(Waiter.CoffeeServed(coffee), _) =>
      log.info("Expected a {}, but got a {}!", favoriteCoffee, coffee)
      waiter ! Waiter.Complaint(favoriteCoffee)
      stay()
  }

  when(State.Drinking, finishCoffeeDuration) {
    case Event(StateTimeout, coffeeCount) if coffeeCount > caffeineLimit =>
      throw CaffeineException
    case Event(StateTimeout, _) =>
      orderFavoriteCoffee()
      goto(State.Waiting)
  }

  onTransition {
    case State.Waiting -> State.Drinking =>
      log.info("Enjoying my {} yummy {}!", nextStateData, favoriteCoffee)
  }

  initialize()

  override def preStart(): Unit =
    orderFavoriteCoffee()

  override def postStop(): Unit =
    log.info("Goodbye!")

  private def orderFavoriteCoffee(): Unit =
    waiter ! Waiter.ServeCoffee(favoriteCoffee)
}
