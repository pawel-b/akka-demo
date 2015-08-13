package pl.pawelb

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object CheckMaxThreads extends App {

  for (i <- 1 to 100000) {
    if (i % 100 == 0) println("starting thread " + i)
    new PlainThread().start()
  }

}


class PlainThread extends Thread {
  override def run = {
    Thread.sleep(100000)
  }
}

object CheckMaxActors extends App {

  implicit val system = ActorSystem("testActorSystem")

  for (i <- 1 to 3000000) {
    if (i % 100 == 0) println("starting actor " + i)
    system.actorOf(Props[PlainActor])
  }
  
  system.scheduler.scheduleOnce(10 seconds) {
    system.shutdown()
  }

}

class PlainActor extends Actor {
  
  def receive = {
    case x => println("Received")
  }
}

