package chat

import scala.collection.mutable.HashMap
import scala.concurrent.Channel
import scala.async.Async.async
import scalatags.JsDom._
import scalatags.JsDom.all._
import scala.scalajs.js.JSApp
import org.scalajs.dom.console
import hyperflux.annotation._
import hyperflux.SessionID
import hyperflux.routing_helpers._
import hyperflux.session_helpers._
import hyperflux.template_helpers._

/*
 * Server section
 */

@Server("http://localhost", 24105)
object ChatServer {

  private val users = new HashMap[SessionID, (String, Channel[String])]

  def sayHello(name: String): Boolean = {
    println(s"New user tries to say hello with SID $SID and name '$name'")
    if (users.valuesIterator exists (_._1 == name)) {
      println(s"Name is taken!")
      false
    } else {
      users += (SID -> (name, new Channel[String]))
      println(s"Name available, new user '$name' with SID $SID")
      true
    }
  }

  def writeMessage(msg: String) {
    // this lookup serves two purposes: it checks whether the sending client
    // is registered, and if it is, it retrieves the matching user name
    println(s"Incoming message from $SID")
    users get SID match {
      case Some((name, _)) => {
        println(s"Sender identified as '$name', message: $msg")
        val text = name + ": " + msg
        // place message in every user's channel
        users foreach {
          case (_, (_, chan)) => chan write text
        }
      }
      // in other cases, the sender is not registered.
      // do nothing in this case
      case _ =>
    }
  }

  def readMessage(): String = {
    users get SID match {
      case Some((name, chan)) => {
        println(s"User '$name' is waiting for new messages ..")
        chan.read
      }
      case _ => {
        println(s"readMessage request from unknown user with SID $SID")
        ChatShared.ERROR_MSG
      }
    }
  }
}

object ChatShared {
  val ERROR_MSG = "ERR/NOT_REGISTERED"
}

/*
 * Client logic section
 */

@Client
object ChatClient { 

  def main() { }

  /*
   * This method is called when the submit button on the login page is pressed
   * It retrieves the entered name and hands it to the server
   * If the login was successful, it redirects the client to the actual chat
   */
  def nameEntered() {
    if (ChatServer sayHello (ChatInterface.nameInputBox.value)) {
      redirect(ChatInterface.chatPage)
    } else {
      ChatInterface.errorSpace.innerHTML = "Fehler bei der Anmeldung!"
      ChatInterface.errorSpace.className = "text-danger"
    }
  }

  /*
   * An example of how startup script execution might work
   * When this page is opened, everything in this method's body is executed
   * Finally, the chatPage document is returned
   */
  def chatWindow() { 
    console.log("Starting receiver loop ..")
    var msg = "--- Welcome to the chat! ---"
    while (msg != ChatShared.ERROR_MSG) {
      ChatInterface.chatLog.innerHTML =
        div(msg) + ChatInterface.chatLog.innerHTML
      msg = ChatServer.readMessage()
    }
  }

  /*
   * This method is called when the send button on the chat page is pressed
   * It retrieves the entered message and hands it to the server
   */
  def messageEntered() {
    ChatServer writeMessage (ChatInterface.messageInputBox.value)
    ChatInterface.messageInputBox.value = ""
    ChatInterface.messageInputBox.focus()
  }
}

/*
 * Presentation section
 */
@Interface
object ChatInterface {

  // login area
  @Element
  lazy val errorSpace = div(
    cls := "error_hidden"
  ).render
  
  @Element
  lazy val nameInputBox = input(
    cls := "form-control",
    tpe := "text",
    placeholder := "Please type your name to start ..",
    autofocus
  ).render
  
  @Element
  lazy val startButton = button(
    cls := "btn btn-primary",
    onclick := ChatClient.nameEntered
  )(
    "Start"
  ).render

  @Page
  lazy val loginPage = document(
    "The Hyperflux chat",
    h1("Welcome to the Hyperflux chat"),
    errorSpace,
    div(
      nameInputBox,
      startButton
    )
  )

  // actual chat
  @Element
  lazy val chatLog = div(
    overflow := "scroll",
    height := 400
  ).render
  @Element
  lazy val messageInputBox = input(
    cls := "form-control",
    tpe := "text",
    placeholder := "Type your message here ..",
    autofocus
  ).render
  @Element
  lazy val sendButton = button(
    cls := "btn btn-primary",
    onclick := ChatClient.messageEntered 
  )(
    "Send"
  ).render

  @Page
  lazy val chatPage = document(
    "The Hyperflux chat",
    h1("The Hyperflux chat"),
    div(
      chatLog
    ),
    div(
      messageInputBox,
      sendButton
    ),
    script(ChatClient.chatWindow)
  )
  
  @Page
  lazy val main = loginPage
}