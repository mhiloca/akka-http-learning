package part2_lowlevelserver

import spray.json._

case class Person(name: String, age: Int)

trait PersonJsonProtocol extends DefaultJsonProtocol {

  implicit val personFormat: JsonFormat[Person] = jsonFormat2(Person)

}

object JsonMarshalling extends App with PersonJsonProtocol {

  // marshalling
  val mhiloca = Person("Mhiloca", 41)
  println(mhiloca.toJson.prettyPrint)

  // unmarshalling
  val personJsonString =
    """
      |{
      |  "age": 41,
      |  "name": "Mhiloca"
      |}
      |""".stripMargin

  println(personJsonString.parseJson.convertTo[Person])
}
