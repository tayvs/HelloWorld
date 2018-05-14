package com.example.statistic.topic

sealed trait Line {
  def unapply(line: String): Boolean
}

object Line {
  
  object Delivered extends Line {
    override def unapply(line: String): Boolean = line.head == 'd'
  }
  
  object Bounced extends Line {
    val bouncedMessages = Vector(
      "The email account that you tried to reach does not exist",
      "mailbox unavailable",
      "This account has been disabled or discontinued",
      "user doesn't have a yahoo.com account"
    )
    
    override def unapply(line: String): Boolean = bouncedMessages.exists { bannedMsg => line.contains(bannedMsg) }
  }
  
  object Banned extends Line {
    val bannedMessages: Vector[String] = Vector(
      "Please contact your Internet service provider since part of their network is on our block list",
      "To reduce the amount of spam sent to Gmail, this message has been blocked",
      "temporarily deferred due to user complaints",
      "delivery not authorized"
    )
    
    override def unapply(line: String): Boolean = bannedMessages.exists { bannedMsg => line.contains(bannedMsg) }
  }
  
  object NotDelivery extends Line {
    override def unapply(line: String): Boolean = line.head == 'b'
  }
  
}