package actor

import akka.actor.Props

/**
  * Singleton for DataReceiverActor to make sure there is only one created
  */
object DataReceiverContainer {

  private val system = ActorSystemContainer.actorSystem()
  private val materializer = ActorSystemContainer.materializer()
  private val _actor = system.actorOf(Props(new DataReceiverActor))

  /**
    * Gets the Actor instance
    *
    * @return Actor instance
    */
  def actor() = {
    _actor
  }
}
