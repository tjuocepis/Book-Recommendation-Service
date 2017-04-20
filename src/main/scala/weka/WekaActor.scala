package weka

import java.io.{File, IOException}

import actor.{ActorSystemContainer, RecommendationActor}
import akka.actor.{Actor, ActorLogging, Props}
import cases._
import elasticsearch.ElasticClient
import weka.core.converters.{ArffSaver, ConverterUtils}
import weka.core.neighboursearch.LinearNNSearch
import weka.core.{Attribute, FastVector, Instance, Instances}

import scala.collection.mutable
import scala.concurrent.Await

/**
  * Acotr that does all the nitty gritty Weka stuff in order to build .arff files
  * and calculate recommendations.
  */
class WekaActor extends Actor with ActorLogging {

  override def receive: Receive = {

    // When receving this message, it builds two .arff files. One for all the books and the other one
    // is for the pre defined user that will have his recommendations calculated
    case books: Seq[Book] =>
      val bookSource = createArffFile(books, "newBook")
      val userSource = createArffFile(books, "newUser")

    // When receiving this message it will create an .arff file based on the name that the client
    // provided and will calculate the recommendations.  Then it will send them to Recommendation
    // actor to output them to the client
    case CreateCustomRecommendation(ratings, filename, nRecommendations) =>
      val source = createCustomArffFile(BookDataContainer.getBooks, filename, ratings)
      val recommendations = getRecommendations(nRecommendations, "newBookRatings", source)
      val recActor = ActorSystemContainer.actorSystem().actorOf(Props(new RecommendationActor))
      recActor ! SendRecommendations(recommendations, filename)

    // Will calculate book recommendations for the pre defined user. This message is received only when Main is run
    case CreateRecommendations(source: String) =>
      println(s"[WekaActor] - Creating Recommendations...")
      val recommendations = getRecommendations(5, "newBookRatings", source)
      val recActor = ActorSystemContainer.actorSystem().actorOf(Props(new RecommendationActor))
      recActor ! SendRecommendations(recommendations, "default")
      println(s"[WekaActor] - Recommendations Sent")
  }

  /**
    * Creates an .arff file from the book data
    *
    * @param books Book data
    * @param name File name
    * @return File name
    */
  def createArffFile(books: Seq[Book], name: String): String = {

    // Define a Vector that will hold book attributes which is designed to process data faster
    val attributes = new FastVector

    // For each book we add it to the attributes Vector
    books foreach { rating =>
      attributes.addElement(new Attribute(rating.isbn))
    }

    // We create the data set based on the attributes
    val data = new Instances(s"$name Ratings Data", attributes, attributes.size())
    var valueArray = new Array[Double](data.numAttributes())

    // Based on the name that we received we append different rating values to the data set
    name match {
      case "newBook" =>
        books foreach { book =>
          valueArray = new Array[Double](data.numAttributes())
          for (i <- valueArray.indices) {
            valueArray(i) = CustomRandom.generateRandom(10)
          }
          data.add(new Instance(1.0, valueArray))
        }
      case "newUser" =>
        valueArray = new Array[Double](data.numAttributes())
        for (i <- valueArray.indices) {
          valueArray(i) = CustomRandom.generateRandom(10)

        }
        data.add(new Instance(1.0, valueArray))
    }

    // Create and return the file name and call method to save the .arff file into ./data/
    val filename = name+"Ratings"
    saveDataset(data, false, name+"Ratings")
    filename
  }

  /**
    * Creates an .arff file for the client based on his provided ratings
    *
    * @param books Book data
    * @param name File name
    * @param ratings Client's ratings
    * @return File name
    */
  def createCustomArffFile(books: Seq[Book], name: String, ratings: Map[String, (String, Double)]): String = {

    // Create a Vector to hold attributes
    val attributes = new FastVector

    // For each book we add the isbn to the Vector
    books foreach { rating =>
      attributes.addElement(new Attribute(rating.isbn))
    }

    // Create the data set based on attributes
    val data = new Instances(s"$name Ratings Data", attributes, attributes.size())
    val valueArray = new Array[Double](data.numAttributes())
    val random = scala.util.Random

    // For the number of attributes we iterate and populate the ratings data
    for (i <- valueArray.indices) {
      val attrName = data.attribute(i).name
      // If the user has a rating provided we add it
      if (ratings.contains(attrName)) {
        valueArray(i) = ratings(attrName)._2
      }
      // Otherwise we add a zero
      else
        valueArray(i) = 0//random.nextInt(10)
    }
    data.add(new Instance(1.0, valueArray))
    val filename = name+"Ratings"
    saveDataset(data, false, name+"Ratings")
    filename
  }

