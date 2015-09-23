package pl.pawelb

import akka.actor.ActorSystem
import akka.actor.Props
import pl.pawelb.pizza._

/**
 * Router demo
 */
object PizzaDemoBasics extends App {
  val system = ActorSystem("pizza-system")
  val pizzaCounterActor = system.actorOf(Props[PizzaCounter])
}

