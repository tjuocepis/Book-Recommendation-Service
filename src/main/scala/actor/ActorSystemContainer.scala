package actor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
  * Singleton instance that holds ActorySystem and Materializer
  */
object ActorSystemContainer {

  private val _actorSystem = ActorSystem.create("data-streaming")
  private val _materializer = ActorMaterializer.create(_actorSystem)

  /**
    * Gets the ActorSystem instance
    *
    * @return ActorSystem instance
    */
  def actorSystem() = {
    _actorSystem
  }

  /**
    * Gets the Materializer instance
    *
    * @return Materializer instance
    */
  def materializer() = {
    _materializer
  }
}
