package pl.pawelb

import akka.actor.ActorSystem
import akka.actor.Props
import pl.pawelb.pizza._

object PizzaDemoBasics extends App {
  val system = ActorSystem("pizza-system")
  val pizzaCounterActor = system.actorOf(Props[PizzaCounter])
}

