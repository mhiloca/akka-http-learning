package part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.javadsl.server.MethodRejection
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MissingQueryParamRejection, Rejection, RejectionHandler}

object HandlingRejection extends App {

  implicit val system: ActorSystem = ActorSystem("HandlingRejection")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val simpleRoute =
    path("api" / "myEndpoint") {
      get {
        complete(StatusCodes.OK)
      } ~
      parameter('id) { _ =>
        complete(StatusCodes.OK)
      }
    }

  // Rejection handlers
  val badRequestHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"I have encountered rejections: $rejections")
    Some(complete(StatusCodes.BadRequest))
  }

  val forbiddenHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    println(s"I have encountered rejections: $rejections")
    Some(complete(StatusCodes.Forbidden))
  }

  val simpleRouterWithHandlers =
    handleRejections(badRequestHandler) { // handle rejections from the TOP level
      //define server logic inside
      path("api" / "myEndpoint") {
        get {
          complete(StatusCodes.OK)
        } ~
          post {
            handleRejections(forbiddenHandler) { // handle rejections WITHIN
              parameter('myParam) { _ =>
                complete(StatusCodes.OK)
              }
            }
          }
      }
    }

//  RejectionHandler.default
//  Http().bindAndHandle(simpleRouterWithHandlers, "localhost", 8080)

  // list(method rejection, query param rejection) <-> (query param rejection) = order matters
  /*
  there is a rejection handling akka internal priority, therefore it's
  better to organize your own custom prioryty through assingning various
  handles, instead of gathering all the cases in the same handle
  So multiple handles with single cases in them, it is very different from
  a single handle with multiple cases in it
   */
  implicit val customRejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case m: MissingQueryParamRejection =>
        println(s"I got a query param rejection: $m")
        complete("Rejected query param")
    }
    .handle {
      case m: MethodRejection =>
        println(s"I got a method rejection: $m")
        complete("Rejected Method")

    }
    .result()

  // sealing a route:

  Http().bindAndHandle(simpleRoute, "localhost", 8080)
}
