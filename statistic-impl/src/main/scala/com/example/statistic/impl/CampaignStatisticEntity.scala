package com.example.statistic.impl

import akka.Done
import com.example.statistic.impl.CampaignStatisticCommand.{GetCampaign, _}
import com.example.statistic.impl.CampaignStatisticEvent._
import com.example.statistic.impl.CampaignStatisticState.{CampaignStatistic, StateNotInit}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import scala.collection.immutable.Seq

class CampaignStatisticEntity extends PersistentEntity {
  
  override type Command = CampaignStatisticCommand[_]
  override type Event = CampaignStatisticEvent
  override type State = CampaignStatisticState
  
  override def initialState: CampaignStatisticState = CampaignStatistic.empty
  
  override def behavior: Behavior = {
    case state: CampaignStatistic =>
      Actions()
        .onCommand[MailsStatus, Done] {
        case (MailsStatus(del, notdel, bans, bounces), ctx, _) =>
          val events = Seq[CampaignStatisticEvent.MailsEvent](
            Delivered(del), NotDelivered(notdel), Banned(bans), Bounced(bounces)
          )
            .filter(_.count != 0)
          ctx.thenPersistAll(events: _*) { () => ctx.reply(Done) }
      }
        .onCommand[CampaignStart.type, Done] {
        case (CampaignStart, ctx, _) => ctx.thenPersist(StatusChanged(CampaignStart.status)) { _ => ctx.reply(Done) }
      }
        .onReadOnlyCommand[GetCampaign, CampaignStatisticState] {
        case (GetCampaign(_), ctx, _) if state.status.equals("NotStarted") => ctx.reply(StateNotInit)
        case (GetCampaign(_), ctx, _) => ctx.reply(state)
      }
        .onEvent {
          case (StatusChanged(newStatus), _) => state.copy(status = newStatus)
          
          case (Delivered(c), _) => state.copy(delivered = state.delivered + c)
          case (NotDelivered(c), _) => state.copy(notDelivered = state.notDelivered + c)
          case (Banned(c), _) => state.copy(ban = state.ban + c)
          case (Bounced(c), _) => state.copy(bounce = state.bounce + c)
        }
  }
  
}