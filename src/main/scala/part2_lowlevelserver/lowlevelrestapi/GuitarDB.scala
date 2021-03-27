package part2_lowlevelserver.lowlevelrestapi

import akka.actor.{Actor, ActorLogging}

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)
  case class InsertGuitar(id: Int, quantity: Int)
  case class GuitarCreated(id: Int)
  case class FindGuitar(id: Int)
  case class FindGuitarInStock(inStock: Boolean)
  case object FindAllGuitars
}

class GuitarDB extends Actor with ActorLogging {

  import GuitarDB._

  var guitars: Map[Int, Guitar] = Map()
  var currentGuitarId = 0

  override def receive: Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars")
      sender() ! guitars.values.toList

    case FindGuitar(id) =>
      log.info(s"Searching guitar via id: $id")
      sender() ! guitars.get(id)

    case CreateGuitar(guitar) =>
      log.info(s"Adding guitar $guitar with id $currentGuitarId")
      val newGuitar = Guitar(guitar.make, guitar.model, guitar.quantity)
      guitars = guitars + (currentGuitarId -> newGuitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1

    case InsertGuitar(id, quantity) =>
      log.info(s"Trying to add $quantity items for guitar $id")
      val guitar = guitars.get(id)
      val newGuitar: Option[Guitar] = guitar.map {
        case Guitar(make, model, q) => Guitar(make, model, q + quantity)
      }

      newGuitar.foreach(guitar => guitars = guitars + (id -> guitar))
      sender() ! newGuitar

    case FindGuitarInStock(inStock) =>
      if (inStock)
        sender() ! guitars.values.filter(_.quantity > 0)
      else
        sender() ! guitars.values.filter(_.quantity <= 0)
  }
}

