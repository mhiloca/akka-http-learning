package part3_highlevelserver

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethod, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, post}
import akka.stream.ActorMaterializer

object DirectivesBreakdown extends App {

  implicit val system: ActorSystem = ActorSystem("DirectivesBreakdown")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher
  import akka.http.scaladsl.server.Directives._

  /**
   * Type # 1: filtering directives
   */

  val simpleHttpMethodRoute =
    post { // equivalent for get, put, patch, delete, head, options
      complete(StatusCodes.Forbidden)
    }

  val simplePathRoute =
    path("about") {
      complete(
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h1>Hello from the about page!</h1>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    }

  val complexPathRoute =
    path("api" / "myEndpoint") { // /api/myEndpoint
      complete(StatusCodes.OK)
    }

  val dontConfuse =
    path("api/Endpoint") { //url encodes this path -> localhost:8000/api%2FEndpoint
      complete(StatusCodes.OK)
    }

  val pathEndRoute =
    pathEndOrSingleSlash { // localhost:8000 OR localhost:8000/
      complete(StatusCodes.OK)
    }

//  Http().bindAndHandle(complexPathRoute, "localhost", 8000)

  /**
   * Type 32: exrtact directives
   */

  // GET on /api/item/42
  val pathExtractionRoute = {
    path("api" / "item" / IntNumber) { (itemNumber: Int) =>
      //other directions
      println(s"I've got a number in my path $itemNumber")
      complete(StatusCodes.OK)
    }
  }

  val pathMultiExtractRoute = {
    path("api" / "order" / IntNumber / IntNumber) { (id, inventory) =>
      println(s"I've got TWO numbers in my path: $id and $inventory")
      complete(StatusCodes.OK)


    }
  }

//  Http().bindAndHandle(pathMultiExtractRoute, "localhost", 8080)

  //queries
  val queryParamExtractionRoute = {
    // /api/item?id=45
    path("api" / "item") {
      parameter('id.as[Int]) { (itemId: Int) => // 'id
        println(s"I've extracted the ID as $itemId")
        complete(StatusCodes.OK)
      }

    }
  }

  // extract the whole request
  val extractRequestRoute = {
    path("controlEndpoint") {
      extractRequest { httpRequest: HttpRequest =>
        extractLog { (log: LoggingAdapter) =>
          log.info(s"I got the method: $httpRequest")
          complete(StatusCodes.OK)
        }
      }
    }
  }

//  Http().bindAndHandle(extractRequestRoute, "localhost", 8080)

  /**
   * Type #3: composite directives
   */

  val simpleNestedRoute =
    path("api" / "item") {
      get {
        complete(StatusCodes.OK)
      }
    }

  val compactSimpleNestedRoute = (path("api" / "item") & get) {
    complete(StatusCodes.OK)
  }

  val compactExtractRequestRoute =
    (path("controlEndpoint") & extractRequest & extractLog) { (request, log) =>
      log.info(s"I got the method: ${request.method}")
      complete(StatusCodes.OK)
    }

//  Http().bindAndHandle(compactExtractRequestRoute, "localhost", 8080)

  // /about and /aboutUs
  val repeatedRoute =
    path("about") {
      complete(StatusCodes.OK)
    } ~
    path("aboutUs") {
      complete(StatusCodes.OK)
    }

  val dryRoute = (path("about") | path("aboutUs")) {
    complete(StatusCodes.OK)
  }

  // yourblog.com/42 NAD  yourblog.com?postId=42

  val blogByIdRoute =
    path(IntNumber) { (blogpostId: Int) =>
      // complex server logic
      complete(StatusCodes.OK)
    }

  val blogByQueryParamRoute =
    parameter('postId.as[Int]) {(blogpostId: Int) =>
      // the SAME server logic
      complete(StatusCodes.OK)
    }

   val combinedBlogByIdRoute = // they must extract the same type of value
     (path(IntNumber) | parameter('postId.as[Int])) { (blogspotId: Int) =>
       // your original server logic
       complete(StatusCodes.OK)
     }

  /**
   * Type #4: "actionable" directives
   */

  val completeOkRoute = complete(StatusCodes.OK)

  val failedRoute =
    path("notSupported") {
      failWith(new RuntimeException("Unsupported")) // completes with HTTP 500
    }

  val routeWithRejection =
    path("home") {
      reject
    } ~
    path("index") {
      completeOkRoute
    }

  /**
   * Exercise
   */

  val getOrPutPath =
    path("api" / "myEndpoint")  {
      get {
        completeOkRoute
      } ~
      put {
        complete(StatusCodes.Forbidden)
      }
    }

  Http().bindAndHandle(getOrPutPath, "localhost", 8080)
}
