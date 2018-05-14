package com.example.statistic.topic

import akka.util.ByteString
import com.example.statistic.topic.Line._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service}
import play.api.libs.json.Json
import scala.collection.immutable

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

  val jsonDeser: NegotiatedDeserializer[(String, LocalDeliveryStatus), ByteString] = (wire: ByteString) => {
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

  val jsonSer: NegotiatedSerializer[(String, LocalDeliveryStatus), ByteString] =
    (message: (String, LocalDeliveryStatus)) => ByteString(message.toString().getBytes)

  val msgSer: MessageSerializer[(String, LocalDeliveryStatus), ByteString] =
    new MessageSerializer[(String, LocalDeliveryStatus), ByteString] {
      override def serializerForRequest
      : NegotiatedSerializer[(String, LocalDeliveryStatus), ByteString] = jsonSer

      override def deserializer(protocol: MessageProtocol)
      : NegotiatedDeserializer[(String, LocalDeliveryStatus), ByteString] = jsonDeser

      override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol])
      : NegotiatedSerializer[(String, LocalDeliveryStatus), ByteString] = jsonSer
    }

}






