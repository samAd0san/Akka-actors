# Akka Actors
- Akka is a toolkit and runtime for building highly concurrent, distributed, and resilient message-driven applications on the JVM.<br><br>

- A toolkit is a collection of libraries and tools designed to simplify and streamline the development of specific types of
  applications or functionalities, providing reusable components and utilities.<br><br>

- The Akka toolkit includes libraries and tools such as: <br>
  Akka Actors - concurrency <br>
  Akka Streams - reactive stream processing <br>
  Akka HTTP - building RESTful services <br>
  Akka Cluster - distributed systems <br>
  Akka Persistence - state management <br>
  <br>
- In Elaborated way <br>
  Akka Actors: For building concurrent and scalable applications using the actor model.<br><br>
  Akka Streams: For processing and handling large streams of data in a reactive and non-blocking manner.<br><br>
  Akka HTTP: For creating RESTful web services and clients.<br><br>
  Akka Cluster: For building distributed and clustered systems.<br><br>
  Akka Persistence: For managing state and enabling event sourcing in actor-based applications.<br><br>

# Concurrent and scalable applications<br>
  Concurrent and scalable applications refer to software systems designed to efficiently handle multiple
  tasks or users simultaneously while maintaining performance as workload increases.<br><br> Concurrency allows
  tasks to run concurrently, utilizing resources efficiently and reducing idle time.<br><br> Scalability ensures
  the application can handle increased load by adding resources or nodes without sacrificing performance,
  thereby accommodating growing user bases or data volumes effectively.
  
# Akka Setup
- NewProject -> java 21 -> scala & sbt -> create project
- build.sbt
```chatinput
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "Akka-actors"
  )
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.20"
```
- build.properties
```sbt.version = 1.10.0```
- Create src/main/scala/Playground/Playground.scala
```chatinput
package Playground

import akka.actor.ActorSystem

object Playground extends App {
  val actorSystem = ActorSystem("HelloAkka")
  println(actorSystem.name) // HelloAkka
}
```