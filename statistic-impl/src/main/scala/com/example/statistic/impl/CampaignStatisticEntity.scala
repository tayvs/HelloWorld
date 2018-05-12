package com.example.statistic.impl

import akka.Done
import com.example.statistic.impl.CampaignStatisticCommand.GetCampaign
import com.example.statistic.impl.CampaignStatisticEvent._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}
import CampaignStatisticCommand._

import scala.collection.immutable.Seq

class CampaignStatisticEntity extends PersistentEntity {

  override type Command = CampaignStatisticCommand[_]
  override type Event = CampaignStatisticEvent
  override type State = CampaignStatisticState

  override def initialState: CampaignStatisticState = CampaignStatisticState(0, 0, 0, 0)


  override def behavior: Behavior = {
    case cmp: CampaignStatisticState =>
      Actions()
        .onCommand[MailDelivered.type, Done] {
        case (MailDelivered, ctx, _) => ctx.thenPersist(Delivered) { _ => ctx.reply(Done) }
      }
        .onCommand[MailNotDelivered.type, Done] {
        case (MailNotDelivered, ctx, _) => ctx.thenPersist(NotDelivered) { _ => ctx.reply(Done) }
      }
        .onCommand[MailBanned.type, Done] {
        case (MailBanned, ctx, _) => ctx.thenPersist(Banned) { _ => ctx.reply(Done) }
      }
        .onCommand[MailBounced.type, Done] {
        case (MailBounced, ctx, _) => ctx.thenPersist(Bounced) { _ => ctx.reply(Done) }
      }
        .onReadOnlyCommand[GetCampaign.type, CampaignStatisticState] {
        case (GetCampaign, ctx, state) => ctx.reply(state)
      }
        .onEvent {
          case (Delivered, state) => state.copy(delivered = state.delivered + 1)
          case (NotDelivered, state) => state.copy(notDelivered = state.notDelivered + 1)
          case (Banned, state) => state.copy(ban = state.ban + 1)
          case (Bounced, state) => state.copy(bounce = state.bounce + 1)
        }
  }

}

case class CampaignStatisticState(delivered: Int, notDelivered: Int, ban: Int, bounce: Int)

object CampaignStatisticState {
  implicit val format: Format[CampaignStatisticState] = Json.format
}

sealed trait CampaignStatisticEvent extends AggregateEvent[CampaignStatisticEvent] {
  def aggregateTag = CampaignStatisticEvent.Tag
}

object CampaignStatisticEvent {
  val Tag = AggregateEventTag[CampaignStatisticEvent]

  case object Delivered extends CampaignStatisticEvent

  case object NotDelivered extends CampaignStatisticEvent

  case object Banned extends CampaignStatisticEvent

  case object Bounced extends CampaignStatisticEvent

}

sealed trait CampaignStatisticCommand[R] extends ReplyType[R]

object CampaignStatisticCommand {

  case object GetCampaign extends CampaignStatisticCommand[CampaignStatisticState]

  sealed trait DoneCommands extends CampaignStatisticCommand[Done]

  case object MailDelivered extends DoneCommands

  case object MailNotDelivered extends DoneCommands

  case object MailBanned extends DoneCommands

  case object MailBounced extends DoneCommands

}

object CampaignStatisticSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CampaignStatisticState] /*,
    JsonSerializer[Hello],
    JsonSerializer[GreetingMessageChanged],
    JsonSerializer[HelloworldState]*/
  )
}