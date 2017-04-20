package elasticsearch

import java.net.InetAddress

import akka.actor.Props
import cases.Book
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHitField
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._

/**
  * Instance that defines a method to query Elastic Search
  */
object SearchBook {

  implicit val formats = DefaultFormats

  /**
    * Searches Elastic Search based on the keyword provided and also the location to search in and
    * returns a single book that matched the criteria
    *
    * @param keyword Keyword to query by
    * @param searchBy Where in the document of book data to look at
    * @param client Elastic Search client
    * @return Returns the book that was found
    */
  def search(keyword: String, searchBy: String, client: TransportClient): Seq[Book] = {

    // Prepare the search and set the fields that we want to retrieve
    val response = client.prepareSearch()
      .setIndices("books")
      .setTypes("book-data")
      .addFields("isbn", "title", "author", "year", "publisher")
      .setQuery(QueryBuilders.matchPhraseQuery(searchBy, keyword)).setSize(1000).execute().actionGet()

    var books: Seq[Book] = Seq.empty

    // Filter through the result and create a Book instance with the received meta data
    if (response.getHits.totalHits() > 0) {
      val hits = response.getHits.getHits
      hits foreach { hit =>
        val fields = hit.fields()
        val isbn = fields.get("isbn").value[String]
        val title = fields.get("title").value[String]
        val author = fields.get("author").value[String]
        val year = fields.get("year").value[String]
        val publisher = fields.get("publisher").value[String]
        val tempBook = Book(isbn, title, author, year, publisher)
        books = tempBook +: books
      }
    }

    books
  }
}
