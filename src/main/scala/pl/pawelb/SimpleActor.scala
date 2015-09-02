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

object SimpleActors extends App {
  implicit val system = ActorSystem("simpleActors")
  val greetingActor = system.actorOf(Props[GreetingActor])
  greetingActor ! "Hello"
}

class GreetingActor extends Actor {
  def receive = {
    case message: String => {
      sender ! "Hi" 
    }
  }
}