package pl.pawelb

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.LoggingReceive
import akka.actor.ActorLogging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait MinionMessage
case class DogMessage() extends MinionMessage
case class CatMessage() extends MinionMessage
case class BananaMessage() extends MinionMessage

object MinionBecomeApp extends App with AkkaDemoConfig{
  implicit val system = ActorSystem("becomeActors")
  val owner = system.actorOf(Props[MinionOwner])
  
  system.scheduler.scheduleOnce(30 seconds, new Runnable {
  override def run(): Unit = {
     system.shutdown
    }
  })

}

class MinionOwner extends Actor with ActorLogging {
  val Minion = context.system.actorOf(Props[MinionActor])

  def receive = LoggingReceive({
    case msg: String => log.info("Minion says: {}", msg)
  })
  
  context.system.scheduler.schedule(1 second, 2 seconds, Minion, BananaMessage)
  context.system.scheduler.schedule(5 second, 10 seconds, Minion, DogMessage)
}


class MinionActor extends Actor {
  import context._
  def afraid: Receive = LoggingReceive({
    case DogMessage => sender ! "I am already afraid! :-/"
    case BananaMessage => {
      sender ! "Yay, banana! :-)"
      become(happy)
    }
  })

  def happy: Receive = LoggingReceive({
    case DogMessage => {
      sender ! "A dog, run! :-("
      become(afraid)
    }
    case BananaMessage => sender ! "I am already full of bananas :-D"
  })

  def receive = happy
}

