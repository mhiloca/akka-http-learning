package part2_lowlevelserver.lowlevelrestapi

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import part2_lowlevelserver.lowlevelrestapi.GuitarDB.CreateGuitar



object LowLevelRestApi extends App
  with LowLevelRequestHandler
  with GuitarStoreJsonProtocol
  with QueryHandler
{
  implicit val system: ActorSystem = ActorSystem("LowLevelRest")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /*
  *   - GET on localhost:8080/api/guitar => ALL the guitars in the stores
  *   - GET on localhost:8080/api/guitar?id=X fetches the guitar associated with id X
  *   - POST on localhost:8080/api/guitar => insert the guitar into the store
  * */

  /*
  * setup
  * */
  val guitarDb = system.actorOf(Props[GuitarDB], "LowLevelGuitarDB")

  val guitarList = List(
    Guitar("Fender", "Stratocaster"),
    Guitar("Gibson", "LesPaul"),
    Guitar("Martin", "LX1")
  )

  guitarList.foreach { guitar => guitarDb ! CreateGuitar(guitar)}

  /*
  * server code
  * */

  Http().bindAndHandleAsync(requestHandler, "localhost", 8080)

  /**
   * Exercise: enhance the Guitar case with a quantity field, by default 0
   * - GET to /api/guitar/inventory?inStock=true/false which returns the guitars in stock as a JSON
   * - POST to /api/guitar/inventory?id=X&quantity=Y which adds Y guitars to the stock for guitar with id X
   */
}
