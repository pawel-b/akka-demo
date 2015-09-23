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

/**
 * Basic example of Akka streams usage
 * We have two sources: words, some integers and some pipes that can process them
 */
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
         
    def currentDate = LocalDateTime.now() 
         
    //sources
    val textSource = Source(() => text.split("\\s").iterator)
    val intSource = Source(1 to 30)

    //pipes
    val filterEmptyAndMakeUpper = Flow[String].filter(!_.isEmpty())map(_.toUpperCase)
    val getSqrt = Flow[Int].map(Math.sqrt(_))

    //output sink
    val printSink = Sink.foreach[Any] {
      println(_)
    }
    
    //put pipes together and run them
    textSource.via(filterEmptyAndMakeUpper).to(printSink).run()
    intSource.via(getSqrt).to(printSink).run()

    //shutdown after some time
    system.scheduler.scheduleOnce(15 seconds, new Runnable {
      override def run(): Unit = {
         system.shutdown
        }
    })

      
}