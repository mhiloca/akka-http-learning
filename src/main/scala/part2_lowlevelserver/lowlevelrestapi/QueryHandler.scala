package part2_lowlevelserver.lowlevelrestapi

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, RequestEntity, StatusCodes}
import akka.http.scaladsl.model.Uri.Query
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._


trait QueryHandler extends GuitarStoreJsonProtocol {
  import part2_lowlevelserver.lowlevelrestapi.GuitarDB._
  import part2_lowlevelserver.lowlevelrestapi.LowLevelRestApi._
  import system.dispatcher

  implicit val timeout: Timeout = Timeout(2 seconds)

  def getGuitar(query: Query): Future[HttpResponse] = {

    val guitarId = query.get("id").map(_.toInt) // Option[Int]

    guitarId match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(id: Int) =>
        val guitarFuture: Future[Option[Guitar]] = (guitarDb ? FindGuitar(id)).mapTo[Option[Guitar]]
        guitarFuture.map {
          case None => HttpResponse(StatusCodes.NotFound)
          case Some(guitar) =>
            HttpResponse(
              entity = HttpEntity(
                ContentTypes.`application/json`,
                guitar.toJson.prettyPrint
              )
            )
        }
    }
  }

  def addToInventory(query: Query): Future[HttpResponse] = {
    val guitarId = query.get("id").map(_.toInt)
    val guitarQuantity = query.get("quantity").map(_.toInt)

    val guitarToBeAddedOption = for {
      id <- guitarId
      quantity <- guitarQuantity
    } yield {
      val newGuitarFuture: Future[Option[Guitar]] = (guitarDb ? InsertGuitar(id, quantity)).mapTo[Option[Guitar]]
      newGuitarFuture.map(_ => HttpResponse(StatusCodes.OK))
    }
    guitarToBeAddedOption.getOrElse(Future(HttpResponse(StatusCodes.NotFound)))
  }

  def getAllGuitars: Future[HttpResponse] = {
    val guitarsFuture: Future[List[Guitar]] = (guitarDb ? FindAllGuitars).mapTo[List[Guitar]]
    guitarsFuture.map { guitars =>
      HttpResponse(
        entity = HttpEntity(
          ContentTypes.`application/json`,
          guitars.toJson.prettyPrint
        )
      )
    }
  }

  def getStockGuitars(query: Query): Future[HttpResponse] = {
    query.get("inStock").map(_.toBoolean) match {
      case None => Future(HttpResponse(StatusCodes.BadRequest))
      case Some(inStock) =>
        val guitarsStockFuture: Future[List[Guitar]] = (guitarDb ? FindGuitarInStock(inStock)).mapTo[List[Guitar]]
        guitarsStockFuture.map { guitars =>
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          )
        }
    }

  }

  def insertGuitar(guitar: Guitar): Future[HttpResponse] = {
      val guitarCreatedFuture: Future[GuitarCreated] = (guitarDb ? CreateGuitar(guitar)).mapTo[GuitarCreated]

      guitarCreatedFuture.map { _ =>
        HttpResponse(StatusCodes.OK)
      }
    }

}

