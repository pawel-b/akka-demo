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
import akka.event.LoggingReceive
import scala.concurrent.{ future, blocking}

case class CloseCounter()

//messages
sealed trait PizzaMessage
case class PizzaBulkRequest(howMany: Int) extends PizzaMessage
case class PizzaRequest(cheeseNeeded: Int, whoWantsPizza: ActorRef) extends PizzaMessage
case class PizzaResponse(amountOfCheese: Int) extends PizzaMessage

sealed trait CheeseMessage
case class CheeseRequest(forPizzaRequest: PizzaRequest, amountOfCheese: Int) extends CheeseMessage
case class CheeseResponse(forPizzaRequest: PizzaRequest, amountOfCheese: Int) extends CheeseMessage
case class NoCheeseLeft(forPizzaRequest: PizzaRequest) extends CheeseMessage
case class AddCheese(newCheese: Int) extends CheeseMessage

//main
object PizzaDemoBasics extends App {
  val system = ActorSystem("pizza-system")
  val pizzaCounterActor = system.actorOf(Props[PizzaCounter])
}

class PizzaCounter extends Actor with akka.actor.ActorLogging {
  val randoms = new Random
  var pizzasToServe = 0
  var pizzasServed = 0

  val cheeseVaultActor = context.system.actorOf(Props[CheeseRepository])
  val pizzaActor = context.system.actorOf(Props(new PizzaMaker(cheeseVaultActor)).withRouter(FromConfig()), name = "pizzaMaker")

  def receive = LoggingReceive({
    case bulk: PizzaBulkRequest => {
      log.info("--> I want new pizzas: {}", bulk.howMany)
      pizzasToServe += bulk.howMany
      for (i <- 1 to bulk.howMany) {
        pizzaActor ! PizzaRequest(randoms.nextInt(10), self)
      }
    }
    case crs: PizzaResponse => {
      log.info("<-- Yay, got new pizza with {} cheese!", crs.amountOfCheese)
      pizzasServed += 1
    }
    case cls: CloseCounter => {
      log.info("Pizzas served {} of requested {}", pizzasServed, pizzasToServe)
      if (pizzasServed == pizzasToServe) {
        log.info("We're done, closing!")
        context.system.shutdown()
      }
      else {
        log.info("We're not done yet...")
        context.system.scheduler.scheduleOnce(5 seconds, self, CloseCounter())
      }
      
    }
  })

  context.system.scheduler.schedule(3 seconds, 1 second, new Runnable() {
    override def run(): Unit = {
      if (pizzasToServe < 100) {
        self ! PizzaBulkRequest(randoms.nextInt(5) + 1)
      }
    }
  })
  
  context.system.scheduler.schedule(1 second, 10 seconds, new Runnable() {
    override def run(): Unit = {
      cheeseVaultActor ! AddCheese(randoms.nextInt(100))
    }
  })

  context.system.scheduler.scheduleOnce(60 seconds, self, CloseCounter()) 

}

class PizzaMaker(cheeseVault: ActorRef) extends Actor with akka.actor.ActorLogging {
  implicit val timeout = Timeout(60 seconds)
  val randoms = new Random

  def receive = LoggingReceive({
    case prq: PizzaRequest => {
      log.info("\tGot pizza request, I need {} cheese for this pizza", prq.cheeseNeeded)
      
      prq.cheeseNeeded match {
        case x: Int if (x > 0) => cheeseVault ? CheeseRequest(prq, prq.cheeseNeeded) pipeTo self
        case y => prq.whoWantsPizza ! PizzaResponse(y)
      }
      
    }
    case crs: CheeseResponse => {
      log.info("\tMaking pizza from {} cheese", crs.amountOfCheese)
      crs.forPizzaRequest.whoWantsPizza ! new PizzaResponse(crs.amountOfCheese)
    }
    case cno: NoCheeseLeft => {
      val secondsToWait = 2
      log.info("\tOK, I'll wait {} seconds", secondsToWait)
      context.system.scheduler.scheduleOnce(secondsToWait seconds, cheeseVault, CheeseRequest(cno.forPizzaRequest, cno.forPizzaRequest.cheeseNeeded))
    }
  })
}

class CheeseRepository extends Actor with akka.actor.ActorLogging {
  var cheeseLeft: Int = 0

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
      if (cheeseLeft > crq.amountOfCheese) {
//        blocking {
//          log.info("\t\tSleeping...")
//          Thread.sleep(1000)
//        }
        getCheese(crq.amountOfCheese)
        assert(cheeseBeforeTake == crq.amountOfCheese + cheeseLeft, "Wrong cheese amount")
        log.info("\t\tFrom {} cheese took {}, left: {}", cheeseBeforeTake, crq.amountOfCheese, cheeseLeft)
        sender ! CheeseResponse(crq.forPizzaRequest, crq.amountOfCheese)
      } else {
        log.info("\t\tWe don't have enough cheese, waiting for some to appear...")
        sender ! NoCheeseLeft(crq.forPizzaRequest)
      }
    }
    case ca: AddCheese => {
      addCheese(ca.newCheese)
    }
  })

}