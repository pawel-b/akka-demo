import akka.actor.{ActorRef, ActorSystem, Actor}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import org.scalatest._
import pl.pawelb.pizza.{NoCheeseLeft, PizzaRequest, CheeseRequest, CheeseRepository}

class CheeseRepositoryTest extends TestKit(ActorSystem("testSystem")) with ImplicitSender with WordSpecLike with MustMatchers {

  "A cheese repository actor without cheese" must {
    "send back a NoCheeseLeft response" in {
      val pizzaRequest = new PizzaRequest(5, null)
      // Creation of the TestActorRef
      val actorRef = TestActorRef[CheeseRepository]
      actorRef ! new CheeseRequest(pizzaRequest)
      expectMsg(new NoCheeseLeft(pizzaRequest, 2))
    }

  }
}