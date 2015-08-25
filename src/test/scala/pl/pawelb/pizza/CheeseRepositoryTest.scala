package pl.pawelb.pizza

import akka.actor.ActorSystem
import akka.testkit._
import org.scalatest._
import pl.pawelb.pizza._

class CheeseRepositoryTest extends TestKit(ActorSystem("testSystem")) with ImplicitSender with WordSpecLike with MustMatchers {

  "A cheese repository actor without cheese" must {
    "send back a NoCheeseLeft response" in {
      val cheeseRepository = TestActorRef[CheeseRepository]
      val pizzaRequest = new PizzaRequest(5, null)
      cheeseRepository ! new CheeseRequest(pizzaRequest)
      expectMsg(new NoCheeseLeft(pizzaRequest, 2))
    }

  }

  "A cheese repository actor with cheese" must {
    "send back a cheese if requested" in {
      val cheeseRepository = TestActorRef[CheeseRepository]
      val pizzaRequest = new PizzaRequest(5, null)
      cheeseRepository ! new AddCheese(10)
      
      cheeseRepository ! new CheeseRequest(pizzaRequest)
      expectMsg(new CheeseResponse(pizzaRequest))

      cheeseRepository ! new CheeseRequest(pizzaRequest)
      expectMsg(new CheeseResponse(pizzaRequest))

      cheeseRepository ! new CheeseRequest(pizzaRequest)
      expectMsg(new NoCheeseLeft(pizzaRequest, 2))
    }

  }
}