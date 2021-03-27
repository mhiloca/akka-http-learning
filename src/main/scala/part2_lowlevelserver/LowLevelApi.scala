package part2_lowlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._


object LowLevelApi extends App {

  implicit val system: ActorSystem = ActorSystem("LowLevelApi")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val serverSource = Http().bind("localhost", 8000)
  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
    println(s"Accepted incoming connection from: ${connection.remoteAddress}")
  }

 /* val serverBindingFuture = serverSource.to(connectionSink).run()
  serverBindingFuture.onComplete {
    case Success(binding) =>
      println("Server binding successful")
      binding.terminate(2 seconds)
    case Failure(e) => println(s"Server binding failed: $e")
  }*/

  /*
    Method 1:  synchronously serve HTTP responses
   */

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(
        StatusCodes.OK, // HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h1>Hello from Akka HTTP!<h1>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound, // 404
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h2>OOPS! The resource can't be found.</h2>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
  }

  val httpSinkConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithSyncHandler(requestHandler)
  }

//  Http().bind("localhost", 8080).runWith(httpSinkConnectionHandler)
//  Http().bindAndHandleSync(requestHandler, "localhost", 8000)


  /*
    Method 2: serve back Http responses ASYNCHRONOUSLY
   */

  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) => // method, URI, HTTP headers, content, protocol (HTTP1.1/HTTP2.0)
      Future(HttpResponse(
        StatusCodes.OK, // HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h1>Hello from Akka HTTP!<h1>
            |   <h2>this is running on another thread</h2>
            | </body>
            |</html>
            |""".stripMargin
        )
      ))
    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(
        StatusCodes.NotFound, // 404
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h2>OOPS! The resource can't be found.</h2>
            | </body>
            |</html>
            |""".stripMargin
        )
      ))
  }

  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithAsyncHandler(asyncRequestHandler)
  }
/*  // stream-based "manual" version
  Http().bind("localhost", 8081).runWith(httpAsyncConnectionHandler)

  // shorthand version
  Http().bindAndHandleAsync(asyncRequestHandler, "localhost", 8081)*/

  /*
    Method 3: async via Akka Streams
   */

  val streamsBasedRequestHandler: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) => // method, URI, HTTP headers, content, protocol (HTTP1.1/HTTP2.0)
      HttpResponse(
        StatusCodes.OK, // HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h1>Hello from Akka HTTP!<h1>
            |   <h2>this is running on another thread</h2>
            |   <h2>we are using an Akka Streams flow for this!</h2>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound, // 404
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h2>OOPS! The resource can't be found.</h2>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
  }

  // manual version
/*  Http().bind("localhost", 8082).runForeach { connection =>
    connection.handleWith(streamsBasedRequestHandler)
  }*/

//  Http().bindAndHandle(streamsBasedRequestHandler, "localhost", 8082)

  /**
   * Exercise: Create your own http server running on localhost on 8388m
   * which replies with the following:
   *  - with a welcome message on the "front door" localhost:8388
   *  - with a proper HTML on localhost/about
   *  - with a 404 message otherwise
   */

  val httpServerHandler: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/about"), _, _, _) =>
      HttpResponse(
        // status code OK (200) is default
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h1>This is an exercise</h1>
            |   <h2>To check if we understood how to create a low-level server</h2>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    case HttpRequest(HttpMethods.GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(
        // status OK (200) is default
        entity = HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          "welcome"
        )
      )
    // path /search redirects to some other part of our website/webapp/microservice
    case HttpRequest(HttpMethods.GET, Uri.Path("/search"), _, _, _) =>
      HttpResponse(
        StatusCodes.Found,
        headers = List(Location("http://google.com"))
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          "Oops! Resource not found"
        )
      )
  }

  val bindingFuture = Http().bindAndHandle(httpServerHandler, "localhost", 8388)

  // shutdown the server
  bindingFuture.flatMap(binding => binding.unbind())
    .onComplete(_ => system.terminate())
}
