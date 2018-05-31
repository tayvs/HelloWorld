package com.example.statistic.impl

import akka.stream.scaladsl.Flow
import akka.{Done, NotUsed}
import com.example.statistic.api.model.{ProdVMTA, ProdVMTAStatistic, StatisticProdDetails}
import com.example.statistic.api.{AccumulateWhileUnchanged, Campaign, CampaignStatisticService}
import com.example.statistic.impl.CampaignStatisticCommand.{CampaignStart, GetCampaign}
import com.example.statistic.impl.CampaignStatisticState.{CampaignStatistic, StateNotInit}
import com.example.statistic.topic.{JobId, KafkaTopic, LocalDeliveryStatus}
import com.example.statistic.topic.LocalDeliveryStatus._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.breakOut

class CampaignStatisticServiceImpl(
                                    persistentEntityRegistry: PersistentEntityRegistry,
                                    statisticTopic: KafkaTopic
                                  )(implicit ec: ExecutionContext) extends CampaignStatisticService {

  statisticTopic
    .acctStatisticTopic
    .subscribe
    .atLeastOnce(
      Flow[LocalDeliveryStatus]
        .collect { case dm: DefinedMail => dm }
        .via(new AccumulateWhileUnchanged[DefinedMail, String](_.jobId.jobId))
        .mapAsync(1)(bulkProcessStatistic.invoke)
    )

  override def getCampaign(campaignId: String): ServiceCall[NotUsed, StatisticProdDetails] =
    ServiceCall { _ =>
      persistentEntityRegistry
        .refFor[CampaignStatisticEntity](campaignId)
        .ask(GetCampaign(campaignId))
        .mapTo[CampaignStatistic]
        .map(getCampaign2StatisticProdDetails)
    }

  override def startCampaign(campaignId: String): ServiceCall[NotUsed, StatisticProdDetails] =
    ServiceCall { _ =>
      val persEnt = persistentEntityRegistry
        .refFor[CampaignStatisticEntity](campaignId)

      persEnt.ask(CampaignStart)
        .flatMap(_ => persEnt.ask(GetCampaign(campaignId)))
        .mapTo[CampaignStatistic]
        .map(getCampaign2StatisticProdDetails)
    }

  def getCampaign2StatisticProdDetails(stat: CampaignStatistic): StatisticProdDetails = {
    val servers: List[ProdVMTAStatistic] = ProdVMTAStatistic(
      name = "server",
      status = "undefined",
      list = stat.vmtas.values
        .map(vmta => ProdVMTA(
          ip = JobId(vmta.jobId).ip,
          totalMailCount = 999,
          sentMailCount = vmta.delivered,
          bannedMailCount = vmta.ban,
          bounceMailCount = vmta.bounce,
          notDeliveryCount = vmta.notDelivered,
          status = vmta.status,
          domainId = JobId(vmta.jobId).domain + JobId(vmta.jobId).ip,
          domain = JobId(vmta.jobId).domain
        ))(breakOut)
    ) :: Nil
    StatisticProdDetails(servers = servers, status = "Started")
  }

  override def processStatistic: ServiceCall[LocalDeliveryStatus, Done] = ServiceCall {
    Some(_)
      .collect {
        case SuccessDelivery(jobId) => CampaignStatisticCommand.MailsStatus(jobId, 1)
        case NotDelivery(jobId) => CampaignStatisticCommand.MailsStatus(jobId, 0, 1)
        case BouncedMail(jobId) => CampaignStatisticCommand.MailsStatus(jobId, 0, 0, 1)
        case BannedDelivery(jobId) => CampaignStatisticCommand.MailsStatus(jobId, 0, 0, 0, 1)
      }
      .map { status =>
        persistentEntityRegistry
          .refFor[CampaignStatisticEntity](status.jobId.campaignId)
          .ask(status)
      }
      .getOrElse(Future.successful(Done))
  }

  override def bulkProcessStatistic: ServiceCall[List[DefinedMail], Done] = ServiceCall { statuses =>
    val jobId = statuses.head.jobId
    val status = statuses
      .foldLeft(CampaignStatisticCommand.MailsStatus(jobId))((acc, el) => el match {
        case _: SuccessDelivery => acc.copy(delivered = acc.delivered + 1)
        case _: NotDelivery => acc.copy(notDelivered = acc.notDelivered + 1)
        case _: BouncedMail => acc.copy(bounce = acc.bounce + 1)
        case _: BannedDelivery => acc.copy(ban = acc.ban + 1)
      })

    persistentEntityRegistry
      .refFor[CampaignStatisticEntity](status.jobId.campaignId)
      .ask(status)
  }

}
