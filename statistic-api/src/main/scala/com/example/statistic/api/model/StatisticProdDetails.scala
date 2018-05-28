package com.example.statistic.api.model

import play.api.libs.json.{Format, Json}

case class StatisticProdDetails (
  servers: List[ProdVMTAStatistic],
  redirects: Option[RedirectStatistic] = None,
	revenue: String = "",
	status: String
)
object StatisticProdDetails {
	implicit val format: Format[StatisticProdDetails] = Json.format
}