name := "yinyang"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache
)

resolvers += "org.sedis" at "http://pk11-scratch.googlecode.com/svn/trunk/"
 
play.Project.playScalaSettings
