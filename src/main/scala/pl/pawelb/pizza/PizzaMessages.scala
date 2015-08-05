package pl.pawelb.pizza;

import akka.actor.ActorRef

case class CloseCounter()

sealed trait PizzaMessage
case class PizzaBulkRequest(howMany: Int) extends PizzaMessage
case class PizzaRequest(cheeseNeeded: Int, whoWantsPizza: ActorRef) extends PizzaMessage
case class PizzaResponse(amountOfCheese: Int) extends PizzaMessage

sealed trait CheeseMessage
case class CheeseRequest(forPizzaRequest: PizzaRequest) extends CheeseMessage
case class CheeseResponse(forPizzaRequest: PizzaRequest) extends CheeseMessage
case class NoCheeseLeft(forPizzaRequest: PizzaRequest, secondsToWait: Int) extends CheeseMessage
case class AddCheese(newCheese: Int) extends CheeseMessage
