package com.example.hellostream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.example.hellostream.api.HelloworldStreamService
import com.example.hello.api.HelloworldService

import scala.concurrent.Future

/**
  * Implementation of the HelloworldStreamService.
  */
class HelloworldStreamServiceImpl(helloworldService: HelloworldService) extends HelloworldStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(helloworldService.hello(_).invoke()))
  }
}
