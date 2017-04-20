import java.sql.{Connection, DriverManager, PreparedStatement, SQLException}
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

import akka.actor.Props
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source, Zip}
import akka.stream.{ClosedShape, FlowShape}
import cases.Book
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import flows.BookDataFlows
import stream.DataStreamer
import subscribers.BookDataSubscriber
import weka.core.converters.CSVLoader
import actor.ActorSystemContainer

/**
  * This main doesn't need to be run. It is used to create the base .arff files that we will check recommendations
  * against and is also used to populate MySQL and Elastic Search
  */
object Main {

  implicit val system = ActorSystemContainer.actorSystem()
  implicit val materializer = ActorSystemContainer.materializer()

  def main(args: Array[String]): Unit = {
    println("Main Started...")
//    DataStreamer.startDataStream()
  }
}
