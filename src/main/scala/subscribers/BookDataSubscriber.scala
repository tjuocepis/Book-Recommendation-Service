package subscribers

import java.io.{File, IOException}

import actor.DataReceiverContainer
import akka.actor.ActorLogging
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}
import cases.{Book, BuildArffFile, CreateRecommendations}
import elasticsearch.IndexBookData
import weka.core.converters.ArffSaver
import weka.core.{Attribute, FastVector, Instance, Instances}

/**
  * Subscriber that consumes incoming book data and packages it up and upon completion sends it to DataReceiverActor
  */
class BookDataSubscriber extends ActorSubscriber with ActorLogging {

  // Set strategy to get book data one by one
  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  var books = Seq.empty[Book]

  override def receive: Receive = {

    // Append to head of Sequence as book data arrives
    case OnNext(book: Book) =>
      books = book +: books

    case OnError =>
      println("Error receiving UserBookRating")

    // When all book data arrives send it of to DataReceiverActor.
    // The commented out lines are not needed because they are executed when Main is run
    // and that's for building .arff files and indexing to elastic serach which is done already
    case OnComplete =>
      println("[subscribers.BookDataSubscriber] - Done receiving all books!")
//      DataReceiverContainer.actor() ! BuildArffFile(books)
      DataReceiverContainer.actor() ! IndexBookData(books)
//      DataReceiverContainer.actor() ! CreateRecommendations
      println(s"[subscribers.BookDataSubscriber] - Sent books to actor.DataReceiverActor")
  }
}