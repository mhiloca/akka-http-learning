package part3_highlevelserver.highlevel_example

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import part3_highlevelserver.highlevel_example.GuitarDB._
import spray.json._

import scala.concurrent.Future


object HighLevelExample extends App with GuitarStoreJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("HighLevelExample")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(3 seconds)

  import system.dispatcher


  /*
    GET /api/guitar fetches all the guitars
    GET /api/guitar?id=X fetches the guitar with id X
    GET /api/guitar/X fetches guitar with id X
    GET /api/guitar/inventory?inStock=true
   */

  val guitarDb = system.actorOf(Props[GuitarDB], "guitarDb")

  val guitarList = List(
    Guitar("Fender", "Stratocaster"),
    Guitar("Gibson", "LesPaul"),
    Guitar("Martin", "LX1")
  )

  guitarList foreach { guitar =>
    guitarDb ! CreateGuitar(guitar)
  }

  val guitarServerRoute =
    (path("api" / "guitar" / IntNumber) | (path("api" / "guitar") & parameter('id.as[Int])) & get) {guitarId: Int =>
      val guitarFuture: Future[Option[Guitar]] = (guitarDb ? FetchGuitar(guitarId)).mapTo[Option[Guitar]]
      val entityFuture = guitarFuture.map { guitar =>
        HttpEntity(
          ContentTypes.`application/json`,
          guitar.toJson.prettyPrint
        )
      }
      complete(entityFuture)
    } ~
    (path("api" / "guitar") & get) {
      val guitarsFuture: Future[List[Guitar]] = (guitarDb ? FetchAllGuitars).mapTo[List[Guitar]]
      val entityFuture = guitarsFuture.map { guitars =>
        HttpEntity(
          ContentTypes.`application/json`,
          guitars.toJson.prettyPrint
        )
      }
      complete(entityFuture)
    } ~
    (path("api" / "guitar" / "inventory") & parameter('inStock.as[Boolean])) { inStock =>
      val guitarsFuture: Future[List[Guitar]] = (guitarDb ? FetchStock(inStock)).mapTo[List[Guitar]]
      val entityFuture = guitarsFuture.map { guitars =>
        HttpEntity(
          ContentTypes.`application/json`,
          guitars.toJson.prettyPrint
        )
      }
      complete(entityFuture)
    }

  def toHttpEntity(payload: String): HttpEntity.Strict =
    HttpEntity(ContentTypes.`application/json`, payload)

  val simplifiedGuitarServerRoute =
    (pathPrefix("api" / "guitar") & get) {
      (path("inventory") & parameter('inStock.as[Boolean])) { inStock: Boolean =>
        complete((guitarDb ? FetchStock(inStock))
          .mapTo[List[Guitar]]
          .map(_.toJson.prettyPrint)
          .map(toHttpEntity))
      } ~
      (path(IntNumber) | parameter('id.as[Int])) { guitarId: Int =>
        complete((guitarDb ? FetchGuitar(guitarId))
          .mapTo[Option[Guitar]]
          .map(_.toJson.prettyPrint)
          .map(toHttpEntity))
      } ~
        pathEndOrSingleSlash {
        complete((guitarDb ? FetchAllGuitars)
          .mapTo[List[Guitar]]
          .map(_.toJson.prettyPrint)
          .map(toHttpEntity))
      }
    }

  Http().bindAndHandle(simplifiedGuitarServerRoute, "localhost", 8080)

}
