package com.example.statistic.impl

import com.example.statistic.impl.CampaignStatisticCommand._
import com.example.statistic.impl.CampaignStatisticEvent._
import com.example.statistic.impl.CampaignStatisticState._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import scala.collection.immutable

object CampaignStatisticSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer[CampaignStatistic],
    JsonSerializer[StateNotInit.type],
    JsonSerializer[GetCampaign],
    JsonSerializer[MailsStatus],
    JsonSerializer[Delivered],
    JsonSerializer[NotDelivered],
    JsonSerializer[Banned],
    JsonSerializer[Bounced],
    JsonSerializer[CampaignStart.type],
    JsonSerializer[StatusChanged]
  )
}
