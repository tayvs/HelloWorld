package com.example.statistic.api.model

case class RedirectStatistic (
  opened: Int,
  redirected: Int,
  uniqueRedirect: Int,
  unsubscribedOffer: Int,
  unsubscribedDelivery: Int
)