package weka

import actor.{ActorSystemContainer, DataReceiverActor}
import akka.actor.Props

/**
  * Singleton for WekaActor container to make sure only one of them is created
  */
object WekaActorContainer {

  private val system = ActorSystemContainer.actorSystem()
  private val materializer = ActorSystemContainer.materializer()
  private val _actor = system.actorOf(Props(new WekaActor))

  /**
    * Gets the Actor instance
    *
    * @return Actorinstance
    */
  def actor() = {
    _actor
  }
}
