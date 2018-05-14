package com.example.statistic.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.example.statistic.impl.CampaignStatisticCommand.GetCampaign
import com.example.statistic.impl.CampaignStatisticState.CampaignStatistic
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class CampaignStatisticEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("CampaignStatisticEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(CampaignStatisticSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def entityId = "camp-1"

  private def withTestDriver(block: PersistentEntityTestDriver[CampaignStatisticCommand[_], CampaignStatisticEvent, CampaignStatisticState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CampaignStatisticEntity, entityId)
    block(driver)
    driver.getAllIssues should have size 0
  }

  "CampaignStatistic entity" should {

    "return empty campaign if campaign not created" in withTestDriver { driver =>
      val outcome = driver.run(GetCampaign(entityId))
      outcome.replies should contain only CampaignStatistic.empty
    }

    "statistic must be with entityId and one delivery mail" in withTestDriver { driver =>
      val outcomes1 = driver.run(CampaignStatisticCommand.MailDelivered)
      outcomes1.events should contain only CampaignStatisticEvent.Delivered
      outcomes1.replies should contain only Done
      val outcomes2 = driver.run(GetCampaign(entityId))
      outcomes2.replies should contain only CampaignStatistic(1, 0, 0, 0, "NotStarted")
    }
  }

}
