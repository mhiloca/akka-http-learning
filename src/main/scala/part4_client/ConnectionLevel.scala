package part4_client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success}
import spray.json._

object ConnectionLevel extends App with PaymentJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("ConnectionLevel")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher
  import PaymentSystemDomain._

  val connectionFlow = Http().outgoingConnection("www.google.com")

  def oneOfRequest(request: HttpRequest) = {
    Source.single(request).via(connectionFlow).runWith(Sink.head)
  }

 /* oneOfRequest(HttpRequest()).onComplete {
    case Success(response) => println(s"Got successful response: $response")
    case Failure(exception) => println(s"Sending the request failed: $exception")
  }*/

  /*
    small payments system
   */

  val creditCards = List(
    CreditCard("3423-2382-2938-2362", "424", "tx-test-account"),
    CreditCard("1234-1234-1234-1234", "892", "tx-daniel-account"),
    CreditCard("3423-2382-4321-4321", "321", "tx-great-account"),
  )

  val paymentRequests = creditCards.map {
    creditCard => PaymentRequest(creditCard, "rtjvm-store-account", 99.0)
  }

  val serverHttpRequests = paymentRequests.map(paymentRequest =>
    HttpRequest(
      HttpMethods.POST,
      uri = Uri("/api/payments"),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        paymentRequest.toJson.prettyPrint
      )
    )
  )

  Source(serverHttpRequests)
    .via(Http().outgoingConnection("localhost", 8080))
    .runWith(Sink.foreach[HttpResponse](println))

}
