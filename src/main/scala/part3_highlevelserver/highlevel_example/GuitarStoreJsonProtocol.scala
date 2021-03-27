package part3_highlevelserver.highlevel_example

import spray.json._

trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {

  implicit val guitarFormat: JsonFormat[Guitar] = jsonFormat3(Guitar)

}
