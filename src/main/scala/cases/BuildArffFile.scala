package cases

/**
  * Actor message to tell to build and .arff file
  * @param books Books to build .arff file from
  */
case class BuildArffFile(books: Seq[Book])
