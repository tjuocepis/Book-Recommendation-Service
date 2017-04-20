package weka

/**
  * Instance that generates random numbers from 0 to 10
  */
object CustomRandom {

  private val random = new scala.util.Random(System.currentTimeMillis())
  private var lastRandomNumber = 0

  /**
    * Generates a random number and makes sure that it does not repeat
    *
    * @return Random number
    */
  def generateRandom(i: Int): Int = {
    var randomNumber = 0
    do {
      randomNumber = random.nextInt(i)
    } while (randomNumber == lastRandomNumber)
    lastRandomNumber = randomNumber
    randomNumber
  }
}
