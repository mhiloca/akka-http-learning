package part3_highlevelserver.highlevel_exercise

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import spray.json._

import scala.util.{Failure, Success}



object PersonServerRoute extends PersonJsonProtocol {
  implicit val timeout: Timeout = Timeout(2 seconds)

  import QueryHandler._
  import PersonDb._
  import HighLevelExercise._
  import system.dispatcher

  val personServerRoute =
    pathPrefix("api" / "people") {
      get {
        (path(IntNumber) | parameter('pin.as[Int])) { pin =>
          complete((personDb ? FindPerson(pin)).mapTo[Person]
            .map(_.toJson.prettyPrint)
            .map(toHttpEntity)
          )
        } ~
          pathEndOrSingleSlash {
            complete((personDb ? FindAllPeople).mapTo[List[Person]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
            )
          }
      } ~
      (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
        val entityFutureStrict = request.entity.toStrict(2 seconds)
        val personFuture = entityFutureStrict.map(_.data.utf8String.parseJson.convertTo[Person])

        onComplete(personFuture) {
          case Success(person) =>
            complete((personDb ? InsertPerson(person))
              .mapTo[Person]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
            )
          case Failure(e) => complete(StatusCodes.BadRequest)
        }
      }
    }

}
