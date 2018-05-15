package com.example.statistic.api.model

case class ProdVMTAStatistic(
  name:  String,
  status: String,
  list: List[ProdVMTA]
)