package part3_highlevelserver.highlevel_exercise

import akka.actor.{Actor, ActorLogging}

class PersonDb extends Actor with ActorLogging {
  import PersonDb._

  var currentPersonId: Int = 0
  var people: Map[Int, Person] = Map()

  def retrievePersonIdFromPin(pin: Int): Person = {
    val personId = people.filter(_._2.pin == pin).keys.head
    people(personId)
  }

  override def receive: Receive = {

    case InsertPerson(person) =>
      log.info(s"Inserting person to db")
      people += currentPersonId -> person
      currentPersonId += 1
      sender() ! person

    case FindPerson(pin) =>
      log.info(s"Retrieving person with id: $pin")
      sender() ! retrievePersonIdFromPin(pin)

    case FindAllPeople =>
      log.info("Retrieving all people from the DB")
      sender() ! people.values.toList
  }

}

object PersonDb {
  case class InsertPerson(person: Person)
  case class FindPerson(pin: Int)
  case object FindAllPeople
}
