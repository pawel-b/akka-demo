import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp

object test {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(89); 

	val i = 5;System.out.println("""i  : Int = """ + $show(i ));$skip(34); 
	
	println(s"starting thread $i");$skip(46); 
	
  println("Welcome to the Scala worksheet");$skip(46); 
  
  implicit val system = ActorSystem("Sys");System.out.println("""system  : akka.actor.ActorSystem = """ + $show(system ));$skip(77); 
  
  
  val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");System.out.println("""sdf  : java.text.SimpleDateFormat = """ + $show(sdf ));$skip(44); val res$0 = 
  
  sdf.parse("2015-07-25T15:29:34-08:00");System.out.println("""res0: java.util.Date = """ + $show(res$0));$skip(45); 
  
  val textSource = Tcp().bind("http", 80);System.out.println("""textSource  : akka.stream.scaladsl.Source[akka.stream.scaladsl.Tcp.IncomingConnection,scala.concurrent.Future[akka.stream.scaladsl.Tcp.ServerBinding]] = """ + $show(textSource ));$skip(36); val res$1 = 
	
  "asdas das das dasd".split(" ");System.out.println("""res1: Array[String] = """ + $show(res$1))}
}
