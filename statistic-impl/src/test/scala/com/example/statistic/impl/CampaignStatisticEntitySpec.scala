package com.example.statistic.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.example.statistic.impl.CampaignStatisticCommand.{CampaignStart, GetCampaign}
import com.example.statistic.impl.CampaignStatisticEvent.StatusChanged
import com.example.statistic.impl.CampaignStatisticState.{CampaignStatistic, StateNotInit}
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
  
  private def withTestDriver(block: PersistentEntityTestDriver[CampaignStatisticCommand[_], CampaignStatisticEvent,
    CampaignStatisticState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CampaignStatisticEntity, entityId)
    block(driver)
    driver.getAllIssues should have size 0
  }
  
  "CampaignStatistic entity" should {
    
    "statistic must be StateNotInit BUT commands must return Done and persist event" in withTestDriver { driver =>
      val outcomes1 = driver.run(CampaignStatisticCommand.MailsStatus(1))
      outcomes1.events should contain only CampaignStatisticEvent.Delivered()
      outcomes1.replies should contain only Done
      
      val outcomes2 = driver.run(GetCampaign(entityId))
      outcomes2.replies should contain only StateNotInit
    }
    
    "statistic must persist StatusChanged event with status" in withTestDriver{ driver =>
      val outcomesChangeStatus = driver.run(CampaignStart)
      outcomesChangeStatus.events should contain only StatusChanged(CampaignStart.status)
      outcomesChangeStatus.replies should contain only Done
    }
    
    "statistic must be with entityId and one delivered mail" in withTestDriver { driver =>
      val outcomes1 = driver.run(CampaignStatisticCommand.MailsStatus(1))
      outcomes1.events should contain only CampaignStatisticEvent.Delivered()
      outcomes1.replies should contain only Done
      
      val changeStatusCommand = CampaignStart
      
      val outcomesChangeStatus = driver.run(changeStatusCommand)
      outcomesChangeStatus.events should contain only StatusChanged(changeStatusCommand.status)
      outcomesChangeStatus.replies should contain only Done
      
      driver.run(GetCampaign(entityId))
        .replies should contain only CampaignStatistic(1, 0, 0, 0, changeStatusCommand.status)
    }
    
    "return StateNotInit if campaign not created" in withTestDriver { driver =>
      val outcome = driver.run(GetCampaign(entityId))
      outcome.replies should contain only StateNotInit
    }
  }
  
}