  /**
    * Saves the .arff file into ./data/
    *
    * @param dataset The data set from which to save the file
    * @param batchSave If we want to batch save or do it iteratively
    * @param name Name of file
    * @throws IOException Input Output exception if we can parse the dataset
    */
  @throws[IOException]
  def saveDataset(dataset: Instances, batchSave: Boolean, name: String) {

    val saver: ArffSaver = new ArffSaver()

    if (batchSave) {
      saver.setInstances(dataset)
      val file = new File(s"./data/$name.arff")
      file.createNewFile()
      saver.setFile(file)
      saver.writeBatch()
    }
    else {
      saver.setRetrieval(2)
      saver.setInstances(dataset)
      val file = new File(s"./data/$name.arff")
      file.createNewFile()
      saver.setFile(file)
      var i: Int = 0
      while (i < dataset.numInstances) {
        saver.writeIncremental(dataset.instance(i))
        i += 1; i - 1
      }
      saver.writeIncremental(null)
    }
    println(s"Finished Creating Weka $name.arff File...")
  }

  /**
    * Calculates the recommendations based on linear neighbors search
    *
    * @param nRecommendations How many recommendations to return
    * @param bookSource Book data .arff file
    * @param userSource User .arff file that we need to do recommendations for
    * @return A Sequence of Recommendation Models
    */
  def getRecommendations(nRecommendations: Int, bookSource: String, userSource: String): Seq[RecModel] = {

    println("Starting to Calculate Recommendations...\n(This may take 30-60 seconds. Thank you for your patience!)")

    // Get the data from .arff files into Weka data set format
    val allRatingsSource = new ConverterUtils.DataSource(s"data/$bookSource.arff")
    val userRatingsSource = new ConverterUtils.DataSource(s"data/$userSource.arff")
    val bookRatingsDataSet = allRatingsSource.getDataSet
    val userRatingDataSet = userRatingsSource.getDataSet
    val userRatingInstance = userRatingDataSet.firstInstance

    // Do a linear search on the book data
    val kNN = new LinearNNSearch(bookRatingsDataSet)
    var userNeighbors: Option[Instances] = None
    var distancesToNeighbors: Option[Array[Double]] = None

    // Try to find the 10 nearest neighbors and calculate the distances
    // The closer the distance the more similar the user's ratings are
    try {
      userNeighbors = Some(kNN.kNearestNeighbours(userRatingInstance, 10))
      distancesToNeighbors = Some(kNN.getDistances)
    } catch {
      case e: Exception =>
        println("No neighbors could be found for the user " + e.printStackTrace())
    }

    val similarities = new Array[Double](distancesToNeighbors.get.length)

    // For all the distances we create a ration of similarities
    for (i <- distancesToNeighbors.get.indices) {
      similarities(i) = 1.0 / distancesToNeighbors.get(i)
    }

    // Initialize collection for the recommendations
    var recommendations = mutable.Map.empty[String, Seq[Int]]
    val neighborIterator = userNeighbors.get.enumerateInstances

    // For all the neighbors we found
    while (neighborIterator.hasMoreElements) {
      val neighbor = neighborIterator.nextElement.asInstanceOf[Instance]
      val bookISBN = neighbor.enumerateAttributes
      var index = -1
      // Loop through all the book ISBN numbers
      while (bookISBN.hasMoreElements) {
        index += 1
        val _bookISBN = bookISBN.nextElement.asInstanceOf[Attribute]
        // Get the value of the rating from book data
        userRatingInstance.value(index) < 1 match {
          case true =>
            val bookISBNString = userRatingInstance.attribute(index).name
            var ratings = Seq.empty[Int]
            // Check to see if we have the book ISBN in our recommendation collection
            recommendations.contains(bookISBNString) match {
              case true => ratings = recommendations(bookISBNString)
              case false => // Do Nothing
            }
            // Collect ratings and recommendation data
            ratings = neighbor.value(index).toInt +: ratings
            recommendations = recommendations + (bookISBNString -> ratings)
          case false => // Do Nothing
        }
      }
    }

    var recommendationRankings = Seq.empty[RecModel]
    val recommendationIterator = recommendations.keySet.iterator

    // Loop through all the recommendations
    while(recommendationIterator.hasNext) {
      val bookISBN = recommendationIterator.next()
      var similarity = 0.0
      var similarityWeight = 0.0
      val rec = recommendations(bookISBN)

      // For all the recommendations we calculate weights to determine the similarity of the user's ratings
      for (i <- rec.indices) {
        val rating = rec.apply(i)
        similarity += similarities(i)
        similarityWeight += similarities(i) * rating
      }

      // Create the recommendation model and input the accuracy score and isbn number
      val recommendationModel = new RecModel
      recommendationModel.arffAttributeName = bookISBN
      recommendationModel.calculatedScore = similarityWeight / similarity
      recommendationRankings = recommendationModel +: recommendationRankings
    }

    // Sort the recommendation ranks so that we have from best to worst
    recommendationRankings = recommendationRankings.sorted

    var recs = Seq.empty[RecModel]

    // Collect all the recommendations in to a list
    for (i <- 0 until nRecommendations) {
      recs = recs :+ recommendationRankings(i)
    }

    println("Done Calculating Recommendations, Grabbing Book Meta Data From GoogleSQL")

    recs
  }
}