package com.example.statistic.impl

import akka.NotUsed
import com.example.statistic.api.CampaignStatisticService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

class CampaignStatisticServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)
  extends CampaignStatisticService {
  
  override def getCampaign(campaignId: String): ServiceCall[NotUsed, NotUsed] = ???
  override def acctStatisticTopic: Topic[NotUsed] = ???
  
}
