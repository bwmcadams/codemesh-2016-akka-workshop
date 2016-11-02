package com.lightbend.akka.coffeehouse

import akka.actor.{ Actor, ActorRef, FSM, Props, Stash }
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Barista {

  case class PrepareCoffee(coffee: Coffee, guest: ActorRef)
  case class CoffeePrepared(coffee: Coffee, guest: ActorRef)

  sealed trait State

  object State {
    case object Ready extends State
    case object Busy extends State
  }

  def props(prepareCoffeeDuration: FiniteDuration, accuracy: Int): Props =
    Props(new Barista(prepareCoffeeDuration, accuracy))
}

class Barista(prepareCoffeeDuration: FiniteDuration, accuracy: Int) extends Actor
with Stash with FSM[Barista.State, Option[ActorRef]] {

  import Barista._

  startWith(State.Ready, None)

  when(State.Ready) {
    case Event(PrepareCoffee(coffee, guest), _) =>
      setTimer("coffee-prepared", CoffeePrepared(pickCoffee(coffee), guest), prepareCoffeeDuration)
      goto(State.Busy) using Some(sender())
  }

  when(State.Busy) {
    case Event(coffeePrepared: CoffeePrepared, Some(waiter)) =>
      waiter ! coffeePrepared
      unstashAll()
      goto(State.Ready) using None
    case _ =>
      stash()
      stay()
  }

  initialize()

  private def pickCoffee(coffee: Coffee): Coffee =
    if (Random.nextInt(100) < accuracy)
      coffee
    else
      Coffee.anyOther(coffee)
}
