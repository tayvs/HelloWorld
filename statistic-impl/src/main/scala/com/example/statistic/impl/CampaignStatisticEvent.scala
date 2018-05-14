package com.example.statistic.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

sealed trait CampaignStatisticEvent extends AggregateEvent[CampaignStatisticEvent] {
  def aggregateTag = CampaignStatisticEvent.Tag
}

object CampaignStatisticEvent {
  val Tag = AggregateEventTag[CampaignStatisticEvent]
  
  sealed trait MailsEvent extends CampaignStatisticEvent {
    def count: Int
  }
  
  case class Delivered(count: Int = 1) extends MailsEvent
  object Delivered {
    implicit val format: Format[Delivered] = Json.format
  }
  
  case class NotDelivered(count: Int = 1) extends MailsEvent
  object NotDelivered {
    implicit val format: Format[NotDelivered] = Json.format
  }
  
  case class Banned(count: Int = 1) extends MailsEvent
  object Banned {
    implicit val format: Format[Banned] = Json.format
  }
  
  case class Bounced(count: Int = 1) extends MailsEvent
  object Bounced {
    implicit val format: Format[Bounced] = Json.format
  }
  
  case class StatusChanged(newStatus: String) extends CampaignStatisticEvent
  object StatusChanged {
    implicit val format: Format[StatusChanged] = Json.format
  }
  
}