package com.example.statistic.impl

import akka.stream.scaladsl.Flow
import akka.{Done, NotUsed}
import com.example.statistic.api.{Campaign, CampaignStatisticService}
import com.example.statistic.impl.CampaignStatisticCommand.GetCampaign
import com.example.statistic.topic.{KafkaTopic, LocalDeliveryStatus}
import com.example.statistic.topic.LocalDeliveryStatus._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}

class CampaignStatisticServiceImpl(
  persistentEntityRegistry: PersistentEntityRegistry,
  statisticTopic: KafkaTopic
)(implicit ec: ExecutionContext) extends CampaignStatisticService {
  
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
          case SuccessDelivery => CampaignStatisticCommand.MailDelivered
          case NotDelivery => CampaignStatisticCommand.MailNotDelivered
          case BouncedMail => CampaignStatisticCommand.MailBounced
          case BannedDelivery => CampaignStatisticCommand.MailBanned
        }
        .map { status =>
          persistentEntityRegistry
            .refFor[CampaignStatisticEntity](campaignId)
            .ask(status)
        }
        .getOrElse(Future.successful(Done))
  }
  
}
