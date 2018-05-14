package com.example.statistic.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json._

sealed trait CampaignStatisticCommand[R] extends ReplyType[R]

object CampaignStatisticCommand {
  
  case class GetCampaign(entityId: String) extends CampaignStatisticCommand[CampaignStatisticState]
  object GetCampaign {
    implicit val format: Format[GetCampaign] = Json.format
  }
  
  sealed trait DoneCommands extends CampaignStatisticCommand[Done]
  
  case class MailsStatus(delivered: Int = 0, notDelivered: Int = 0, ban: Int = 0, bounce: Int = 0) extends DoneCommands
  object MailsStatus {
    implicit val format: Format[MailsStatus] = Json.format
  }
  
  sealed abstract class ChangeStatusCommands(val status: String) extends DoneCommands
  case object CampaignStart extends ChangeStatusCommands("Start") {
    
    implicit val format: Format[CampaignStart.type] = new Format[CampaignStart.type] {
      override def reads(json: JsValue): JsResult[CampaignStart.type] = {
        json.validate[String]
          .filter(_ == CampaignStart.productPrefix)
          .map(_ => CampaignStart)
      }
      override def writes(o: CampaignStart.type): JsValue = JsString(CampaignStart.productPrefix)
    }
    
  }
  
}