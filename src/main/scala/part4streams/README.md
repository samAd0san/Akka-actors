# Akka Streams
**Akka Streams** is a toolkit for building asynchronous, non-blocking data processing pipelines. Its main functionality is to handle large or continuous streams of data efficiently with built-in support for back-pressure to manage data flow and resource usage.

**Key features:**<br>
**Sources and Sinks** for creating and consuming data.<br>
**Flows** for transforming data.<br>
**Graphs** for complex stream processing.<br>
**Materializer** for running streams and managing resources.<br>

# Visual Representation of Akka Streams Flow
```scala
          Source
            |
            v
    +----------------+
    |                |
    |    Flow        |
    |  (Processing   |
    |  Stage 1)      |
    |                |
    +--------+-------+
             |
             v
    +--------+-------+
    |                |
    |    Flow        |
    |  (Processing   |
    |  Stage 2)      |
    |                |
    +--------+-------+
             |
             v
    +--------+-------+
    |                |
    |    Flow        |
    |  (Processing   |
    |  Stage 3)      |
    |                |
    +--------+-------+
             |
             v
           Sink
```
**Key Concepts Illustrated**

**1. Source:** The starting point of the stream, producing data elements.<br>
**Example:** Source(1 to 100)

**2. Flows:** Intermediate processing stages that transform the data.<br>
**Example:** Flow[Int].map(_ * 2)

**3. Sink:** The endpoint where data is consumed or outputted.<br>
**Example:** Sink.foreach(println)

**4. Upstream:** The components in the stream closer to the source.

**5. Downstream:** The components in the stream closer to the sink.

**6. Backpressure:** Control mechanism ensuring that each stage processes data at its own pace without being overwhelmed.

**7. Materialized Values:** Values produced when running the stream.

**8. Buffering:** Storing intermediate elements to handle processing speed variations.

**9. Error Handling:** Strategies to manage and recover from errors.

**10. Asynchronous Boundaries:** Points where processing can be distributed across threads or actors.

**11. Graphs:** Complex topologies defined using GraphDSL.

### Example Flow
Imagine a stream processing pipeline where we read integers from 1 to 100, double each value, filter out even numbers, and print the results:

```scala
val source: Source[Int, NotUsed] = Source(1 to 100)

val flow1: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 2)

val flow2: Flow[Int, Int, NotUsed] = Flow[Int].filter(_ % 2 == 0)

val sink: Sink[Int, Future[Done]] = Sink.foreach(println)

source
  .via(flow1)  // Doubling each value
  .via(flow2)  // Filtering even numbers
  .to(sink)    // Printing the results
  .run()
```

```scala
          Source (1 to 100)
                 |
                 v
       +------------------+
       | Flow (Double     |
       | Each Value)      |
       +---------+--------+
                 |
                 v
       +------------------+
       | Flow (Filter     |
       | Even Numbers)    |
       +---------+--------+
                 |
                 v
               Sink (Print)

```

<h3>Alpakka: A Reactive Enterprise Integration Library</h3>
- Alpakka is a powerful and versatile library designed for building reactive and stream-oriented integration pipelines in Java and Scala. It's built on top of the robust Akka Streams framework, leveraging its strengths in handling asynchronous data flows and backpressure.  

```
// This program consumes messages from a Kafka topic named "my-topic" and logs them to the console.
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.duration._

object KafkaConsumerExample extends App {

  // Create an ActorSystem to manage the application's lifecycle
  implicit val system = ActorSystem("KafkaConsumerExample")

  // Configure Consumer Settings
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092") // Replace with your Kafka broker's address
    .withGroupId("my-consumer-group")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest") // Start consuming from the beginning of the topic

  // Create a Kafka Consumer Source
  val consumer = Consumer.plainSource(consumerSettings, Subscriptions.topics("my-topic"))
    .map(_.value()) // Extract the message value from the Kafka record
    .log("Received message") // Log the received message to the console
    .runWith(Sink.ignore) // Consume the message stream without further processing

  // Terminate the ActorSystem
  system.terminate()
}
```
