package part4_client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import part4_client.PaymentSystemDomain.PaymentRequest
import spray.json._

import java.util.UUID
import scala.util.{Failure, Success}

object RequestLevel extends App with PaymentJsonProtocol {

  implicit val system: ActorSystem = ActorSystem("RequestLevel")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

//  val responseFuture = Http().singleRequest(HttpRequest(uri= "http://www.google.com"))

 /* responseFuture.onComplete {
    case Success(response) =>
      //VERY IMPORTANT
      response.discardEntityBytes()
      println(s"The request was successful and returned: $response")
    case Failure(exception) =>
      println(s"The request failed because of: $exception")
  }*/

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
      uri = Uri("http://localhost:8080/api/payments"),
      entity = HttpEntity(
        ContentTypes.`application/json`,
        paymentRequest.toJson.prettyPrint
      )
    )
  )

  Source(serverHttpRequests)
    .mapAsync(10)(request => Http().singleRequest(request))
    .runForeach(println)
}
