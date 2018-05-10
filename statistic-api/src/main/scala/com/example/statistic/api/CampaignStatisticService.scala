package com.example.statistic.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

import CampaignStatisticService._

trait CampaignStatisticService extends Service {
  
  def getCampaign(campaignId: String): ServiceCall[NotUsed, Campaign]
  
  def processStatistic(acctStatistic: NotUsed): ServiceCall[Source[NotUsed, NotUsed], Source[NotUsed, NotUsed]]
  
  def acctStatisticTopic: Topic[NotUsed]
  
  override final def descriptor: Descriptor = {
    import Service._
    import com.lightbend.lagom.scaladsl.api.transport.Method
    named("statistic")
      .withCalls(
        restCall(Method.GET, "/campaign/statistic/:id", getCampaign _)
      )
      .withTopics(
        topic("campaign", acctStatisticTopic)
      )
      .withAutoAcl(true)
  }
  
}
object CampaignStatisticService {
  
  case class Campaign(campaignId: String)
  
}
