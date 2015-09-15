package pl.pawelb

import akka.actor._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait MinionState
case object Happy extends MinionState
case object Afraid extends MinionState

sealed trait MinionData
case object Uninitialized extends MinionData
case object IHaveFood extends MinionData

object MinionFsmDemo extends App {
  val system = ActorSystem("fsmActors")
  val minion = system.actorOf(Props[MinionFsm])

  system.scheduler.schedule(1 second, 2 seconds, minion, BananaMessage)
  system.scheduler.schedule(5 second, 10 seconds, minion, DogMessage)

  system.scheduler.scheduleOnce(30 seconds, new Runnable {
    override def run(): Unit = {
      system.shutdown
    }
  })
}

class MinionFsm extends FSM[MinionState, MinionData] {
  
  startWith(Happy, Uninitialized)

  when(Afraid) {
    case Event(BananaMessage, _) =>
      goto(Happy) using IHaveFood
  }

  when(Happy) {
    case Event(BananaMessage, _) =>
      stay
    case Event(DogMessage, _) =>
      goto(Afraid) using IHaveFood
  }

  onTransition {
    case x -> y    => log.info("entering {} from {}", y, x)
  }

  initialize()
}

