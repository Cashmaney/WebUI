name := """WebUI"""

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += evolutions

libraryDependencies ++= Seq(
  "javax.inject" % "javax.inject" % "1",
  "org.webjars" % "bootstrap" % "3.3.4",
  "org.webjars" % "angular-ui-bootstrap" % "0.13.0",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "com.github.nscala-time" %% "nscala-time" % "2.10.0",
  "mysql" % "mysql-connector-java" % "5.1.35",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  ws
)
