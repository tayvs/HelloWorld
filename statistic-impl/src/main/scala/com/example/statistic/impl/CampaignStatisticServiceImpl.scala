package com.example.statistic.impl

import akka.stream.scaladsl.Flow
import akka.{Done, NotUsed}
import com.example.statistic.api.{Campaign, CampaignStatisticService}
import com.example.statistic.impl.CampaignStatisticCommand.{CampaignStart, GetCampaign}
import com.example.statistic.impl.CampaignStatisticState.{CampaignStatistic, StateNotInit}
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
    .atLeastOnce(
      Flow[(String, LocalDeliveryStatus)]
        .mapAsync(8)(processStatistic.invoke(_))
    )
  
  override def getCampaign(campaignId: String): ServiceCall[NotUsed, Campaign] =
    ServiceCall { _ =>
      persistentEntityRegistry
        .refFor[CampaignStatisticEntity](campaignId)
        .ask(GetCampaign(campaignId))
        .mapTo[CampaignStatistic]
        .map(el => Campaign(campaignId, el.delivered, el.notDelivered, el.ban, el.bounce, el.status))
    }
  
  override def startCampaign(campaignId: String): ServiceCall[NotUsed, Campaign] =
    ServiceCall { _ =>
      val persEnt = persistentEntityRegistry
        .refFor[CampaignStatisticEntity](campaignId)
      
      persEnt.ask(CampaignStart)
        .flatMap(_ => persEnt.ask(GetCampaign(campaignId)))
        .mapTo[CampaignStatistic]
        .map(el => Campaign(campaignId, el.delivered, el.notDelivered, el.ban, el.bounce, el.status))
    }
  
  override def processStatistic: ServiceCall[(String, LocalDeliveryStatus), Done] = ServiceCall {
    case (campaignId, status) =>
      Some(status)
        .collect {
          case SuccessDelivery => CampaignStatisticCommand.MailsStatus(1)
          case NotDelivery => CampaignStatisticCommand.MailsStatus(0, 1)
          case BouncedMail => CampaignStatisticCommand.MailsStatus(0, 0, 1)
          case BannedDelivery => CampaignStatisticCommand.MailsStatus(0, 0, 0, 1)
        }
        .map { status =>
          persistentEntityRegistry
            .refFor[CampaignStatisticEntity](campaignId)
            .ask(status)
        }
        .getOrElse(Future.successful(Done))
  }
  
}
