package part3_highlevelserver.highlevel_example

import akka.actor.{Actor, ActorLogging}
import part3_highlevelserver.highlevel_example.GuitarDB.{FetchGuitar, FetchStock}

class GuitarDB extends Actor with ActorLogging {
  import GuitarDB._

  var guitars: Map[Int, Guitar] = Map()
  var currentGuitarId: Int = 0

  override def receive: Receive = {
    case CreateGuitar(guitar) =>
      log.info(s"Inserting into stock guitar: ${guitar.make} - ${guitar.model}")
      guitars += (currentGuitarId -> guitar)
      currentGuitarId += 1

    case FetchGuitar(guitarId) =>
      log.info(s"Retrieving guitar $guitarId")
      sender() ! guitars.get(guitarId)

    case FetchStock(inStock) =>
      if (inStock) {
        log.info("Retrieving guitars in stock")
        sender() ! guitars.values.filter(guitar => guitar.quantity > 0)
      } else {
        log.info("Retrieving guitars not in store")
        sender() ! guitars.values.filter(guitar => guitar.quantity <= 0)
      }

    case FetchAllGuitars =>
      log.info(s"Retrieving all the guitars")
      sender() ! guitars.values.toList
  }

}

object GuitarDB {

  case class CreateGuitar(guitar: Guitar)
  case class FetchGuitar(guitarId: Int)
  case class FetchStock(inStock: Boolean)
  case object FetchAllGuitars

}
