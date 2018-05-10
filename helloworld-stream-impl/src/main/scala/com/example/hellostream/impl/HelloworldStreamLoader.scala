package com.example.hellostream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.example.hellostream.api.HelloworldStreamService
import com.example.hello.api.HelloworldService
import com.softwaremill.macwire._

class HelloworldStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloworldStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloworldStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HelloworldStreamService])
}

abstract class HelloworldStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[HelloworldStreamService](wire[HelloworldStreamServiceImpl])

  // Bind the HelloworldService client
  lazy val helloworldService = serviceClient.implement[HelloworldService]
}
