package pl.pawelb.pizza

import akka.actor.Actor
import akka.event.LoggingReceive
import pl.pawelb.PizzaRequest

//messages
sealed trait CheeseMessage
case class CheeseRequest(forPizzaRequest: PizzaRequest) extends CheeseMessage
case class CheeseResponse(forPizzaRequest: PizzaRequest) extends CheeseMessage
case class NoCheeseLeft(forPizzaRequest: PizzaRequest, secondsToWait: Int) extends CheeseMessage
case class AddCheese(newCheese: Int) extends CheeseMessage

/**
 * Mutable state encapsulated in an actor
 */
class CheeseRepository extends Actor with akka.actor.ActorLogging {
  var cheeseLeft: Int = 0
  var waitTime: Int = 1

  def addCheese(amountOfCheese: Int) = {
    cheeseLeft += amountOfCheese
    log.info("\t\tWow, we got some new cheese: {}, left: {}", amountOfCheese, cheeseLeft)
  }

  def getCheese(amountOfCheese: Int) = {
    cheeseLeft -= amountOfCheese
  }

  def receive = LoggingReceive({
    case crq: CheeseRequest => {
      val cheeseBeforeTake = cheeseLeft
      if (cheeseLeft >= crq.forPizzaRequest.cheeseNeeded) {
        waitTime = 1
        //blocking
        //log.info("\t\tSleeping...")
        //Thread.sleep(1000)
        getCheese(crq.forPizzaRequest.cheeseNeeded)
        assert(cheeseBeforeTake == crq.forPizzaRequest.cheeseNeeded + cheeseLeft, "Wrong cheese amount!")
        log.info("\t\tFrom {} cheese took {}, left: {}", cheeseBeforeTake, crq.forPizzaRequest.cheeseNeeded, cheeseLeft)
        sender ! CheeseResponse(crq.forPizzaRequest)
      } else {
        waitTime += 1
        log.info("\t\tWe don't have enough cheese, wait {} seconds for some to appear...", waitTime)
        sender ! NoCheeseLeft(crq.forPizzaRequest, waitTime)
      }
    }
    case ca: AddCheese => {
      addCheese(ca.newCheese)
    }
  })

}

