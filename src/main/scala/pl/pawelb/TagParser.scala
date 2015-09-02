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
case class FlickrGetTagsRequest(userId: String) extends FlickrMessage
case class TagCountRequest() extends FlickrMessage
case class TagCountResponse(tagCounts: Map[String, Int]) extends FlickrMessage

//dto
case class FlickrImageInfo(title: String, link: String, date_taken: Option[Date], description: String, published: Date, author: String, author_id: Option[String], tags: String)

trait FlickrHttpEnabled extends AkkaDemoConfig{
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

  def getFlickrImageInfos(url: String) : List[FlickrImageInfo]= {
      val responseBody = makeHttpRequest(url).body.replace("'", "\\\\u0027")
      val parsedJson = parse(responseBody) \ "items"
      parsedJson.extract[List[FlickrImageInfo]]
  }

  def getFlickrUserIdsFromImageInfos(url: String) : List[String] = {
    getFlickrImageInfos(url).map(i => i.author_id).flatten
  }

  def getFlickrTagsFromImageInfos(url: String) : List[String] = {
    getFlickrImageInfos(url).map(i => i.tags).filter(x => x != "")
  }
}

object TagApp extends App with AkkaDemoConfig{
  implicit val system = ActorSystem("imageParser")
  val tagMain = system.actorOf(Props[FlickrTagMain])
  tagMain ! FlickrGetTagsRequest(getString("flickr.userId"))
}

class FlickrTagMain extends Actor with ActorLogging with AkkaDemoConfig {
  
  def receive = {
    case req: FlickrGetTagsRequest => {
      val tagCounter = context.actorOf(Props[FlickrTagCounter])
      val favouritesActor = context.actorOf(Props(new FlickrFavouritesInfoDownloader(tagCounter)))
      val friendsFavouritesActor = context.actorOf(Props(new FlickrFriendsFavouritesInfoDownloader(tagCounter)))
      favouritesActor ! req
      friendsFavouritesActor ! req

      context.system.scheduler.scheduleOnce(5 seconds, tagCounter, TagCountRequest())
    }
    case res: TagCountResponse => {
      val topTags = res.tagCounts.toSeq.sortWith(_._2 > _._2).take(5)
      log.info("Top tags: {}", topTags)

      context.system.scheduler.scheduleOnce(5 seconds, new Runnable {
        override def run(): Unit = {
          context.system.shutdown
        }
      })
    }
  }
}

class FlickrFavouritesInfoDownloader(counterRef: ActorRef) extends Actor with FlickrHttpEnabled with ActorLogging {
  def receive = LoggingReceive({
    case req: FlickrGetTagsRequest => {
      getFlickrTagsFromImageInfos(String.format(getString("flickr.url.favourites"), req.userId)).foreach { tag => counterRef ! tag }
      context.stop(self)
    }
  })
}

class FlickrFriendsFavouritesInfoDownloader(counterRef: ActorRef) extends Actor with FlickrHttpEnabled with ActorLogging {
  def receive = LoggingReceive({
    case req: FlickrGetTagsRequest => {
      val userIds = getFlickrUserIdsFromImageInfos(String.format(getString("flickr.url.friends"), req.userId))
      log.info("Friend ids: {}", userIds)
      userIds.foreach {id => 
        val favouritesActor = context.actorOf(Props(new FlickrFavouritesInfoDownloader(counterRef)))
        favouritesActor ! FlickrGetTagsRequest(id)
      }
      context.stop(self)
    }
  })
}

class FlickrTagCounter extends Actor with ActorLogging with AkkaDemoConfig {
  var tagCount = new scala.collection.mutable.HashMap[String, Int]
  
  def receive = LoggingReceive({
    case req: String => {
      req.split(" ").foreach {tag =>       
        tagCount(tag) = tagCount.getOrElse(tag, 0) + 1
      }
    }
    case getCount: TagCountRequest => {
      sender ! TagCountResponse(tagCount.toMap)
      context.stop(self)
    }
  })
  
}