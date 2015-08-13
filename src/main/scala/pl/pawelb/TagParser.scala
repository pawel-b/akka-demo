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
case class GetTagCount() extends FlickrMessage
case class FlickrAllRequest() extends FlickrMessage
case class FlickrAllResponse() extends FlickrMessage

//dto
case class FlickrImageInfo(title: String, link: String, dateTaken: Option[Date], description: String, published: Date, author: String, authorId: Option[String], tags: String)

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

object TagParser extends App {
  implicit val system = ActorSystem("imageParser")
  val tagMain = system.actorOf(Props[FlickrTagMain])
  system.scheduler.schedule(1 second, 1 second, tagMain, FlickrAllRequest())

  system.scheduler.scheduleOnce(30 seconds) {
    system.shutdown()
  }

}

class FlickrTagMain extends Actor with ActorHttp with ActorLogging with AkkaDemoConfig {
  val tagCounter = context.actorOf(Props[FlickrTagCounter])
  context.system.scheduler.schedule(5 second, 5 second, tagCounter, GetTagCount())
  
  def receive = LoggingReceive({
    case req: FlickrAllRequest => {
      getPublicStreamImages.foreach { 
        val tagParser = context.actorOf(Props(new FlickrTagParser(tagCounter)))
        tagParser ! _
      }
    }
  })

  def getPublicStreamImages = {
      val responseBody = makeHttpRequest(getString("flickr.url.publicAll")).body.replace("'", "\\\\u0027")
      val parsedJson = parse(responseBody) \ "items"
      parsedJson.extract[List[FlickrImageInfo]]
  }

}

class FlickrTagParser(counterRef: ActorRef) extends Actor with ActorHttp with ActorLogging with AkkaDemoConfig {

  def receive = LoggingReceive({
    case req: FlickrImageInfo => {
      req.tags.split(" ").filter(x => x != "").foreach { x => counterRef ! x }
    }
  })

}

class FlickrTagCounter extends Actor with ActorHttp with ActorLogging with AkkaDemoConfig {
  var tagCount = new scala.collection.mutable.HashMap[String, Int]
  
  def receive = LoggingReceive({
    case tag: String => {
      tagCount(tag) = tagCount.getOrElse(tag, 0) + 1 
    }
    case getCount: GetTagCount => {
      log.info("Tag count max {}", tagCount.toSeq.sortBy(f => f._2).takeRight(5))
    }
  })
  
}