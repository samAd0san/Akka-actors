ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val akkaVersion = "2.6.20"
lazy val scalaTestVersion = "3.2.16"

lazy val root = (project in file("."))
  .settings(
    name := "Akka-actors",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )

// libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.20"