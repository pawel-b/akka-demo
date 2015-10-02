package pl.pawelb

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * How many threads can we have?
 */
object CheckMaxThreads extends App {

  val start = System.currentTimeMillis()
  for (i <- 1 to 3000000) {
    if (i % 100 == 0) println(s"starting threads $i")
    if (i == 10000) {
      val end = System.currentTimeMillis() - start
      println(s"starting $i threads took $end ms")
    } 
    new PlainThread().start()
  }

}


class PlainThread extends Thread {
  override def run = {
    Thread.sleep(10000000)
  }
}
