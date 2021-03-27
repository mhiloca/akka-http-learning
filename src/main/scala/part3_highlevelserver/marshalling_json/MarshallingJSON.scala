package part3_highlevelserver.marshalling_json

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object MarshallingJSON extends App {

  implicit val system: ActorSystem = ActorSystem("MarshallingJSON")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import GameAreaMap._
  import RtjvmGameRoute._

  val rtjvGameMap = system.actorOf(Props[GameAreaMap], "rtjvGameAreaMap")
  val playersList = List(
    Player("martin_killz_u", "Warrior", 70),
    Player("rollandbraveheart007", "Elf", 67),
    Player("daniel_rock03", "Wizard", 30)
  )

  playersList foreach { player => rtjvGameMap ! AddPlayer(player)}

  Http().bindAndHandle(rtjvmGameRouteSkel, "localhost", 8080)

}
