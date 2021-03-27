package part3_highlevelserver.marshalling_json


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait PlayerJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val playerFormat = jsonFormat3(Player)

}
