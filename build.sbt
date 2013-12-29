name := "yinyang"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "com.typesafe" %% "play-plugins-redis" % "2.1.1"
)

resolvers += "org.sedis" at "http://pk11-scratch.googlecode.com/svn/trunk/"
 
play.Project.playScalaSettings
