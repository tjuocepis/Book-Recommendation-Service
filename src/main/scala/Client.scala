import java.lang.Integer
import java.net.InetAddress

import cases.{Book, CreateCustomRecommendation}
import elasticsearch.{ElasticClient, SearchBook}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import stream.DataStreamer
import weka.{RecModel, WekaActorContainer}

import scala.io.StdIn

/**
  * Client application that is used to enter book ratings and get recommendations.  User can search books
  * by name which queries Elastic Search in order to see if the book exists in the data set
  */
object Client {

  def main(args: Array[String]): Unit = {
    println("Client Started...")

    DataStreamer.startDataStream()

    val client = ElasticClient.client()

//    val settings = Settings.builder().put("cluster.name", "elasticsearch-titus").build()
//    val client: TransportClient = TransportClient.builder().settings(settings).build()
//      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("104.197.88.210"), 9300))

    var EXIT = false
    var ratings = Map.empty[String, (String, Double)]
    var name = ""

    println("\n\n======================[Welcome To Book Recommendation System]====================")
    println("Built by Edgaras Juocepis (2016)")
    println("\n*** Please make your window is full screen for full effect! ***\n")
    println("What is your name?")
    name = StdIn.readLine()

    val recs = checkElasticForRecommendations(client, name)
    if (recs.isDefined) {
      val r = recs.get
      for (i <- 0 until recs.size) {
        println(r.get(i))
      }
      println(s"Welcome back $name! Looks like we have some recommendations for you already!")
      println(s"Would you like to get new recommendations? (yes/no)")
      val option = StdIn.readLine()
      if (!option.contentEquals("yes")) {
        println("Okay then! Thank you for using my recommendation system!")
        sys.exit(0)
      }
    }

    println(s"\nNice to meet you $name!\n")

    while(!EXIT) {
      println("You have a few options here (For better results you should rate at least 10 books, but not necessary):")
      println("\t1) Search book by title or keyword in title")
      println("\t2) Search book by author")
      println("\t3) Give me recommendations")
      println("\t4) Exit\n")
      println("TIPS: - Single words work best if searching by keyword")
      println("      - The more books you rate the more unique are the recommendations")
      println("      - Recommendations work really well if rating books by same author")
      println("        I rated all the Harry Potter books and got recommended a lot of girly books :]\n")
      print("What's your option? ")

      var option = StdIn.readLine()

      var optionInt = com.google.common.primitives.Ints.tryParse(option)

      while (optionInt == null || optionInt.intValue() < 1 || optionInt.intValue() > 4) {
        println("Wrong input! Please input a number from 1 to 4")
        option = StdIn.readLine()
        optionInt = com.google.common.primitives.Ints.tryParse(option)
      }

      optionInt.intValue() match {
        case 1 =>
          println("\nWonderful! You chose to search for a book by title")
          println("What book are you looking to rate?\n")
          val title = StdIn.readLine()
          val books = SearchBook.search(title, "title", client)
          if (books.nonEmpty) {
            println(s"Sweet! We found ${books.size} books for you to choose from!")
            println("Please input a number to look at the book (1 for the first book)")
            var option = StdIn.readLine()
            var optionInt = com.google.common.primitives.Ints.tryParse(option)
            while (optionInt == null || optionInt.intValue() < 1 || optionInt.intValue() > books.size) {
              println(s"Wrong input! Please input a number from 1 to ${books.size}")
              option = StdIn.readLine()
              optionInt = com.google.common.primitives.Ints.tryParse(option)
            }
            println(s"You chose book number ${optionInt.intValue}:")
            val book = books(optionInt.intValue-1)
            println(book.toString + "\n")
            var ratingSubmitted = false
            while (!ratingSubmitted) {
              println("Please rate this book from 1 to 10")
              val rating = StdIn.readInt()
              rating match {
                case r if r > 0 && r <= 10 =>
                  ratingSubmitted = true
                  ratings = ratings + (book.isbn -> (book.isbn, r.toDouble))
                  println(s"Rating of $r was accepted!\n")
                case bad =>
                  println(s"Rating of $bad is not valid!\n")
              }
            }
          }
          else {
            println("\nSorry! Book does not exist in the data set")
            println("Would you like to try again? (yes/no)")
            val option = StdIn.readLine()
            option.toLowerCase match {
              case "yes" => // Continue
              case "no" => EXIT = true
            }
          }
        case 2 =>
          println("\nWonderful! You chose to search for a book by author")
          println("What's the author you're looking for?\n")
          val title = StdIn.readLine()
          val books = SearchBook.search(title, "author", client)
          if (books.nonEmpty) {
            println(s"Sweet! We found ${books.size} books for you to choose from!")
            println("Please input a number to look at the book (1 for the first book)")
            var option = StdIn.readLine()
            var optionInt = com.google.common.primitives.Ints.tryParse(option)
            while (optionInt == null || optionInt.intValue() < 1 || optionInt.intValue() > books.size) {
              println(s"Wrong input! Please input a number from 1 to ${books.size}")
              option = StdIn.readLine()
              optionInt = com.google.common.primitives.Ints.tryParse(option)
            }
            println(s"You chose book number ${optionInt.intValue}:")
            val book = books(optionInt.intValue())
            println(book.toString + "\n")
            var ratingSubmitted = false
            while (!ratingSubmitted) {
              println("Please rate this book from 1 to 10")
              val rating = StdIn.readInt()
              rating match {
                case r if r > 0 && r <= 10 =>
                  ratingSubmitted = true
                  ratings = ratings + (book.isbn -> (book.isbn, r.toDouble))
                  println(s"Rating of $r was accepted!\n")
                case bad =>
                  println(s"Rating of $bad is not valid!\n")
              }
            }
          }
        case 3 =>
          println("How many recommendations would you like? (You can choose up to 25)")
          val n = StdIn.readInt()
          println("Please wait while we get your recommendations!")
          WekaActorContainer.actor() ! CreateCustomRecommendation(ratings, name, n)
        case 4 =>
          println("Thank you for using my book recommendation system!")
          EXIT = true
      }
    }
    sys.exit(0)
  }

  def checkElasticForRecommendations(client: TransportClient, name: String): Option[java.util.List[Object]] = {

    // Prepare the search and set the fields that we want to retrieve
    val response = client.prepareSearch()
      .setIndices("books")
      .setTypes("recs")
      .addFields("name", "recs")
      .setQuery(QueryBuilders.matchPhraseQuery("name", name)).setSize(1).execute().actionGet()

    var recs: Option[java.util.List[Object]] = None

    // Filter through the result and create a Book instance with the received meta data
    if (response.getHits.totalHits() > 0) {
      val hits = response.getHits.getHits
      hits foreach { hit =>
        val fields = hit.fields()
        val _recs = fields.get("recs").getValues
        _recs match {
          case list: java.util.List[Object] =>
            recs = Some(list)
        }
      }
    }
    recs
  }
}
