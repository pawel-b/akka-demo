package pl.pawelb

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._
import java.time.LocalDateTime
import scala.concurrent.Future
import java.util.Date

object BasicTransformation extends App
{

    implicit val system = ActorSystem("Sys")
    import system.dispatcher

    implicit val materializer = ActorMaterializer()

    val text =
      """|Lorem Ipsum is simply dummy text of the printing and typesetting industry.
         |Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
         |when an unknown printer took a galley of type and scrambled it to make a type
         |specimen book.""".stripMargin
         
    val dateSource = Source(5 seconds, 1 second, LocalDateTime.now())
    val textSource = Source(() => text.split("\\s").iterator)
    
    val makeUpper = Flow[String].map(_.toUpperCase)
    
    val printSink = Sink.foreach[Any] {
      println(_)
    }
    
    textSource.via(makeUpper).to(printSink).run()
      
    dateSource.to(printSink).run()
      
}