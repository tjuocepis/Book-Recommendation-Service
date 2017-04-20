package actor

import akka.actor.{Actor, ActorLogging, Props}
import cases._
import elasticsearch.{ElasticIndexRequestHandler, IndexBookData}
import weka.WekaActor

/**
  *   Receives book data for WekaActor to build Weka .arff files and also sends message to WekaActor to start
  *   calculating recommendations
  */
class DataReceiverActor extends Actor with ActorLogging {

  override def receive: Receive = {

    /**
      * Upon receiving this message sends the book data to WekaActor
      */
    case BuildArffFile(books: Seq[Book]) =>
      println(s"[actor.DataReceiverActor] - Received books data")
      val wekaActor = ActorSystemContainer.actorSystem().actorOf(Props(new WekaActor))
      wekaActor ! books

    /**
      * Receives book data and adds it to a custom container object that will be used when client requests to
      * get recommendations.  The commented out lines are for indexing the book data to elastic search (the book
      * data is already indexed)
      */
    case IndexBookData(books: Seq[Book]) =>
//      val elasticHandler = ActorSystemContainer.actorSystem().actorOf(Props(new ElasticIndexRequestHandler))
//      elasticHandler ! IndexBookData(books)
        BookDataContainer.setBooks(books)

    /**
      * Upon receiving this message it forwards a message to WekaActor to start calculating recommendations
      */
    case CreateRecommendations =>
      val wekaActor = ActorSystemContainer.actorSystem().actorOf(Props(new WekaActor))
      wekaActor ! CreateRecommendations
      println(s"[actor.DataReceiverActor] - Create Recommendations Message Sent")
  }
}
