package com.example.statistic.impl

import akka.Done
import com.example.statistic.impl.CampaignStatisticCommand.{GetCampaign, _}
import com.example.statistic.impl.CampaignStatisticEvent.{MailsEvent, _}
import com.example.statistic.impl.CampaignStatisticState.{CampaignStatistic, StateNotInit, VMTA}
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
        case (ms: MailsStatus, ctx, _) => ctx.thenPersistAll(mailStatus2PersistenceEvents(ms): _*) { () => ctx.reply(Done) }
      }
        .onCommand[CampaignStart.type, Done] {
        case (CampaignStart, ctx, _) => ctx.thenPersist(StatusChanged(CampaignStart.status)) { _ => ctx.reply(Done) }
      }
        .onReadOnlyCommand[GetCampaign, CampaignStatisticState] {
        case (GetCampaign(_), ctx, _) if state.status.equals("NotStarted") => ctx.invalidCommand("Campaign not found") /*reply(StateNotInit)*/
        case (GetCampaign(_), ctx, _) => ctx.reply(state)
      }
        .onEvent {
          case (StatusChanged(newStatus), _) => state.copy(status = newStatus)

          case (me: MailsEvent, _) => updateState(state, me.jobId.jobId)(me)
        }
  }

  implicit val str2Symb: String => Symbol = Symbol(_)

  def updateState(state: CampaignStatistic, jobId: Symbol)(func: VMTA => VMTA): CampaignStatistic = {
    val vmta = state.vmtas.getOrElse(jobId, VMTA.inProcess(jobId.toString()))
    val vmtas = state.vmtas.updated(jobId, func(vmta))
    state.copy(vmtas = vmtas)
  }

  implicit val mailEvent2VMTAUpdate: MailsEvent => VMTA => VMTA = {
    case Delivered(_, count) => vmta => vmta.copy(delivered = vmta.delivered + count)
    case NotDelivered(_, count) => vmta => vmta.copy(notDelivered = vmta.notDelivered + count)
    case Banned(_, count) => vmta => vmta.copy(ban = vmta.ban + count)
    case Bounced(_, count) => vmta => vmta.copy(bounce = vmta.bounce + count)
  }

  implicit val mailStatus2PersistenceEvents: MailsStatus => Seq[CampaignStatisticEvent.MailsEvent] = {
    case MailsStatus(jobId, del, notdel, bans, bounces) =>
      Seq(Delivered(jobId, del), NotDelivered(jobId, notdel), Banned(jobId, bans), Bounced(jobId, bounces)).filter(_.count != 0)
  }

}