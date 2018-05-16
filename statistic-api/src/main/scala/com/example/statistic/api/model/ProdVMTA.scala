package com.example.statistic.api.model

case class ProdVMTA(
  ip: String,
  totalMailCount: Int,
  sentMailCount: Int,
  bannedMailCount: Int,
  bounceMailCount: Int,
  notDeliveryCount: Int,
  status: String,
  statusSpam: Option[String] = None,
  banned: Boolean = false,
  isSpam: Boolean = false,
  domainId: String,
  domain: String
)