package part4streams.docs

import akka.stream.ClosedShape

object Example extends App {

  import akka.NotUsed
  import akka.actor.ActorSystem
  import akka.stream.scaladsl._

  final case class Author(handle: String)

  final case class Hashtag(name: String)

  final case class Tweet(author: Author, timestamp: Long, body: String) {
    def hashtags: Set[Hashtag] =
      body
        .split(" ")
        .collect {
          case t if t.startsWith("#") => Hashtag(t.replaceAll("[^#\\w]", ""))
        }
        .toSet
  }

  val akkaTag = Hashtag("#akka")

  val tweets: Source[Tweet, NotUsed] = Source(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
      Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
      Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
      Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
      Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
      Nil)

  implicit val system: ActorSystem = ActorSystem("reactive-tweets")

  tweets
    .filterNot(_.hashtags.contains(akkaTag)) // Remove all tweets containing #akka hashtag
    .map(_.hashtags) // Get all sets of hashtags ...
    .reduce(_ ++ _) // ... and reduce them to a single set, removing duplicates across all tweets
    .mapConcat(identity) // Flatten the set of hashtags to a stream of hashtags
    .map(_.name.toUpperCase) // Convert all hashtags to upper case
    .runWith(Sink.foreach(println)) // Attach the Flow to a Sink that will finally print the hashtags

  /**
   * Now let’s say we want to persist all hashtags, as well as all author names from this one live stream.
   * For example we’d like to write all author handles into one file, and all hashtags into another file
   * on disk. This means we have to split the source stream into two streams which will handle the writing
   * to these different files.
   * */
  // Implement the sinks
  val writeAuthors: Sink[Author, NotUsed] = Sink.foreach[Author](author => println(s"Author: ${author.handle}")).mapMaterializedValue(_ => NotUsed)
  val writeHashtags: Sink[Hashtag, NotUsed] = Sink.foreach[Hashtag](hashtag => println(s"Hashtag: ${hashtag.name}")).mapMaterializedValue(_ => NotUsed)
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val bcast = b.add(Broadcast[Tweet](2))
    tweets ~> bcast.in
    bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
    bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
    ClosedShape
  })
  g.run()

  system.terminate()
}

/**
 * NOTE: What is ClosedShape
 * If you have a source feeding into a flow and then into a sink, and all are connected properly, it forms a ClosedShape—meaning the graph is complete and can be run. Without a ClosedShape, the graph cannot be materialized.
 */

/**
 * OUTPUT:
 * 07:26:05.773 [reactive-tweets-akka.actor.default-dispatcher-5] INFO akka.event.slf4j.Slf4jLogger - Slf4jLogger started
 * #BANANAS
 * #APPLES
 * #ORANGES
 */

/**
 * OUTPUT: (for GraphDSL)
 * Author: rolandkuhn
 * Hashtag: #akka
 * Author: patriknw
 * Hashtag: #akka
 * Author: bantonsson
 * Hashtag: #akka
 * Author: drewhk
 * Hashtag: #akka
 * Author: ktosopl
 * Hashtag: #akka
 * Author: mmartynas
 * Hashtag: #akka
 * Author: akkateam
 * Hashtag: #akka
 * Author: bananaman
 * Hashtag: #bananas
 * Author: appleman
 * Hashtag: #apples
 * Author: drama
 * Hashtag: #apples
 * Hashtag: #oranges
 */