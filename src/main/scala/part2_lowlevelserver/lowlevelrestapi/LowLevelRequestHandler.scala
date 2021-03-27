package part2_lowlevelserver.lowlevelrestapi

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._



trait LowLevelRequestHandler extends GuitarStoreJsonProtocol with QueryHandler {
  import part2_lowlevelserver.lowlevelrestapi.LowLevelRestApi._
  import system.dispatcher

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/guitar"), _, _, _) =>
      val query = uri.query() // query object <=> Map[String, String]
      if (query.isEmpty) getAllGuitars else getGuitar(query)

    case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/inventory"), _, _, _) =>
      val query = uri.query()
      getStockGuitars(query)

    case HttpRequest(HttpMethods.POST, uri@Uri.Path("/api/inventory"), _, _, _) =>
      val query = uri.query()
      addToInventory(query)

    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitar"), _, entity, _) =>
      val entityFutureStrict = entity.toStrict( 3 seconds)

      entityFutureStrict.flatMap { strictEntity =>
        val guitar = strictEntity
          .data
          .utf8String
          .parseJson
          .convertTo[Guitar]

        insertGuitar(guitar)
      }

    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(StatusCodes.NotFound))
  }
}