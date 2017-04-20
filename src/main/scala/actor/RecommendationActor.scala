package actor

import java.sql.{Connection, DriverManager, ResultSet}
import javax.net.ssl.SSLSocketFactory

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import cases._
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import elasticsearch.{ElasticClient, ElasticIndexRequestHandler}
import weka.RecModel

/**
  * This actor prints out the results of the recommendations.  Also it queries GoogleMySQL in order to retrieve
  * book meta data by looking up book ISBN numbers from the recommendations
  */
class RecommendationActor extends Actor with ActorLogging {

  override def receive: Receive = {

    /**
      * Upon receiving this message this actor starts to build a MySQL query based on book ISBN numbers and prints
      * out results out to the console
      */
    case SendRecommendations(recs, name) =>

//      println(s"[RecommendationActor] - Recommendations Received")

      // Create google credentials
      val credential = GoogleCredential.getApplicationDefault
      val databaseName = "cs441_final"
      val instanceConnectionName = "cs441-hw3-148523:us-central1:book-data"

      // Create connection url
      val jdbcUrl = String.format(
        "jdbc:mysql://google/%s?cloudSqlInstance=%s&"
          + "socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false",
        databaseName,
        instanceConnectionName)

      // Create the beginning of MySQL query
      var sqlQuery = "SELECT * FROM books WHERE isbn IN ("

      // Iterate through the recommendations
      val iterator = recs.iterator
      while (iterator.hasNext) {
        val rec = iterator.next
        val isbn = rec.arffAttributeName
        val score = rec.calculatedScore
        iterator.hasNext match {
          case true => sqlQuery = sqlQuery.concat(s"'$isbn', ") // keep appending to the query based on isbn numbers
          case false => sqlQuery = sqlQuery.concat(s"'$isbn')") // when last isbn, close the query parenthesis
        }
      }

      var connection: Option[Connection] = None // Connection to database will eventually be made
      val socketFactory = SSLSocketFactory.getDefault
      var result: Option[ResultSet] = None // Result from database will eventually arrive

      println(s"[WekaActor] - Querying MySql")

      // Try connecting to the database and if success then send the query
      try {
        connection = Some(DriverManager.getConnection(jdbcUrl, "root", "M@fuka$1"))
        val preparedStatement = connection.get.prepareStatement(sqlQuery)
        result = Some(preparedStatement.executeQuery)
      } catch {
        case e: Exception =>
          println("[Google MySQL] - Something With MySQL Went Wrong!")
          e.printStackTrace()
      }

      var recommendations: Seq[String] = Seq.empty[String]
      var responseString = "======================================================\n"

      // If we got a result back loop through it and get the values received and keep concatenating
      // recommendations string
      result.isDefined match {
        case true =>
          val res = result.get
          var index = 0
          while(result.get.next()) {
            val isbn = res.getString("isbn")
            val title = res.getString("title")
            val author = res.getString("author")
            val year = res.getInt("year")
            val publisher = res.getString("publisher")
            val image = res.getString("image_l")
            responseString = responseString.concat(s"${recs(index).calculatedScore} =>\n" +
              s"$title\n" +
              s"By: $author\n" +
              s"$year\n" +
              s"Publisher: $publisher\n" +
              s"ISBN: $isbn\n" +
              s"Image: $image\n" +
              s"======================================================\n")
            index += 1
            recommendations = recommendations :+ responseString
          }
          val elastic = ActorSystemContainer.actorSystem().actorOf(Props(new ElasticIndexRequestHandler))
          elastic ! CacheRecommendations(ElasticClient.client(), Recommendations(name, recommendations))
      }

      // Print the recommendations with book meta data to the console
      println(responseString)
      println("Thank you for using my book recommendation system!\nHave a pleasant day!")
  }
}
