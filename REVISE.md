**REMINDER:** Check the README.md file the main one and also nested one.<br>

**Akka Actors Overview:**<br>
Akka Actors provide a model for building concurrent and distributed systems. They simplify the development of parallel applications by encapsulating state and behavior in individual units (actors) that communicate through asynchronous message passing. Each actor processes messages sequentially, maintaining its own state and managing its lifecycle.<br><br> 
**Key concepts include:**<br>
**Actors:** Independent entities that perform computations and manage state.<br>
**Messages:** Units of communication between actors.<br>
**Mailboxes:** Queues where messages are stored until an actor processes them.<br>
**Supervision:** Mechanism for handling errors in child actors.<br>

**Akka Streams Overview:**<br>
Akka Streams is a library for handling large streams of data with backpressure support, allowing you to process data in a memory-efficient and responsive manner. It uses a model based on the Reactive Streams specification.

**Key concepts include:**<br>
**Source:** Origin of a stream of elements.<br>
**Flow:** Intermediate processing stage that transforms elements.<br>
**Sink:** Endpoint that consumes the stream of elements.<br>
**Backpressure:** Mechanism to handle situations where producers generate data faster than consumers can process it.<br>

**Revision Projects:**<br>
**Simple Chat Application:** Build a chat system using Akka Actors to handle messaging between users and manage state.

**Data Pipeline:** Create a data processing pipeline using Akka Streams to read, transform, and write data efficiently, implementing various operators like map, filter, and reduce.

**Real-time Analytics:** Develop a real-time analytics system using Akka Streams to process streaming data, apply transformations, and produce aggregated results.

**File Processing:** Implement a file processing system where Akka Streams read large files, process them line by line or in chunks, and write the results to another file.

**API Gateway:** Design an API gateway that uses Akka Actors to manage and route requests to different services, handling failures and retries.