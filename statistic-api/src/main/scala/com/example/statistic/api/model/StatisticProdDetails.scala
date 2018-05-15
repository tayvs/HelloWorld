package com.example.statistic.api.model

case class StatisticProdDetails (
  servers: List[ProdVMTAStatistic],
  redirects: Option[RedirectStatistic] = None,
	revenue: String = "",
	status: String
)