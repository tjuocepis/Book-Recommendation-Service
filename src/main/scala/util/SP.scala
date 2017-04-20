package util

/**
  * Utility tool to process strings
  */
object SP {

  /**
    * Removes quotes from the ends of the string e.g. "string" = string
    *
    * @param s string to process
    * @return processed string
    */
  def ridQuotes(s: String): String = {
    s.substring(1, s.length-1)
  }
}