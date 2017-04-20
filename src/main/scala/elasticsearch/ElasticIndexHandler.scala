package elasticsearch

import java.net.InetAddress

import akka.actor.{Actor, ActorLogging}
import cases.{Book, CacheRecommendations}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._

/**
  * Actor that handles the request to index book data into elastic search (Elastic search is already
  * indexed with book data)
  */
class ElasticIndexRequestHandler extends Actor with ActorLogging {

  implicit val formats = DefaultFormats

  override def receive: Receive = {

    /**
      * Builds the elastic search index with book data
      */
    case IndexBookData(books: Seq[Book]) =>

      // Connect to Elastic Search
      val client = ElasticClient.client()
//      val settings = Settings.builder().put("cluster.name", "elasticsearch-titus").build()
//      val client = TransportClient.builder().settings(settings).build()
//        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("104.197.88.210"), 9300))

      // Tell elastic search that we will be data inserting in bulk
      val bulkRequest = client.prepareBulk()

      println("[ElasticIndexRequestHandler] - Preparing Bulk Request For Books")

      // For each book convert it into JSON format and add it to the bulk request
      books foreach { book =>
        val fileJson = write(book)
        bulkRequest.add(client.prepareIndex("books", "book-data").setSource(fileJson))
      }

      println("[ElasticIndexRequestHandler] - Finished Preparing Bulk Request For Books")

      // Submit the bulk request
      val bulkResponse = bulkRequest.get()

      // Check to see if succeeded
      if (bulkResponse.hasFailures)
        println("[ElasticIndexRequestHandler] - Error Adding Books")
      else
        println("[ElasticIndexRequestHandler] - Finished Adding Books")


    case CacheRecommendations(client, recs) =>

      val bulkRequest = client.prepareBulk()

      val json = write(recs)

      bulkRequest.add(client.prepareIndex("books", "recs").setSource(json))

      val bulkResponse = bulkRequest.get()

      // Check to see if succeeded
      if (bulkResponse.hasFailures)
        println("[ElasticIndexRequestHandler] - Error Adding Books")
      else
        println("[ElasticIndexRequestHandler] - Finished Adding Books")
  }
}

// Actor message to start indexing book data to Elastic Search
case class IndexBookData(fileData: Seq[Book])
