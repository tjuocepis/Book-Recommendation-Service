package elasticsearch

import java.net.InetAddress

import actor.{ActorSystemContainer, DataReceiverActor}
import akka.actor.Props
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress

/**
  * Created by titusjuocepis on 12/12/16.
  */
object ElasticClient {

  private val settings = Settings.builder().put("cluster.name", "elasticsearch-titus").build()
  private val _client: TransportClient = TransportClient.builder().settings(settings).build()
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("104.197.88.210"), 9300))

  /**
    * Gets the Actor instance
    *
    * @return Actor instance
    */
  def client() = {
    _client
  }
}
