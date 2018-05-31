package com.example.statistic.api

import akka.stream._
import akka.stream.stage._


final class AccumulateWhileUnchanged[E, P](propertyExtractor: E => P)
  extends GraphStage[FlowShape[E, List[E]]] {

  val in: Inlet[E] = Inlet[E]("AccumulateWhileUnchanged.in")
  val out: Outlet[List[E]] = Outlet[List[E]]("AccumulateWhileUnchanged.out")

  override def shape: FlowShape[E, List[E]] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    private var currentState: Option[P] = None
    private var buffer = List.empty[E]

    setHandlers(in, out, new InHandler with OutHandler {

      override def onPush(): Unit = {
        val nextElement = grab(in)
        val nextState = propertyExtractor(nextElement)

        if (currentState.isEmpty || currentState.contains(nextState)) {
          buffer  = nextElement :: buffer
          pull(in)
        } else {
          push(out, buffer)
          buffer = Nil
        }

        currentState = Some(nextState)
      }

      override def onPull(): Unit = {
        pull(in)
      }

      override def onUpstreamFinish(): Unit = {
        if (buffer.nonEmpty) {
          emit(out, buffer)
        }
        completeStage()
      }
    })

    override def postStop(): Unit = {
      buffer = Nil
    }
  }
}