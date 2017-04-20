package weka

import java.io.{BufferedReader, File, FileReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

/**
  * Instance that I used to cut down the size of files because my implementation of Weka could not handle
  * really large files.  Originally I was working with files around 500,000 data entries. I cut them down
  * to about 10,000
  */
object PartOfFile {

  def main(args: Array[String]): Unit = {

    val bookReader = new BufferedReader(new FileReader("data/BX-Books.csv"))
    val bookFile = new File("data/BX-Books-New.csv").createNewFile()
    val bookPath = Paths.get("data/BX-Books-New.csv")
    var bookCounter = 0

    var line = bookReader.readLine() + "\n"

    println("READING BOOKS FILE")
    line = bookReader.readLine() + "\n"
    bookCounter += 1
    Files.write(bookPath,
      line.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.APPEND)
    while (bookReader != null && bookCounter < 5000) {
      bookCounter += 1
      val line = bookReader.readLine() + "\n"
      Files.write(bookPath,
        line.getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.APPEND)
    }

    println("DONE!")

    bookReader.close()
  }
}
