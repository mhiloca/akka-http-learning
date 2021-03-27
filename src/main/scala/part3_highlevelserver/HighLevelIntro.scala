package part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer


object HighLevelIntro extends App {

  implicit val system: ActorSystem = ActorSystem("HighLevelIntro")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  // directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoute: Route =
    path("home") { // DIRECTIVE
      complete(StatusCodes.OK) // DIRECTIVE
    }

  val pathGetRoute: Route =
    path("home") { // if the request hits path "/home"
      get { // if the request is a GET
        complete(StatusCodes.OK)
      }
    }

  // chaining directives with ~
  val chainedRoute: Route = {
    path("myEndPoint") {
      get {
        complete(StatusCodes.OK)
      } /* VERY IMPORTANT -------------------> */~
      post {
        complete(StatusCodes.Forbidden)
      }
    } ~
      path("home") {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
              |<html>
              | <body>
              |   <h1> Hello from the high level HTTP!</h1>
              | </body>
              |</html>
              |""".stripMargin
          )
        )
      }
  } // routing tree

  Http().bindAndHandle(chainedRoute, "localhost", 8080)


}
