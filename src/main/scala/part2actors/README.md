# Actor System
- An ActorSystem in Akka is a container for actors, managing their lifecycle and providing the necessary infrastructure for their communication. Setting up an ActorSystem is the first step in using Akka actors.

# Changing Actor Behaviour
### Part 1
- Changing actor behavior isn't explicitly used. Instead, the FussyKid actor's behavior changes implicitly by modifying its internal state (HAPPY or SAD) based on the messages it receives (either Food(VEGETABLE) or Food(CHOCOLATE)). This internal state affects how the actor responds to Ask messages, leading to different responses (KidAccept or KidReject) based on the current state.





