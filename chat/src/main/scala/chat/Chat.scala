package chat

import scala.collection.mutable.HashMap
import scala.concurrent.Channel
import scala.async.Async.async
import scalatags.JsDom._
import scalatags.JsDom.all._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSApp
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
    if (users.valuesIterator contains name) {
      false
    } else {
      users += (SID -> (name, new Channel[String]))
      println("New user '" + name + "' with SID " + SID)
      true
    }
  }

  def writeMessage(msg: String) {
    println("New message from " + SID + ": " + msg)
    // this lookup serves two purposes: it checks whether the sending client
    // is registered, and if it is, it retrieves the matching user name
    users get SID match {
      case Some((name, _)) => {
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

  val ERROR_MSG = "ERR/NOT_REGISTERED"
  def readMessage(): String = {
    users get SID match {
      case Some((_, chan)) => chan read
      case _ => ERROR_MSG
    }
  }
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
    val name = ChatInterface.nameInputBox.value
    if (ChatServer sayHello name) {
      redirect(ChatInterface.chatPage)
    } else {
      ChatInterface.errorSpace.innerHTML = "Fehler bei der Anmeldung!"
      ChatInterface.errorSpace.className = "error"
    }
  }

  /*
   * An example of how startup script execution might work
   * When this page is opened, everything in this method's body is executed
   * Finally, the chatPage document is returned
   */
  def chatWindow() {
    // spawn message receiver "thread"
    var msg = ""
    do {
      msg = ChatServer.readMessage()
      ChatInterface.chatLog.innerHTML += br.render + msg
    } while (msg != ChatServer.ERROR_MSG)
  }

  /*
   * This method is called when the send button on the chat page is pressed
   * It retrieves the entered message and hands it to the server
   */
  def messageEntered() {
    val msg = ChatInterface.messageInputBox.value
    ChatServer writeMessage msg
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
    tpe := "text",
    placeholder := "Please type your name to start .."
  ).render
  
  @Element
  lazy val startButton = button(
    value := "Start",
    onclick := ChatClient.nameEntered
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
    tpe := "text",
    placeholder := "Type your message here .."
  ).render
  @Element
  lazy val sendButton = button(
    value := "Send",
    onclick := ChatClient.messageEntered
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
    div(onload := ChatClient.chatWindow)
  )
}