package com.example.statistic.topic

sealed trait LocalDeliveryStatus

object LocalDeliveryStatus {

  sealed trait DefinedMail extends LocalDeliveryStatus {
    val jobId: JobId
  }

  case object NotFound extends LocalDeliveryStatus

  case class SuccessDelivery(jobId: JobId) extends DefinedMail

  case class NotDelivery(jobId: JobId) extends DefinedMail

  case class BouncedMail(jobId: JobId) extends DefinedMail

  case class BannedDelivery(jobId: JobId) extends DefinedMail

}