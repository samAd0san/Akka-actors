### What is Props?

Props is a configuration class used in Akka to define how an actor should be created. It works like a recipe or blueprint for creating actor instances. It contains details like the actor's constructor and any additional configuration (e.g., dispatcher settings). The Props object is immutable and can be safely shared.

```scala
import akka.actor.{Actor, ActorSystem, Props}

// 1. Basic actor without constructor arguments
class MySimpleActor extends Actor {
  def receive: Receive = { case msg => println(s"Received: $msg") }
}

val system = ActorSystem("MySystem")
val props1 = Props[MySimpleActor]() // Props instance without arguments
val simpleActor = system.actorOf(props1, "simpleActor")
simpleActor ! "Hello"

// 2. Actor with constructor arguments
class MyActorWithArgs(name: String) extends Actor {
  def receive: Receive = { case msg => println(s"Hello $name, received: $msg") }
}

// Using Props to pass arguments to the actor
val props2 = Props(new MyActorWithArgs("John")) // Props with arguments
val actorWithArgs = system.actorOf(props2, "actorWithArgs")
actorWithArgs ! "How are you?"

```

**Explanation:**

1. Props[MySimpleActor]() creates an instance of Props for MySimpleActor, which doesn't take constructor arguments.
2. Props(new MyActorWithArgs("John")) creates an instance of Props that passes the string "John" to the actor's constructor.
3. You then pass this Props to the actorOf method to create the actor in the ActorSystem.

So, passing Props means you're providing a blueprint with all the necessary information to create and configure the actor.