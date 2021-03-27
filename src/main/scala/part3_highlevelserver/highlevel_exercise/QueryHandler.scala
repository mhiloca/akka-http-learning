package part3_highlevelserver.highlevel_exercise

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity

object QueryHandler {

  def toHttpEntity(payload: String) = HttpEntity(ContentTypes.`application/json`, payload)


}
