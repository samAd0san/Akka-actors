# Why Akka?
Akka is an excellent choice for building concurrent, parallel, and distributed systems due to its high-level abstraction, which simplifies handling complex tasks that traditionally require low-level concurrency mechanisms. Here’s why Akka stands out:

**Simplifies Concurrency:** Traditional concurrency models use low-level constructs like atomics and locks to manage shared state across threads. Atomics These are operations (like atomic increments, swaps, or compares) that are guaranteed to complete without interruption. They ensure that when a thread modifies a shared variable, no other thread can interfere, allowing for safe concurrent updates without locks.

While a lock is a mechanism that prevents multiple threads from accessing a shared resource at the same time. When a thread locks a resource, other threads must wait until the lock is released. This ensures that only one thread modifies the shared state at a time, preventing data corruption. However, these approaches can lead to deadlocks (where threads wait endlessly for resources) or contention (when too many threads wait on a lock). Managing these is error-prone and difficult, especially in large systems.

**Actor Model:** Akka uses the actor model, where each actor is an independent unit with its own state, and it communicates with other actors via asynchronous message passing. Actors don’t share state, which means there's no need for locks or atomics. This removes the risk of deadlocks or race conditions, making concurrency far easier to handle.

**Fault Tolerance:** Akka actors are also resilient, meaning they can recover from failures in the system, which is hard to achieve with traditional concurrency models. The actor model helps isolate failures and ensures that the overall system remains stable.

**Scalability:** While atomics and locks are useful for managing concurrency within a single process or machine, Akka can easily scale beyond a single machine to support distributed systems. By abstracting away the complexity of managing state across different machines, Akka allows systems to scale horizontally across servers, handling more traffic and bigger workloads effortlessly.

In short, Akka offers a cleaner, safer, and more scalable way to manage concurrency compared to low-level mechanisms like atomics and locks.
# What is Akka?
   Akka is a toolkit for building reactive applications that are designed to be responsive, resilient, elastic, and message-driven. Here’s a breakdown of what these terms mean:

**Responsive:** Akka systems are designed to respond quickly and consistently to user or system inputs, even under heavy load or failure.

**Resilient:** Akka's actor model allows systems to recover from failures gracefully. Each actor is isolated, so if one actor fails, it doesn't bring down the whole system.

**Elastic:** Akka systems can scale up or down based on the load. If more resources are needed, the system can spawn more actors or distribute tasks across more machines.

**Message-Driven:** Akka actors communicate by passing messages asynchronously. This decouples components, enabling non-blocking operations and reducing the complexity of managing interactions between different parts of the system.

The actor model in Akka is a key feature. Each actor:<br>
**Encapsulates state** and behavior.<br>
**Processes one message at a time,** ensuring thread safety without the need for locks or atomics.<br>
**Communicates via messages** rather than direct method calls, ensuring loose coupling and better system flexibility.

# Low-Level Concurrency Constructs and Why Akka is Better
Low-level concurrency constructs like atomics and locks are mechanisms used to control how multiple threads interact with shared data:

**Atomics:** These are operations that happen in one go, preventing any other thread from interfering. While fast, atomics are limited in scope and cannot handle complex synchronization needs.

**Locks:** Locks ensure that only one thread can access a shared resource at a time, preventing race conditions but introducing the risk of deadlocks and performance bottlenecks if too many threads compete for a lock.

While these tools work for small-scale or low-level concurrency management, they become difficult to manage in large, distributed systems. This is where Akka shines:

**No Shared State:** Akka’s actor model eliminates the need for atomics or locks by ensuring actors never share mutable state.
Asynchronous Messaging: Actors communicate asynchronously, meaning they send messages without waiting for a response. This non-blocking interaction helps scale the system more easily.
Concurrency Without Complexity: Akka abstracts away the complexity of low-level concurrency by providing an easy-to-use actor system where concurrency is handled automatically without explicit synchronization tools like locks.

# Terms
**Race Condition:**
A race condition occurs when two or more threads or processes attempt to modify shared data simultaneously, and the final outcome depends on the timing of their execution. This can lead to unpredictable behavior and bugs because the operations on shared data are not properly synchronized.

For example, if two threads try to increment the same variable at the same time, they may read the same initial value, increment it separately, and write the new value back, leading to the variable being incremented only once instead of twice. This lack of coordination causes incorrect or unexpected results, making race conditions difficult to detect and debug.

In multi-threaded environments, race conditions are common when using shared resources without appropriate safeguards like locks, mutexes, or the actor model (as used in Akka) to ensure that only one thread or actor can modify shared data at a time.

**Concurrent:** Multiple tasks or actors are executed in overlapping time periods but not necessarily simultaneously, focusing on managing multiple tasks efficiently within a single system or process.

**Parallel:** Tasks or actors run simultaneously on different CPU cores or threads, allowing multiple computations to happen at the same time, improving speed and efficiency.

**Distributed:** Tasks or actors run across multiple, separate machines or systems that communicate with each other over a network, ensuring scalability and fault tolerance by spreading workloads.
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