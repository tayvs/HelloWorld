package com.example.statistic.topic

sealed trait LocalDeliveryStatus

object LocalDeliveryStatus {

  case object NotFound extends LocalDeliveryStatus

  case object SuccessDelivery extends LocalDeliveryStatus

  case object NotDelivery extends LocalDeliveryStatus

  case object BouncedMail extends LocalDeliveryStatus

  case object BannedDelivery extends LocalDeliveryStatus

}