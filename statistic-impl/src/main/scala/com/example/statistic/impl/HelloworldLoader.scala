package com.example.statistic.impl

import com.example.statistic.api.CampaignStatisticService
import com.example.statistic.topic.KafkaTopic
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class CampaignStatisticLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CampaignStatisticApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CampaignStatisticApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[CampaignStatisticService])
}

abstract class CampaignStatisticApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[CampaignStatisticService](wire[CampaignStatisticServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = CampaignStatisticSerializerRegistry
  
  lazy val acctKafkaTopic = serviceClient.implement[KafkaTopic]
  
  // Register the HelloWorld persistent entity
  persistentEntityRegistry.register(wire[CampaignStatisticEntity])
}
