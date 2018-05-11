package com.example.statistic.topic

import akka.util.ByteString
import scala.collection.immutable
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.NegotiatedDeserializer
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

trait KafkaTopic extends Service {
  
  def acctStatisticTopic: Topic[(String, LocalDeliveryStatus)]
  
  override final def descriptor: Descriptor = {
    import Service._
    named("acct-statistic-topic")
      .withTopics(
        topic("campaign", acctStatisticTopic)(KafkaTopic.msgSer)
      )
      .withAutoAcl(true)
  }
  
}
object KafkaTopic {
  
  val jsonSer: NegotiatedDeserializer[(String, LocalDeliveryStatus), ByteString] = (wire: ByteString) => {
    val csvLine = Json
      .parse(wire.iterator.asInputStream)
      .as[List[String]].mkString(",")
    
    val localDeliveryStatus = csvLine match {
      case Delivered() => LocalDeliveryStatus.SuccessDelivery
      case Bounced() => LocalDeliveryStatus.BouncedMail
      case Banned() => LocalDeliveryStatus.BannedDelivery
      case NotDelivery() => LocalDeliveryStatus.NotDelivery
      case _ => LocalDeliveryStatus.NotFound
    }
    
    val campaignId = "1111"
    (campaignId, localDeliveryStatus)
  }
  
  implicit val msgSer: MessageSerializer[(String, LocalDeliveryStatus), ByteString] =
    new MessageSerializer[(String, LocalDeliveryStatus), ByteString] {
      override def serializerForRequest
      : MessageSerializer.NegotiatedSerializer[(String, LocalDeliveryStatus), ByteString] = ???
      override def deserializer(protocol: MessageProtocol)
      : NegotiatedDeserializer[(String, LocalDeliveryStatus), ByteString] = jsonSer
      override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol])
      : MessageSerializer.NegotiatedSerializer[(String, LocalDeliveryStatus), ByteString] = ???
    }
  
}

sealed trait LocalDeliveryStatus

object LocalDeliveryStatus {
  
  case object NotFound extends LocalDeliveryStatus
  
  case object SuccessDelivery extends LocalDeliveryStatus
  case object NotDelivery extends LocalDeliveryStatus
  case object BouncedMail extends LocalDeliveryStatus
  case object BannedDelivery extends LocalDeliveryStatus
  
}

sealed trait Line {def unapply(line: String): Boolean}
object Delivered extends Line {
  override def unapply(line: String): Boolean = line.head == 'd'
}
object Bounced extends Line {
  val bouncedMessages = Vector(
    "The email account that you tried to reach does not exist",
    "mailbox unavailable",
    "This account has been disabled or discontinued",
    "user doesn't have a yahoo.com account"
  )
  
  override def unapply(line: String): Boolean = bouncedMessages.exists { bannedMsg => line.contains(bannedMsg) }
}
object Banned extends Line {
  val bannedMessages: Vector[String] = Vector(
    "Please contact your Internet service provider since part of their network is on our block list",
    "To reduce the amount of spam sent to Gmail, this message has been blocked",
    "temporarily deferred due to user complaints",
    "delivery not authorized"
  )
  
  override def unapply(line: String): Boolean = bannedMessages.exists { bannedMsg => line.contains(bannedMsg) }
}
object NotDelivery extends Line {
  override def unapply(line: String): Boolean = line.head == 'b'
}

