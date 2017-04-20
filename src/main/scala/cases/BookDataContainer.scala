package cases

import actor.{ActorSystemContainer, DataReceiverActor}
import akka.actor.Props

/**
  * Created by titusjuocepis on 12/11/16.
  */
object BookDataContainer {

  private var books = Seq.empty[Book]

  /**
    * Gets the ActorSystem instance
    *
    * @return ActorSystem instance
    */
  def getBooks = {
    books
  }

  def setBooks(books: Seq[Book]) = {
    this.books = books
  }
}
