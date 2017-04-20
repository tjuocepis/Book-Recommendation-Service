package stream

import actor.ActorSystemContainer
import akka.actor.Props
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, FlowShape}
import cases.Book
import flows.BookDataFlows
import subscribers.BookDataSubscriber

/**
  * Instance that defines a method to start streaming
  */
object DataStreamer {

  implicit val materializer = ActorSystemContainer.materializer()

  /**
    * Streams book data from a file
    *
    */
  def startDataStream() = {

    // Get the book data lines
    val bookDataLines = scala.io.Source.fromFile("data/BX-Books-Tiny.csv", "ISO-8859-1").getLines().drop(1)

    // Create the data streaming graph
    val graph = RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder =>

        // SOURCES
        val booksSource = builder.add(Source.fromIterator(() => bookDataLines)).out

        // FLOWS
        val stringToBookFlowShape: FlowShape[String, Book] =
          builder.add(BookDataFlows.csvToBookFlow)

          // SINKS
        val bookDataSink = builder.add(Sink.actorSubscriber(Props(new BookDataSubscriber))).in

        // GRAPH STRUCTURE
        booksSource ~> stringToBookFlowShape ~> bookDataSink

        // CLOSE
        ClosedShape

    }).run()
  }
}
