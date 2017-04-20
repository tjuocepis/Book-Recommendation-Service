package cases

import weka.RecModel

/**
  * Actor message for RecommendationsActor to start querying MySQL and print recommendations to the console
  *
  * @param recs Recommendations models
  */
case class SendRecommendations(recs: Seq[RecModel], name: String)
