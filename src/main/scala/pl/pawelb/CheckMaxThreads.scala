package pl.pawelb

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object CheckMaxThreads extends App {

  val start = System.currentTimeMillis()
  for (i <- 1 to 3000000) {
    if (i % 100 == 0) println(s"starting actors $i")
    if (i == 10000) {
      val end = System.currentTimeMillis() - start
      println(s"starting $i actors took $end ms")
    } 
    new PlainThread().start()
  }

}


class PlainThread extends Thread {
  override def run = {
    Thread.sleep(10000000)
  }
}

object CheckMaxActors extends App {

  implicit val system = ActorSystem("testActorSystem")

  val start = System.currentTimeMillis()
  for (i <- 1 to 3000000) {
    if (i % 100 == 0) println(s"starting actors $i")
    if (i == 10000) {
      val end = System.currentTimeMillis() - start
      println(s"starting $i actors took $end ms")
    } 
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

