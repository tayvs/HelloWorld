package com.example.statistic.api.model

import play.api.libs.json.{Format, Json}

case class ProdVMTAStatistic(
  name:  String,
  status: String,
  list: List[ProdVMTA]
)
object ProdVMTAStatistic {
  implicit val format: Format[ProdVMTAStatistic] = Json.format
}