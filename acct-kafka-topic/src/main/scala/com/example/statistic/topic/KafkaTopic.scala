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

  def acctStatisticTopic: Topic[LocalDeliveryStatus]

  override final def descriptor: Descriptor = {
    import Service._
    named("acct-statistic-topic")
      .withTopics(
        topic("testCampaign", acctStatisticTopic)(KafkaTopic.msgSer)
      )
      .withAutoAcl(true)
  }

}

object KafkaTopic {

  val jsonDeser: NegotiatedDeserializer[LocalDeliveryStatus, ByteString] = (wire: ByteString) => {
    val csvData = Json.parse(wire.iterator.asInputStream).as[List[String]]

    csvData.mkString(",") match {
      case Delivered() => LocalDeliveryStatus.SuccessDelivery(JobId(csvData(19)))
      case Bounced() => LocalDeliveryStatus.BouncedMail(JobId(csvData(19)))
      case Banned() => LocalDeliveryStatus.BannedDelivery(JobId(csvData(19)))
      case NotDelivery() => LocalDeliveryStatus.NotDelivery(JobId(csvData(19)))
      case _ => LocalDeliveryStatus.NotFound
    }
  }

  val jsonSer: NegotiatedSerializer[LocalDeliveryStatus, ByteString] =
    (message: LocalDeliveryStatus) => ByteString(message.toString.getBytes)

  val msgSer: MessageSerializer[LocalDeliveryStatus, ByteString] = new MessageSerializer[LocalDeliveryStatus, ByteString] {
    override def serializerForRequest: NegotiatedSerializer[LocalDeliveryStatus, ByteString] = jsonSer

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[LocalDeliveryStatus, ByteString] = jsonDeser

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[LocalDeliveryStatus, ByteString] = jsonSer
  }

}






