package Playground

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}

import scala.collection.mutable

object SimpleCacheManager {
  implicit val system: ActorSystem = ActorSystem("SimpleCacheActor")
  implicit val materializer: Materializer = Materializer(system)

  object CacheManager {
    private val cache: mutable.Map[Int, Int] = mutable.Map()

    def addToCache(key: Int, value: Int): Unit = {
      cache.put(key, value)
      println(s"Added to Cache: $key -> $value")
    }

    def getCache: Map[Int, Int] = cache.toMap
  }

  def processNumber(num: Int): Int = num * 2

  val numberSource = Source(1 to 5)
  val processFlow = Flow[Int].map { num =>
    val processed = processNumber(num)
    CacheManager.addToCache(num, processed)
    processed
  }
  val resultSink = Sink.foreach[Int](result => println(s"Processed Result: $result"))

  val graph = RunnableGraph.fromGraph {
    numberSource.via(processFlow).to(resultSink)
  }

  def run(): Unit = {
    graph.run()
    println("Cache contents are processed: " + CacheManager.getCache)
  }

  def main(args: Array[String]): Unit = {
    SimpleCacheManager.run()
  }
}

/**
 * Cache contents are processed: Map()
 * Added to Cache: 1 -> 2
 * Processed Result: 2
 * Added to Cache: 2 -> 4
 * Processed Result: 4
 * Added to Cache: 3 -> 6
 * Processed Result: 6
 * Added to Cache: 4 -> 8
 * Processed Result: 8
 * Added to Cache: 5 -> 10
 * Processed Result: 10
 */