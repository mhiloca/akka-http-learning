package part3_highlevelserver.highlevel_exercise
import spray.json._

trait PersonJsonProtocol extends DefaultJsonProtocol {

  implicit val personFormat: JsonFormat[Person] = jsonFormat2(Person)

}
