package pl.pawelb

import akka.actor._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait MinionState
case object Happy extends MinionState
case object Afraid extends MinionState
case object Confused extends MinionState

sealed trait MinionData
case object Uninitialized extends MinionData
case object IHaveFood extends MinionData
case object NoFood extends MinionData

object MinionFsmDemo extends App {
  val system = ActorSystem("fsmActors")
  val minion = system.actorOf(Props[MinionFsm])

  system.scheduler.schedule(1 second, 2 seconds, minion, BananaMessage)
  system.scheduler.schedule(5 second, 4 seconds, minion, CatMessage)
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
    case Event(CatMessage, _) =>
      goto(Confused) using stateData
  }

  when(Confused) {
    case Event(BananaMessage, _) =>
      goto(Happy) using IHaveFood
    case Event(DogMessage, _) =>
      goto(Afraid) using NoFood
  }

  when(Happy) {
    case Event(DogMessage, _) =>
      goto(Afraid) using NoFood
    case Event(CatMessage, _) =>
      goto(Confused) using stateData
  }


  onTransition {
    case x -> y    => log.info("entering {} from {}", y, x)
  }

  initialize()
}

