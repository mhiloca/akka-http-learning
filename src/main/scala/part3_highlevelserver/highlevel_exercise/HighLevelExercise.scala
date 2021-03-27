package part3_highlevelserver.highlevel_exercise

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.language.postfixOps
import scala.concurrent.duration._
import part3_highlevelserver.highlevel_exercise.PersonDb._

object HighLevelExercise extends App with PersonJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("HighLevelExercise")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(3 seconds)

  import PersonServerRoute._


  /**
   * Exercise
   *
   * - GET /api/people: retrieve ALL the people you have registered
   * - GET /api/people/pin: retrieve the person with that PIN, return as JSON
   * - GET /api/people?pin=X (same)
   * - POST /api/people with a JSON payload denoting a Person, add that person to your database
   */

  val personDb = system.actorOf(Props[PersonDb], "personDb")

  var people = List(
    Person(1, "Alice"),
    Person(2, "Charlie"),
    Person(3, "Bob")
  )

  people foreach {person => personDb ! InsertPerson(person)}

  Http().bindAndHandle(personServerRoute, "localhost", 8080)

}
