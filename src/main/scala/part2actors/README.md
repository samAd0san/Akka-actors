# Actor System
- An ActorSystem in Akka is a container for actors, managing their lifecycle and providing the necessary infrastructure for their communication. Setting up an ActorSystem is the first step in using Akka actors.

# Akka Supervisors:

- In Akka, supervisors are responsible for overseeing the lifecycle of child actors. They monitor the health of their children and take appropriate actions when failures occur. This includes restarting failed actors, escalating failures to higher-level supervisors, or stopping the entire supervision hierarchy.

# Actor
An actor in Akka is a fundamental unit of computation that encapsulates state and behavior. Actors communicate with each other by sending and receiving messages asynchronously. Each actor processes one message at a time, ensuring thread safety without needing low-level concurrency tools like locks.

# Changing Actor Behaviour
### Part 1
- Changing actor behavior isn't explicitly used. Instead, the FussyKid actor's behavior changes implicitly by modifying its internal state (HAPPY or SAD) based on the messages it receives (either Food(VEGETABLE) or Food(CHOCOLATE)). This internal state affects how the actor responds to Ask messages, leading to different responses (KidAccept or KidReject) based on the current state.

# Akka Configuration
- Akka Configuration sets up how your Akka system behaves, including things like logging, actor settings, and deployment options, using a application.conf file.

# Akka Config Setup
1. To get started with Akka Configuration, you need to create a application.conf file in your project's <b> src/main/resources </b> directory.
- ### src/main/resources/application.conf

- ```chatinput
  // src/main/resources/application.conf
  akka {
      actor {
      provider = "local"
      }
  loglevel = "DEBUG"
  stdout-loglevel = "DEBUG"
  }
  
2. Include the configuration in your Akka system initialization:
```chatinput
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class MyActor extends Actor with ActorLogging {
  def receive = {
    case msg => log.info(s"Received message: $msg")
  }
}

object AkkaConfigDemo extends App {
  val config = ConfigFactory.load() // Loads application.conf by default
  val system = ActorSystem("MySystem", config)
  val myActor = system.actorOf(Props[MyActor], "myActor")

  myActor ! "Hello, Akka!"
}
```
- <b> Output </b>
```chatinput
"C:\Program Files\Java\jdk-21\bin\java.exe" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.3\lib\idea_rt.jar=55894:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.3\bin" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath D:\Scala\Akka-actors\target\scala-2.13\classes;C:\Users\sohai\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor_2.13\2.6.20\akka-actor_2.13-2.6.20.jar;C:\Users\sohai\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-testkit_2.13\2.6.20\akka-testkit_2.13-2.6.20.jar;C:\Users\sohai\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\config\1.4.2\config-1.4.2.jar;C:\Users\sohai\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-java8-compat_2.13\1.0.0\scala-java8-compat_2.13-1.0.0.jar;C:\Users\sohai\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.14\scala-library-2.13.14.jar part2actors.IntroAkkaConfig
[DEBUG] [06/30/2024 11:47:29.479] [main] [EventStream] StandardOutLogger started
[DEBUG] [06/30/2024 11:47:29.668] [main] [EventStream(akka://MySystem)] logger log1-Logging$DefaultLogger started
[DEBUG] [06/30/2024 11:47:29.668] [main] [EventStream(akka://MySystem)] logger log1-Logging$DefaultLogger started
[DEBUG] [06/30/2024 11:47:29.670] [main] [EventStream(akka://MySystem)] Default Loggers started
[DEBUG] [06/30/2024 11:47:29.670] [main] [EventStream(akka://MySystem)] Default Loggers started
[DEBUG] [06/30/2024 11:47:29.706] [main] [akka.serialization.Serialization(akka://MySystem)] Replacing JavaSerializer with DisabledJavaSerializer, due to `akka.actor.allow-java-serialization = off`.
[INFO] [06/30/2024 11:47:29.726] [MySystem-akka.actor.default-dispatcher-6] [akka://MySystem/user/myAkkaConfigActor] Received Message: Hello, Akka
```

### Difference between ask and tell in Akka?
- 'ask' will send the message and return a future, which can be awaited until timeout or a reply is received.
- 'tell' will send the message and return immediately.

```
import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._

class MyActor extends Actor {
  def receive = {
    case "hello" => sender() ! "world"
  }
}

object Main extends App {
  val system = ActorSystem("MySystem")
  val myActor = system.actorOf(Props[MyActor], "myActor")

  // Using `ask`:
  val future = myActor.ask(Timeout(5 seconds)) {
    "hello"
  }
  future.onComplete {
    case Success(result) => println(s"Received result: $result")
    case Failure(e) => println(s"Failed with: $e")
  }

  // Using `tell`:
  myActor ! "hello"
}
```

**OUTPUT:** ``` Received result: world ```

- The output will be "world" only once. The tell line is not expecting a response, so it doesn't print anything. The ask line, on the other hand, waits for a response and prints it.
