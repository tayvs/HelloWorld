package com.example.statistic.impl

import play.api.libs.json._

sealed trait CampaignStatisticState

object CampaignStatisticState {
  
  case object StateNotInit extends CampaignStatisticState {
    
    implicit val format: Format[StateNotInit.type] = new Format[StateNotInit.type] {
      override def reads(json: JsValue): JsResult[StateNotInit.type] = {
        json.validate[String]
          .filter(_ == StateNotInit.productPrefix)
          .map(_ => StateNotInit)
      }
      override def writes(o: StateNotInit.type): JsValue = JsString(StateNotInit.productPrefix)
    }
    
  }
  
  case class CampaignStatistic(delivered: Int, notDelivered: Int, ban: Int, bounce: Int, status: String)
    extends CampaignStatisticState
  
  object CampaignStatistic {
    def empty: CampaignStatistic = CampaignStatistic(0, 0, 0, 0, "NotStarted")
    
    implicit val format: Format[CampaignStatistic] = Json.format
  }
  
}