package part2_lowlevelserver.lowlevelrestapi


//step 1
import spray.json._

// step 2
trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {

  // step 3
  implicit val guitarFormat: JsonFormat[Guitar] = jsonFormat3(Guitar)

}