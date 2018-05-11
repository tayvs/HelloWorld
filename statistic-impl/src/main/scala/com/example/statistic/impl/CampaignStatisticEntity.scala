package com.example.statistic.impl

import akka.Done
import com.example.statistic.impl.CampaignStatisticEvent._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}
import scala.collection.immutable.Seq

class CampaignStatisticEntity extends PersistentEntity {
  
  override type Command = CampaignStatisticCommand[_]
  override type Event = CampaignStatisticEvent
  override type State = CampaignStatisticState
  
  override def initialState: CampaignStatisticState = CampaignStatisticState(0, 0, 0, 0)
  
  override def behavior: Behavior =
    Actions()
      .onCommand[CampaignStatisticCommand[Done] with CampaignStatisticEvent, Done] {
      case (comAndEvent: CampaignStatisticEvent, ctx, state) =>
        ctx.thenPersist(comAndEvent) { _ => ctx.reply(Done)
      }
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

case class CampaignStatisticState(delivered: Int, notDelivered: Int, ban: Int, bounce: Int)
object CampaignStatisticState {
  implicit val format: Format[CampaignStatisticState] = Json.format
}

sealed trait CampaignStatisticEvent extends AggregateEvent[CampaignStatisticEvent] {
  def aggregateTag = CampaignStatisticEvent.Tag
}
object CampaignStatisticEvent {
  val Tag = AggregateEventTag[CampaignStatisticEvent]
  
  case object Delivered extends CampaignStatisticEvent with CampaignStatisticCommand[Done]
  case object NotDelivered extends CampaignStatisticEvent with CampaignStatisticCommand[Done]
  case object Banned extends CampaignStatisticEvent with CampaignStatisticCommand[Done]
  case object Bounced extends CampaignStatisticEvent with CampaignStatisticCommand[Done]
}

sealed trait CampaignStatisticCommand[R] extends ReplyType[R]
case object GetCampaign extends CampaignStatisticCommand[CampaignStatisticState]

object CampaignStatisticSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CampaignStatisticState]/*,
    JsonSerializer[Hello],
    JsonSerializer[GreetingMessageChanged],
    JsonSerializer[HelloworldState]*/
  )
}