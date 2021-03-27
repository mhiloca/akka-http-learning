package part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.CompactByteString
import akka.http.scaladsl.server.Directives._
import scala.concurrent.duration._

object WebsocketsDemo extends App {

  implicit val system: ActorSystem = ActorSystem("WebsocketsDemo")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  /*
   Message type:
    - TextMessage
        # text based msg that can be sent, either by the frontend or by the backend
        # the TextMessage object is a wrapper over a Source, and this Source will
          emit strings
          the reason why it's because these messages can be of any size, thus TextMessage
          is a wrapper over a stream, an Akka Stream
    - BinaryMessage
        it can contain anything (sound, videos, any format)
        it is a wrapper over a Source of bytes

    We can handle messages in route as a directive - handleWebSocketMessages()
    this wrappers over a Flow[Message, Message, Any]
      - the best way to address this flow is by creating a Partial Function
        where you can address both TextMessage and BinaryMessage
   */

  val textMessage = TextMessage(Source.single("Hello via a text message"))
  val binaryMessage = BinaryMessage(Source.single(CompactByteString("Hello via a binary message")))


  val html =
    """
      |<html lang="en">
      |<head>
      |    <meta charset="UTF-8">
      |    <title>Websockets</title>
      |    <script>
      |
      |        var exampleSocket = new WebSocket("ws://localhost:8080/greeter");
      |        console.log("Starting websocket...");
      |
      |        exampleSocket.onmessage = function(event) {
      |            var newChild = document.createElement("div");
      |            newChild.innerText = event.data;
      |
      |            document.getElementById("1").appendChild(newChild);
      |            exampleSocket.send("Socket says: Hello, server!");
      |        };
      |
      |        exampleSocket.onopen = function(event) {
      |            exampleSocket.send("Socket seems to be open...");
      |        };
      |
      |
      |    </script>
      |</head>
      |<body>
      |    Starting Websocket...
      |    <div id="1">
      |    </div>
      |</body>
      |</html>
      |""".stripMargin

  def webSocketFlow: Flow[Message, Message, Any] = Flow[Message].map {
    case tm: TextMessage =>
      TextMessage(Source.single("Serve says back: ") ++ tm.textStream ++ Source.single("!"))
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      TextMessage(Source.single("Server received a binary message..."))
  }
  val webSocketRoute =
    (pathEndOrSingleSlash & get) {
      complete(HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        html
      ))
    } ~
    path("greeter") {
      handleWebSocketMessages(socialFlow)
    }


  Http().bindAndHandle(webSocketRoute, "localhost", 8080)

  case class SocialPost(owner: String, content: String)

  val socialFeed = Source(
    List(
      SocialPost("Martin", "Scala3 has been announced!"),
      SocialPost("Daniel", "A new Rock the JVM course is open"),
      SocialPost("Martin", "I killed Java")
    )
  )

  val socialMessages = socialFeed
    .throttle(1, 2 seconds)
    .map(socialPost => TextMessage(s"${socialPost.owner} says: ${socialPost.content}"))

  val socialFlow: Flow[Message, Message, Any] = Flow.fromSinkAndSource(
    Sink.foreach[Message](println),
    socialMessages
  )
}
