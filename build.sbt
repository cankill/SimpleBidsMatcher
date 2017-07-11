
name := "SimpleStackExchange"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.github.scopt"           %% "scopt"           % "3.6.0",
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.5.0",
  "ch.qos.logback"             %  "logback-classic" % "1.1.7",
  "org.scalatest"              %% "scalatest"       % "3.0.1"   % Test,
  "org.scalamock"              %% "scalamock-scalatest-support" % "3.5.0" % Test

)

    
