package com.example.statistic.impl

import akka.stream.scaladsl.Flow
import akka.{Done, NotUsed}
import com.example.statistic.api.{Campaign, CampaignStatisticService, LocalDeliveryStatus}
import com.example.statistic.api.LocalDeliveryStatus._
import com.example.statistic.topic.{KafkaTopic, LocalDeliveryStatus}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import scala.concurrent.Future

class CampaignStatisticServiceImpl(
  persistentEntityRegistry: PersistentEntityRegistry,
  statisticTopic: KafkaTopic
) extends CampaignStatisticService {
  
  statisticTopic
    .acctStatisticTopic
    .subscribe
    .atLeastOnce(Flow[(String, LocalDeliveryStatus)].mapAsync(8)(processStatistic.invoke(_)))
  
  override def getCampaign(campaignId: String): ServiceCall[NotUsed, Campaign] =
    ServiceCall { _ =>
      persistentEntityRegistry
        .refFor[CampaignStatisticEntity](campaignId)
        .ask(GetCampaign)
        .map(el => Campaign(campaignId, el.delivered, el.notDelivered, el.ban, el.bounce))
    }
  
  override def processStatistic: ServiceCall[(String, LocalDeliveryStatus), Done] = ServiceCall {
    case (campaignId, status) =>
      Some(status)
        .collect {
          case SuccessDelivery => CampaignStatisticEvent.Delivered
          case NotDelivery => CampaignStatisticEvent.NotDelivered
          case BouncedMail => CampaignStatisticEvent.Bounced
          case BannedDelivery => CampaignStatisticEvent.Banned
        }
        .map { status =>
          persistentEntityRegistry
            .refFor[CampaignStatisticEntity](campaignId)
            .ask(status)
        }
        .getOrElse(Future.successful(Done))
  }
  
}
