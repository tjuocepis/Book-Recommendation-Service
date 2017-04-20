package mysql

import java.sql.{Connection, DriverManager}
import javax.net.ssl.SSLSocketFactory

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

/**
  * Instance that defines a method that loads a CSV file into a remote Google MySQL
  */
object LoadCsvToMySql {

  /**
    * Loads a CSV file into Google MySQL
    */
  def loadBooksCsvToMySql() = {

    // Create google credentials for the database
    val credential = GoogleCredential.getApplicationDefault
    val databaseName = "cs441_final"
    val instanceConnectionName = "cs441-hw3-148523:us-central1:book-data"

    // Create the database connection url
    val jdbcUrl = String.format(
      "jdbc:mysql://google/%s?cloudSqlInstance=%s&"
        + "socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false",
      databaseName,
      instanceConnectionName)

    // Make the query for creating a new table
    val createBooksTable = """CREATE TABLE IF NOT EXISTS books ( isbn VARCHAR(255), title VARCHAR(255), """ +
      """author VARCHAR(255), year INT, publisher VARCHAR(255), image_s VARCHAR(255), """ +
      """image_m VARCHAR(255), image_l VARCHAR(255), PRIMARY KEY (isbn) )"""

    // Make the query for loading CSV file to the created table
    val loadCsvToSql = """LOAD DATA LOCAL INFILE 'data/BX-Books-Tiny.csv' """ +
      """INTO TABLE books FIELDS TERMINATED BY ';' """ +
      """ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS """ +
      """(isbn, title, author, year, publisher, image_s, image_m, image_l)"""

    // Connection that will be made
    var connection: Option[Connection] = None
    val socketFactory = SSLSocketFactory.getDefault

    // Try to connect to the database, if succeeded execute the query for creating the books table
    try {
      connection = Some(DriverManager.getConnection(jdbcUrl, "root", "M@fuka$1"))
      val preparedStatement = connection.get.prepareStatement(createBooksTable)
      val success = preparedStatement.execute
    } catch {
      case e: Exception =>
        println("[Google MySQL] - Something With MySQL Went Wrong!")
        e.printStackTrace()
    }

    // Try to execute the query to load CSV file into the database
    try {
      val preparedStatement = connection.get.prepareStatement(loadCsvToSql)
      val success = preparedStatement.execute()
    } catch {
      case e: Exception =>
        println("[Google MySQL] - Loading Books CSV To MySQL Went Wrong!")
        e.printStackTrace()
    }
  }
}
