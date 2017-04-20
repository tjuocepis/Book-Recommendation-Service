package flows

import actor.ActorSystemContainer
import akka.stream.scaladsl.Flow
import akka.stream.{ActorAttributes, Supervision}
import cases.Book
import util.SP

/**
  * Instance that defines the Flows necessary to process incoming Book data
  */
object BookDataFlows {

  implicit val system = ActorSystemContainer.actorSystem()

  /**
    * Takes in single book rating strings and creates a UserBookRating instance
    * The passed in array contains book rating info that came from splitting book rating data string
    *
    * @param cols Array containing single rating info
    * @return Single UserBookRating instance
    */
  def csvLinesArrayToBook(cols: Array[String]) = {
    Book(SP.ridQuotes(cols(0)), SP.ridQuotes(cols(1)), SP.ridQuotes(cols(2)), SP.ridQuotes(cols(3)), SP.ridQuotes(cols(4)))
  }

  /**
    * Splits the incoming ratings strings and maps the results to a function that converts the data
    * to UserBookRating instances
    *
    * @return UserBookRating instances
    */
  def csvToBookFlow = Flow[String].map(_.split(";").map(_.trim)).map(csvLinesArrayToBook)
    .withAttributes(ActorAttributes.supervisionStrategy {
      e: Throwable =>
        system.log.error("Error parsing row event: {}", e)
        Supervision.Resume // skips erroneous data and resumes the stream
    })
}
