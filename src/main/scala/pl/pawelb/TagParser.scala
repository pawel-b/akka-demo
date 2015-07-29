package pl.pawelb

import akka.http._
import akka.actor._
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.event.LoggingReceive
import pl.pawelb.AkkaDemoConfig
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.model.Uri.apply

sealed trait FlickrMessage
case class FlickrAllRequest() extends FlickrMessage
case class FlickrAllResponse() extends FlickrMessage


object TagParser extends App {
  implicit val system = ActorSystem("imageParser")

  val tagActor = system.actorOf(Props[FlickrTagParser])
  tagActor ! FlickrAllRequest
  
  system.scheduler.scheduleOnce(10 seconds) {
    system.shutdown()
  }

}

class FlickrTagParser extends Actor with ActorLogging with AkkaDemoConfig {
  implicit val materializer = ActorMaterializer()
  
  def receive = LoggingReceive({
    case FlickrAllRequest =>
      val responseFuture: Future[HttpResponse] = Http(context.system).singleRequest(HttpRequest(uri = getString("flickr.url.publicAll")))
      responseFuture.onComplete { x => log.info("Got response: {}", x) }
  })

}
