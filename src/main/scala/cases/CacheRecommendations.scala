package cases

import org.elasticsearch.client.transport.TransportClient
import weka.RecModel

/**
  * Actor messasge to tell ElasticIndexHandler to cache recommendations for specific user
  *
  * @param client Elastic client
  * @param recs Book recommendations
  */
case class CacheRecommendations(client: TransportClient, recs: Recommendations)
