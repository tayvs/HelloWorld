package com.example.statistic.api

import akka.{Done, NotUsed}
import com.example.statistic.api.model.StatisticProdDetails
import com.example.statistic.topic.LocalDeliveryStatus
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

trait CampaignStatisticService extends Service {
  
  def getCampaign(campaignId: String): ServiceCall[NotUsed, StatisticProdDetails]
  
  def startCampaign(campaignId: String): ServiceCall[NotUsed, StatisticProdDetails]
  
  def processStatistic: ServiceCall[LocalDeliveryStatus, Done]
  
  override final def descriptor: Descriptor = {
    import Service._
    import com.lightbend.lagom.scaladsl.api.transport.Method
    named("statistic")
      .withCalls(
        restCall(Method.GET, "/campaign/statistic/:id", getCampaign _),
        restCall(Method.GET, "/campaign/statistic/:id/start", startCampaign _)
      )
      .withAutoAcl(true)
  }
  
}

case class Campaign(
  campaignId: String,
  delivered: Int,
  notDelivered: Int,
  ban: Int,
  bounce: Int,
  status: String
)
object Campaign {
  implicit val format: Format[Campaign] = Json.format
}
