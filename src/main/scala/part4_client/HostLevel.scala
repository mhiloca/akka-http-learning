package part4_client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import part4_client.PaymentSystemDomain.PaymentRequest
import spray.json._

import java.util.UUID
import scala.util.{Failure, Success}

object HostLevel extends App with PaymentJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("HostLeve")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val poolFlow = Http().cachedHostConnectionPool[Int]("www.google.com")

/*  Source(1 to 10)
    .map(i => (HttpRequest(), i))
    .via(poolFlow)
    .map {
      case (Success(response), value) =>
        // VERY IMPORTANT
        response.discardEntityBytes()
        s"Request $value has received:  $response"
      case (Failure(ex), value) =>
        s"Request $value has failed: $ex"
    }
    .runWith(Sink.foreach[String](println))*/

  val creditCards = List(
    CreditCard("3423-2382-2938-2362", "424", "tx-test-account"),
    CreditCard("1234-1234-1234-1234", "892", "tx-daniel-account"),
    CreditCard("3423-2382-4321-4321", "321", "tx-great-account"),
  )

  val paymentRequests = creditCards.map {
    creditCard => PaymentRequest(creditCard, "rtjvm-store-account", 99.0)
  }

  val serverHttpRequests = paymentRequests.map(paymentRequest =>
    (
      HttpRequest(
        HttpMethods.POST,
        uri = Uri("/api/payments"),
        entity = HttpEntity(
          ContentTypes.`application/json`,
          paymentRequest.toJson.prettyPrint
        )
      ),
      UUID.randomUUID().toString
    )
  )

  Source(serverHttpRequests)
    .via(Http().cachedHostConnectionPool[String]("localhost", 8080))
    .runForeach {
      case (Success(response@HttpResponse(StatusCodes.Forbidden, _, _, _)), id) =>
        println(s"The order id $id was not allowed to proceed: ${response.entity}")
      case (Success(response), id) =>
        println(s"The order id $id, was sucessful and returned the response: ${response.entity}")
      case (Failure(e), id) =>
        println(s"The order id $id has failed because of $e")
    }

  // should be used for how volume and low latency

}
