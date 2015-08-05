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

//messages
sealed trait FlickrMessage
case class FlickrAllRequest() extends FlickrMessage
case class FlickrAllResponse() extends FlickrMessage

//dto
case class FlickrImageInfo(title: String, link: String, dateTaken: Option[Date], description: String, published: Date, author: String, authorId: Option[String], tags: String)

object TagParser extends App {
  implicit val system = ActorSystem("imageParser")

  val tagActor = system.actorOf(Props[FlickrTagParser])
  tagActor ! FlickrAllRequest
  
  system.scheduler.scheduleOnce(10 seconds) {
    system.shutdown()
  }

}

trait ActorHttp extends AkkaDemoConfig{
  implicit val formats = new org.json4s.DefaultFormats {
    override def dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
  }
  
  def makeHttpRequest(url: String, timeoutMilis: Int = 1000): HttpResponse[String] = {
      val httpRequest = Http(url).option(HttpOptions.connTimeout(timeoutMilis))
      val requestWithProxy = hasPath("http.proxyHost") match {
        case true  => httpRequest.proxy(getString("http.proxyHost"), getInt("http.proxyPort"))
        case _     => httpRequest
      }
      requestWithProxy.asString
  }
}

class FlickrTagParser extends Actor with ActorHttp with ActorLogging with AkkaDemoConfig {
  def receive = LoggingReceive({
    case FlickrAllRequest => {
      getPublicStreamImages.foreach { img => log.info(img.tags) }
    }
  })

  def getPublicStreamImages = {
      val responseBody = makeHttpRequest(getString("flickr.url.publicAll")).body.replace("'", "\\\\u0027")
      val parsedJson = parse(responseBody) \ "items"
      parsedJson.extract[List[FlickrImageInfo]]
  }
}
