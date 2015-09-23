package pl.pawelb

import akka.actor._
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import akka.event.LoggingReceive
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.ExecutionContext.Implicits.global
import scalaj.http.Http
import scalaj.http.HttpOptions
import scalaj.http.HttpResponse
import java.util.Date
import scala.io.Source
import java.text.SimpleDateFormat

/**
 * Actor hello world
 */
object SimpleActors extends App {
  val system = ActorSystem("simpleActors")
  val greetingActor = system.actorOf(Props[GreetingActor])
  greetingActor ! "Hello"
  system.scheduler.scheduleOnce(1 seconds, new Runnable {
    override def run(): Unit = {
      system.shutdown
    }
  })
}

class GreetingActor extends Actor with ActorLogging {
  def receive = {
    case message: String => {
      log.info("Message received: {}", message)
    }
  }
}