package part3_highlevelserver
import akka.http.javadsl.server.MethodRejection
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._

case class Book(id: Int, author: String, title: String)

trait BookJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val bookFormat = jsonFormat3(Book)
}
class RouteDSLSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with BookJsonProtocol {

  import RouteDSLSpec._
  "a digital library backend" should {
    "return all the books in the library" in {
      // send an HTTP request through an endpoint that you want to test
      // inspect the response
      Get("/api/book") ~> libraryRoute ~> check {
        //assertions
        status shouldBe StatusCodes.OK
        entityAs[List[Book]] shouldBe books
      }
    }

    "return a book by hitting the query parameter endpoint" in {
      Get("/api/book?id=2") ~> libraryRoute ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Option[Book]] shouldBe Some(Book(2, "JRR Tolkien", "The Lord of the Rings"))
      }
    }

    "return a book by calling the endpoint with the id in the path" in {
      Get("/api/book/2") ~> libraryRoute ~> check {
        response.status shouldBe StatusCodes.OK

        val strictEntityFuture = response.entity.toStrict(1 second)
        val strictEntity = Await.result(strictEntityFuture, 1 second)
        /*
          Await blocks the code for this future and if the timeout
          passes it throws an exception
         */

        strictEntity.contentType shouldBe ContentTypes.`application/json`

        val book = strictEntity.data.utf8String.parseJson.convertTo[Option[Book]]
        book shouldBe Some(Book(2, "JRR Tolkien", "The Lord of the Rings"))
      }
    }

    "insert a book into the 'database'" in {
      val newBook = Book(5, "Steven Pressfield", "The War of Art")
      Post("/api/book", newBook) ~> libraryRoute ~> check {
        status shouldBe StatusCodes.OK
//        assert(books.contains(newBook))
        books should contain(newBook)
      }
    }

    "not accept other methods than POST and GET" in {
      Delete("/api/book") ~> libraryRoute ~> check {
//        rejections.length should be > 0

        val methodRejections = rejections.collect {
          case rejection: MethodRejection => rejection
        }

        methodRejections.length shouldBe 2
      }
    }

    "return all the author's books by calling the endpoint with the author in the path" in {
      Get("/api/book/author/JRR%20Tolkien") ~> libraryRoute ~> check {
        status shouldBe StatusCodes.OK

        entityAs[List[Book]] shouldBe List(
          Book(2, "JRR Tolkien", "The Lord of the Rings"),
          Book(5, "JRR Tolkien", "The Hobbit")
        )
      }
    }
  }

}

object RouteDSLSpec extends BookJsonProtocol {

  //code under test
  var books = List(
    Book(1, "Harper Lee", "To Kill a Mockingbird"),
    Book(2, "JRR Tolkien", "The Lord of the Rings"),
    Book(3, "GRR Martin", "A Song of Ice and Fire"),
    Book(4, "TonyRobins", "Awaken the Giant Within"),
    Book(5, "JRR Tolkien", "The Hobbit")
  )

  /*
    - GET /api/book - returns all the books in the library
    - GET /api/book/X - return a single book with id X
    - GET /api/book?id=X - same
    - POST /api/book - adds a new book the library
    - GET  /api/book/author/X - returns all the books from the actor X
   */

  val libraryRoute =
    pathPrefix("api" / "book") {
      (path("author" / Segment) & get) { author =>
        complete(books.filter(_.author == author))
      } ~
      get {

        (path(IntNumber) | parameter('id.as[Int])) { id =>
          complete(books.find(_.id == id))
        } ~
          pathEndOrSingleSlash {
            complete(books)
          }
      } ~
        post{
          entity(as[Book]) {book =>
            books = books :+ book
            complete(StatusCodes.OK)
          } ~
            complete(StatusCodes.BadRequest)
        }
    }


}

