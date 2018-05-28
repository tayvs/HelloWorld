package com.example.statistic.api.model

import play.api.libs.json.{Format, Json}

case class RedirectStatistic (
  opened: Int,
  redirected: Int,
  uniqueRedirect: Int,
  unsubscribedOffer: Int,
  unsubscribedDelivery: Int
)
object RedirectStatistic {
  implicit val format: Format[RedirectStatistic] = Json.format
}