package mini

import hyperflux.annotation._
import hyperflux.template_helpers._
import scala.scalajs.js
import org.scalajs.dom.alert
import org.scalajs.dom.console
import scalatags.JsDom.all._
 
/*
 * Server section
 */ 

@Server("http://localhost", 24105)
object MiniAppServer {

  private val msg = "Secret message"

  def getMessage(): String = msg
  
  var num = 0
  def getMessageNo(clientNo: Int): String = {
    num = num + 1
    s"Message #$clientNo/$num: l${"ol" * clientNo}"
  }
}

/*
 * Client logic section
 */

@Client
object MiniAppClient {
  
  def alertMessage() = alert(MiniAppServer.getMessage())

  var num = 0
  def addMessageToDOM() {
    num = num + 1
    val msg = MiniAppServer.getMessageNo(num)
    MiniAppInterface.messageSpace.innerHTML =
      div(msg) + MiniAppInterface.messageSpace.innerHTML
  }
  
  def resetNum() { 
    console.log("resetting local counter")
    num = 0
  }
}


/*
 * Interface section
 */
@Interface
object MiniAppInterface {
  
  @Element
  lazy val alertButton = button(
    cls := "btn btn-primary",
    marginRight := 10,
    onclick := MiniAppClient.alertMessage
  )(
    "Show an alert with a message from the server"
  ).render
  
  @Element
  lazy val addButton = button(
    cls := "btn btn-primary",
    marginRight := 10,
    onclick := MiniAppClient.addMessageToDOM
  )(
    "Add another message from the server to the DOM"
  ).render
  
  @Element
  lazy val resetButton = button(
    cls := "btn btn-primary",
    onclick := MiniAppClient.resetNum
  )(
    "Reset"
  ).render
  
  @Element
  lazy val messageSpace = div().render
  
  // main page
  @Page
  lazy val main = document(
    "The Hyperflux MiniApp",
    h1(marginBottom := 30, textAlign.center)("Welcome to the MiniApp"),
    div(marginBottom := 10)(alertButton, addButton, resetButton), 
    div(messageSpace)
  ).render
}