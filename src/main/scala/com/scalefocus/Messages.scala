package com.scalefocus

case class Message(sender: String, recipient: String, content: String)
case class SendMessage(sender: String, recipient: String, content: String)
case class AddNode(id: String, shape: Option[String] = None)
case class AddEdge(source: String, target: String)
case class RouteMessage(senderId: String, recipientId: String, message: String)
case class RegisterActor(id: String)

object Messages {
  def createSendMessage(sender: String, recipient: String, content: String): SendMessage = {
    SendMessage(sender, recipient, content)
  }

  def createMessage(sender: String, recipient: String, content: String): Message = {
    Message(sender, recipient, content)
  }
}
