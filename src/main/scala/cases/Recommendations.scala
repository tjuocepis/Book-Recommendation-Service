package cases

import weka.RecModel

/**
  * Holds recommendation data for specifc user
  *
  * @param name Name of user
  * @param recs Book recommendations
  */
case class Recommendations(name: String, recs: Seq[String])
