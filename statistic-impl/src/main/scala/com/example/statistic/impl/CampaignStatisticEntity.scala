package com.example.statistic.impl

import akka.Done
import com.example.statistic.impl.CampaignStatisticCommand.GetCampaign
import com.example.statistic.impl.CampaignStatisticEvent._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{JsValue => _, _}
import CampaignStatisticCommand._
import com.example.statistic.impl.CampaignStatisticState.{CampaignStatistic, StateNotInit}
import play.api.libs

import scala.collection.immutable.Seq

class CampaignStatisticEntity extends PersistentEntity {

  override type Command = CampaignStatisticCommand[_]
  override type Event = CampaignStatisticEvent
  override type State = CampaignStatisticState

  override def initialState: CampaignStatisticState = CampaignStatistic.empty

  override def behavior: Behavior = {
    case StateNotInit =>
      Actions()

    case state: CampaignStatistic =>
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
        .onReadOnlyCommand[GetCampaign, CampaignStatisticState] {
        case (GetCampaign(_), ctx, _) => ctx.reply(state)
      }
        .onEvent {
          case (Delivered, _) => state.copy(delivered = state.delivered + 1)
          case (NotDelivered, _) => state.copy(notDelivered = state.notDelivered + 1)
          case (Banned, _) => state.copy(ban = state.ban + 1)
          case (Bounced, _) => state.copy(bounce = state.bounce + 1)
        }
  }

}

sealed trait CampaignStatisticState

object CampaignStatisticState {

  case object StateNotInit extends CampaignStatisticState {

    //    implicit object OrderTypeJsonReader extends JsonReader[StateNotInit.type] with JsonWriter[StateNotInit.type] {
    //      lazy val orderTypes = List(StateNotInit)
    //
    //      lazy val string2orderType: Map[json.JsValue, StateNotInit.type] =
    //        orderTypes.map(ot => (json.JsString(ot.toString), ot)).toMap
    //
    //      override def read(json: json.JsValue): StateNotInit.type = string2orderType(json)
    //
    //      override def write(obj: StateNotInit.type): json.JsValue = json.JsString(obj.toString)
    //    }

  }

  case class CampaignStatistic(delivered: Int, notDelivered: Int, ban: Int, bounce: Int, status: String) extends CampaignStatisticState

  object CampaignStatistic {
    def empty: CampaignStatistic = CampaignStatistic(0, 0, 0, 0, "NotStarted")

    implicit val format: Format[CampaignStatistic] = Json.format
  }

}


sealed trait CampaignStatisticEvent extends AggregateEvent[CampaignStatisticEvent] {
  def aggregateTag = CampaignStatisticEvent.Tag
}

object CampaignStatisticEvent {
  val Tag = AggregateEventTag[CampaignStatisticEvent]

  //  implicit object Format extends ObjectJsonReader[CampaignStatisticEvent]

  case object Delivered extends CampaignStatisticEvent

  case object NotDelivered extends CampaignStatisticEvent

  case object Banned extends CampaignStatisticEvent

  case object Bounced extends CampaignStatisticEvent

}

sealed trait CampaignStatisticCommand[R] extends ReplyType[R]

object CampaignStatisticCommand {

  case class GetCampaign(entityId: String) extends CampaignStatisticCommand[CampaignStatisticState]

  object GetCampaign {
    implicit val format: Format[GetCampaign] = Json.format
  }

  sealed trait DoneCommands extends CampaignStatisticCommand[Done]

  case object MailDelivered extends DoneCommands

  case object MailNotDelivered extends DoneCommands

  case object MailBanned extends DoneCommands

  case object MailBounced extends DoneCommands

}

object CampaignStatisticSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CampaignStatistic],
    JsonSerializer[GetCampaign] /*,
    JsonSerializer[Hello],
    JsonSerializer[GreetingMessageChanged],
    JsonSerializer[HelloworldState]*/
  )
}

//object Utils {
//
//  import scala.reflect.ClassTag
//  import scala.reflect.runtime.universe._
//
//  def objectBy[T: ClassTag](name: String): T = {
//    val c = implicitly[ClassTag[T]]
//    Class.forName(c + "$" + name + "$").newInstance().asInstanceOf[T]
//  }
//
//  def string2trait[T: TypeTag : ClassTag]: Map[JsValue, T] = {
//    val clazz = typeOf[T].typeSymbol.asClass
//    clazz.knownDirectSubclasses.map { sc =>
//      val objectName = sc.toString.stripPrefix("object ")
//      (JsString(objectName), objectBy[T](objectName))
//    }.toMap
//  }

  //  class ObjectJsonReader[T: TypeTag : ClassTag] extends JsonReader[T] {
  //    val string2T: Map[JsValue, T] = string2trait[T]
  //
  //    def defaultValue: T = deserializationError(s"${implicitly[ClassTag[T]].runtimeClass.getCanonicalName} expected")
  //
  //    override def read(json: json.JsValue): T = string2T.getOrElse(json, defaultValue)
  //  }

//}