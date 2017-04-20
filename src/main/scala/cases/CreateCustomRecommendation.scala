package cases

/**
  * Actor message to start calculating recommendations for the client
  *
  * @param ratings Book ratings
  * @param filename What the .arff file will be named
  * @param nRecommendations How many recommendations did the client want
  */
case class CreateCustomRecommendation(ratings: Map[String, (String, Double)], filename: String, nRecommendations: Int)
