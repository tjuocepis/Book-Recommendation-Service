package cases

/**
  * Actor message to start calculating recommendations for a pre defined user (Only when Main is run and not Client)
  * @param source .arff file from which to base the recommendations on
  */
case class CreateRecommendations(source: String)
