package pl.pawelb

import pl.pawelb.pizza._
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
        val secondsToCheckAgain = 5
        log.info("We're not done yet, will check in {} seconds...", secondsToCheckAgain)
        context.system.scheduler.scheduleOnce(secondsToCheckAgain seconds, self, CloseCounter())
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
        case x: Int if (x > 0) => cheeseVault ? CheeseRequest(prq) pipeTo self
        case y => prq.whoWantsPizza ! PizzaResponse(y)
      }
      
    }
    case crs: CheeseResponse => {
      log.info("\tMaking pizza from {} cheese", crs.forPizzaRequest.cheeseNeeded)
      crs.forPizzaRequest.whoWantsPizza ! new PizzaResponse(crs.forPizzaRequest.cheeseNeeded)
    }
    case cno: NoCheeseLeft => {
      log.info("\tOK, I'll wait {} seconds", cno.secondsToWait)
      context.system.scheduler.scheduleOnce(cno.secondsToWait seconds, cheeseVault, CheeseRequest(cno.forPizzaRequest))
    }
  })
}

