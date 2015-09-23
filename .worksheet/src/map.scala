object map {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(56); 
  println("Welcome to the Scala worksheet");$skip(72); 
  
  var tagCount = new scala.collection.immutable.HashMap[String, Int];System.out.println("""tagCount  : scala.collection.immutable.HashMap[String,Int] = """ + $show(tagCount ));$skip(83); 
  
  val add = "as dasd as dasdasd".split(" ").groupBy(identity).mapValues(_.size);System.out.println("""add  : scala.collection.immutable.Map[String,Int] = """ + $show(add ));$skip(138); 
                                                  
  tagCount = tagCount ++ add.map{ case (k, v) => k -> (v + tagCount.getOrElse(k, 0)) };$skip(88); 

  tagCount = tagCount ++ add.map{ case (k, v) => k -> (v + tagCount.getOrElse(k, 0)) };$skip(14); val res$0 = 
  
  tagCount;System.out.println("""res0: scala.collection.immutable.HashMap[String,Int] = """ + $show(res$0));$skip(108); val res$1 = 
                                                  
  Map("a" -> 3.0, "b" -> 1.0, "c" -> 2.0).maxBy(_._2)._1;System.out.println("""res1: String = """ + $show(res$1))}
}
