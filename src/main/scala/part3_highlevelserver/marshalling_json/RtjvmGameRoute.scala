package part3_highlevelserver.marshalling_json

import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration._



object RtjvmGameRoute extends PlayerJsonProtocol {

  implicit val timeout: Timeout = Timeout(2 seconds)

  import MarshallingJSON._
  import GameAreaMap._
  import system.dispatcher

  /*
   - GET /api/player - returns all the players in the map, as JSON
   - GET /api/player/(nickname) - returns the player with the given nickname
   - GET /api/player?nickname=X, does the same
   - GET /api/player/class/(charClass) - returns all the players with the given characterClass
   - POST /api/player with JSON payload, adds the player to the map
   - DELETE /api/player with JSON payload, removes the player from the map
  */



  val rtjvmGameRouteSkel =
    pathPrefix("api" / "player") {
      get {
        path("class" / Segment) { characterClass =>
          val playersByClassFuture =  (rtjvGameMap ? GetPlayersByClass(characterClass)).mapTo[List[Player]]
          complete(playersByClassFuture)
        } ~
        (path(Segment) | parameter('nickname)) { nickname =>
          val playerOptionFuture = (rtjvGameMap ? GetPlayer(nickname)).mapTo[Option[Player]]
          complete(playerOptionFuture)
        } ~
        pathEndOrSingleSlash {
          val allPlayersFuture = (rtjvGameMap ? GetAllPlayers).mapTo[List[Player]]
          complete(allPlayersFuture)
        }
      } ~
      (post & pathEndOrSingleSlash & entity(as[Player]) ) { player =>
        complete((rtjvGameMap ? AddPlayer(player)).map(_ => StatusCodes.OK))
      } ~
      (delete & pathEndOrSingleSlash & entity(as[Player])) { player =>
        complete((rtjvGameMap ? RemovePlayer(player)).map(_ => StatusCodes.OK))
      }
    }

}
