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
  banned: Boolean,
  isSpam: Boolean,
  domainId: String,
  domain: String
)