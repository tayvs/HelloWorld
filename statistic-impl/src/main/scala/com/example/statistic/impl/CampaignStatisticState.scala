package com.example.statistic.impl

import play.api.libs.json._

sealed trait CampaignStatisticState

object CampaignStatisticState {

  case object StateNotInit extends Exception("Campaign not found") with CampaignStatisticState {

    implicit val format: Format[StateNotInit.type] = new Format[StateNotInit.type] {
      override def reads(json: JsValue): JsResult[StateNotInit.type] = {
        json.validate[String]
          .filter(_ == StateNotInit.productPrefix)
          .map(_ => StateNotInit)
      }

      override def writes(o: StateNotInit.type): JsValue = JsString(StateNotInit.productPrefix)
    }

  }

  case class VMTA(jobId: String, delivered: Int, notDelivered: Int, ban: Int, bounce: Int, status: String)

  object VMTA {
    def inProcess(jobId: String) = VMTA(jobId, 0, 0, 0, 0, "InProcess")

    implicit val format: Format[VMTA] = Json.format
  }

  case class CampaignStatistic(vmtas: Map[Symbol, VMTA], status: String) extends CampaignStatisticState

  object CampaignStatistic {
    def empty: CampaignStatistic = CampaignStatistic(Map.empty, "NotStarted")

    implicit val format: Format[CampaignStatistic] = Json.format
  }

}