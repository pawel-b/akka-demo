package pl.pawelb

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.routing.FromConfig
import akka.actor.Props
import akka.pattern._
import scala.util.Random
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorRef
import scala.concurrent.ExecutionContext.Implicits.global
import ch.qos.logback.classic.Logger

sealed trait PizzaMessage
case class PizzaRequest() extends PizzaMessage
case class PizzaResponse() extends PizzaMessage

sealed trait CheeseMessage
case class CheeseRequest(amountOfCheese: Int) extends CheeseMessage
case class CheeseResponse(amountOfCheese: Int) extends CheeseMessage
case class NoCheeseLeft(secondsToWait: Int) extends CheeseMessage
case class AddCheese(newCheese: Int) extends CheeseMessage

object PizzaDemo extends App {
  val system = ActorSystem("pizza-system")
  val pizzaCounterActor = system.actorOf(Props[PizzaCounter])

}

class PizzaCounter extends Actor with akka.actor.ActorLogging {

  val cheeseVaultActor = context.system.actorOf(Props[CheeseRepository])
  val pizzaActor = context.system.actorOf(Props(new PizzaMaker(cheeseVaultActor)).withRouter(FromConfig()), name = "pizzaMaker")

  context.system.scheduler.schedule(5 seconds, 1 second, pizzaActor, new PizzaRequest)
  context.system.scheduler.schedule(1 second, 15 seconds, cheeseVaultActor, new AddCheese(10))

  context.system.scheduler.scheduleOnce(60 seconds) {
    context.system.shutdown()
  }

  def receive = {
    case crs: PizzaResponse => {
      log.info("Yay, got new pizza!")
    }
  }

}

class PizzaMaker(cheeseVault: ActorRef) extends Actor with akka.actor.ActorLogging {
  implicit val timeout = Timeout(5 seconds)
  val randoms = new Random

  var cheeseNeeded: Int = 0
  var whoWantsPizza: ActorRef = self

  def receive = {
    case prq: PizzaRequest => {
      log.info("Got {}", prq)
      whoWantsPizza = sender
      cheeseNeeded = randoms.nextInt(10)
      cheeseVault ? new CheeseRequest(cheeseNeeded) pipeTo self
    }
    case crs: CheeseResponse => {
      log.info("Making pizza from {} cheese", crs.amountOfCheese)
      whoWantsPizza ! new PizzaResponse
    }
    case cno: NoCheeseLeft => {
      context.system.scheduler.scheduleOnce(cno.secondsToWait seconds, cheeseVault, new CheeseRequest(cheeseNeeded))
    }
  }
}

class CheeseRepository extends Actor with akka.actor.ActorLogging {
  var cheeseLeft: Int = 0

  def addCheese(amountOfCheese: Int) = {
    cheeseLeft += amountOfCheese
    log.info("Added some new cheese: {}, left: {}", amountOfCheese, cheeseLeft)
  }

  def getCheese(amountOfCheese: Int) = {
    cheeseLeft -= amountOfCheese
    log.info("Took some cheese: {}, left: {}", amountOfCheese, cheeseLeft)
  }

  def receive = {
    case crq: CheeseRequest => {
      if (cheeseLeft > crq.amountOfCheese) {
        getCheese(crq.amountOfCheese)
        sender ! new CheeseResponse(crq.amountOfCheese)
      } else {
        log.info("We don't have enough cheese, waiting for some to appear...")
        sender ! new NoCheeseLeft(2)
      }
    }
    case ca: AddCheese => {
      addCheese(ca.newCheese)
    }
  }

}