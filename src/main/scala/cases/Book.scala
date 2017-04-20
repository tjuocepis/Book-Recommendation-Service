package cases

/**
  * Case class that holds book data
  *
  * @param isbn Isbn number of book
  * @param title Title
  * @param author Author
  * @param year Year published
  * @param publisher Published by who
  */
case class Book(isbn: String, title: String, author: String, year: String, publisher: String) {

  /**
    * Overrides toString method for printing user book rating
    *
    * @return
    */
  override def toString: String = s"Book: '$title', Author: $author, Year: $year, ISBN: $isbn, Publisher: $publisher"
}
