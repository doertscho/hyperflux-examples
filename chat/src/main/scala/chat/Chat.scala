package chat

import scala.collection.mutable.HashMap
import scala.concurrent.Channel
import scalatags.JsDom._
import scalatags.JsDom.all._
import scala.async.Async.async
import scala.concurrent.ExecutionContext.Implicits.global
import hyperflux.annotation._
import hyperflux.SessionID
import hyperflux.routing_helpers._
import hyperflux.session_helpers._
import hyperflux.template_helpers._

/*
 * Server section
 */

@Server("localhost", 24105)
object ChatServer {

  var users = new HashMap[SessionID, (String, Channel[String])]

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

  /*
   * Main entry point: Shows the login page
   * As no client-side scripts have to be executed on startup, there are no
   * further method calls
   */
  @Page
  def main(): Document = loginPage

  /*
   * This method is called when the submit button on the login page is pressed
   * It retrieves the entered name and hands it to the server
   * If the login was successful, it redirects the client to the actual chat
   */
  def nameEntered() {
    val name = nameInputBox.value
    if (ChatServer sayHello name) {
      redirect(chatWindow)
    } else {
      errorSpace.innerHTML = "Fehler bei der Anmeldung!"
      errorSpace.className = "error"
    }
  }

  /*
   * An example of how startup script execution might work
   * When this page is opened, everything in this method's body is executed
   * Finally, the chatPage document is returned
   */
  @Page
  def chatWindow(): Document = {
    // spawn message receiver "thread"
    /*val receiveLoop = async {
      var msg = new String()
      do {
        msg = ChatServer readMessage()
        chatLog.innerHTML += br.render + msg
      } while (msg != ChatServer.ERROR_MSG)
    }*/
    chatPage
  }

  /*
   * This method is called when the send button on the chat page is pressed
   * It retrieves the entered message and hands it to the server
   */
  def messageEntered() {
    val msg = messageInputBox.value
    ChatServer writeMessage msg
  }

  /*
   * Presentation section
   */

  // login area
  lazy val errorSpace = div(
    cls := "error_hidden"
  ) render
  lazy val nameInputBox = input(
    tpe := "text",
    placeholder := "Please type your name to start .."
  ) render
  lazy val startButton = button(
    value := "Start",
    onclick := { () => nameEntered() }
  ) render

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
  lazy val chatLog = div(
    overflow := "scroll",
    height := 400
  ) render
  lazy val messageInputBox = input(
    tpe := "text",
    placeholder := "Type your message here .."
  ) render
  lazy val sendButton = button(
    value := "Send",
    onclick := { () => messageEntered() }
  ) render

  lazy val chatPage = document(
    "The Hyperflux chat",
    h1("The Hyperflux chat"),
    div(
      chatLog
    ),
    div(
      messageInputBox,
      sendButton
    )
  )
}
