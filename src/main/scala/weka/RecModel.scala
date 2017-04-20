package weka

/**
  * Model for a recommendation
  */
class RecModel extends Comparable[RecModel] {

  // The calculated accuracy score and an attribute which is the book ISBN number
  var calculatedScore: Double = .0
  var arffAttributeName: String = ""

  /**
    * Compare function in order to sort a list of recommendations
    *
    * @param model Recommendation model to compare with
    * @return -1 if less, 0 if equal, 1 if greater
    */
  def compareTo(model: RecModel): Int = {
    if (this.calculatedScore > model.calculatedScore)
      return -1
    if (this.calculatedScore < model.calculatedScore)
      return 1
    0
  }

  /**
    * Overrides the toString method to do build a custom string
    *
    * @return Recommendation model string
    */
  override def toString: String = arffAttributeName + ": " + calculatedScore
}