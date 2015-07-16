name := "akka-demo"

version := "1.0"

scalaVersion := "2.11.5"

unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
    Seq( base / "src/main/webapp" )
}

net.virtualvoid.sbt.graph.Plugin.graphSettings

resolvers ++= Seq(
    "Typesafe repository snapshots"    at "http://repo.typesafe.com/typesafe/snapshots/"
  , "Typesafe repository releases"     at "http://repo.typesafe.com/typesafe/releases/"
  , "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/"
  , "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases"
  , "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots"
  , "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging"
  , "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/"
  , "Twitter Repository"               at "http://maven.twttr.com"
  , "sourceforge"                      at "http://oss.sonatype.org/content/groups/sourceforge/"
)

val akkaVersion = "2.3.12"

libraryDependencies ++= Seq(
 "com.typesafe" % "config" % "1.2.1"
 ,"ch.qos.logback" % "logback-classic" % "1.1.2"
 ,"com.typesafe.akka" % "akka-actor_2.11" % akkaVersion
 ,"com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion
 ,"com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion
)
