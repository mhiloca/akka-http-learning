package part3_highlevelserver.marshalling_json

import akka.actor.{Actor, ActorLogging}

object GameAreaMap {
  case object GetAllPlayers
  case class GetPlayer(nickname: String)
  case class GetPlayersByClass(characterClass: String)
  case class AddPlayer(player: Player)
  case class RemovePlayer(player: Player)
  case object OperationSuccess
}
class GameAreaMap extends Actor with ActorLogging {
  import GameAreaMap._

  var players: Map[String, Player] = Map()

  override def receive: Receive = {
    case GetAllPlayers =>
      log.info("Getting all players")
      sender() ! players.values.toList

    case GetPlayer(nickname) =>
      log.info(s"Getting player $nickname")
      sender() ! players.get(nickname)

    case GetPlayersByClass(characterClass) =>
      log.info(s"Getting all players by class $characterClass")
      sender() ! players.values.toList.filter(_.characterClass == characterClass)

    case AddPlayer(player) =>
      log.info(s"Trying to add player $player")
      players += player.nickname -> player
      sender() ! OperationSuccess

    case RemovePlayer(player) =>
      log.info(s"Trying to remove player $player")
      players = players - player.nickname
      sender() ! OperationSuccess
  }
}