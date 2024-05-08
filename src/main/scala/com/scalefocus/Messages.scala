package com.scalefocus

case class Message(sender: String, recipient: String, content: String)
case class SendMessage(sender: String, recipient: String, content: String)

object Messages {
  def createSendMessage(sender: String, recipient: String, content: String): SendMessage = {
    SendMessage(sender, recipient, content)
  }

  def createMessage(sender: String, recipient: String, content: String): Message = {
    Message(sender, recipient, content)
  }
}
