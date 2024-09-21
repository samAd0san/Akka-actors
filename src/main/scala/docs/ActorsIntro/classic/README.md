### Ping Pong Game with Akka Actors
**Summary:**<br>
This code implements a simple "ping pong" game using Akka actors. The game involves two actors: Pinger and Ponger. The Pinger actor initiates the game by sending a "Ping" message to the Ponger. The Ponger responds by sending a "Pong" message back to the Pinger. This process continues until a specified number of "pings" have been exchanged.

**Key Concepts:**<br>
**1. Actors:** Pinger and Ponger are the two actors involved in the game.<br>
**2. Messages:** "Ping" and "Pong" are the messages exchanged between the actors.<br>
**3. State:** The Pinger actor maintains a countdown to track the number of "pings" sent.<br>
**4. Termination:** The game ends when the countdown reaches 0, and both actors are terminated using PoisonPill.<br>

```scala
// ... (rest of the code)

object PingPong {
  // ... (message definitions, actor classes, main function)
}
```

**How it Works:**<br>
1. The HelloWorldMain actor (not explicitly shown in the code) creates the Pinger and Ponger actors.
2. The HelloWorldMain actor sends an initial "Ping" message to the Ponger actor.
3. The Ponger actor receives the "Ping" message and sends a "Pong" message back to the sender (which is the Pinger).
4. The Pinger actor receives the "Pong" message, processes it, and sends another "Ping" message if the countdown is not 0.
5. This process continues until the countdown reaches 0, at which point both actors are terminated.

**Additional Notes:**<br>
- The sender() method is used to get a reference to the actor that sent the current message.
- The PoisonPill message is used to terminate an actor.
- The code uses Akka's scheduling mechanism to initiate the game by sending the initial "Ping" message.