package part1_recap

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object AkkaStreamsRecap extends App {

  implicit val system: ActorSystem = ActorSystem("AkkaStreamsRecap")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val source = Source(1 to 100)
  val sink = Sink.foreach[Int](println)
  val flow = Flow[Int].map(_ + 1)

  val runnableGraph = source.via(flow).to(sink)
//  val simpleMaterializedValue = runnableGraph.run() // materialization

  // MATERIALIZED VALUE
  val sumSink = Sink.fold[Int, Int](0)((currentSum, element) => currentSum + element)
//  val sumFuture = source.runWith(sumSink)

  import system.dispatcher
  /*sumFuture.onComplete {
    case Success(value) => println(s"The sum of all the numbers from the simple source is $value")
    case Failure(e) => println(s"Summing all the numbers from the simple Source failed because of $e")
  }*/

//  val anotherMaterializedValue = source.viaMat(flow)(Keep.right).toMat(sink)(Keep.left).run()

  /*
    1 - materializing a graph means materializing all the components
    2 - a materialized value can be ANYTHING AT ALL
   */

  /*
   backpressure - slow down producers if consumers are slow and ask for nothing to consume
   backpressure actions:
      - buffer elements
      - apply a strategy
      - fail the entire stream
   */

  val bufferedFlow = Flow[Int].buffer(10, OverflowStrategy.dropHead)
  source.async
    .via(bufferedFlow).async
    .runForeach { e =>
      Thread.sleep(100)
      println(e)
    }
}
