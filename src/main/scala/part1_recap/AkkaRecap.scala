package part1_recap

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, PoisonPill, Props, Stash, SupervisorStrategy}
import akka.util.Timeout

import scala.util.Success

object AkkaRecap extends App {

  val system: ActorSystem = ActorSystem("AkkaRecap")

  class SimpleActor extends Actor with ActorLogging with Stash {
    override def receive: Receive = {
      case "create child" =>
        val child = context.actorOf(Props[SimpleActor], "child")
        child ! "Hello, my child"
      case "stash" =>
          stash()
      case "change handler now" =>
        unstashAll()
        context.become(anotherHandler)
      case "change" => context.become(anotherHandler)
      case message => log.info(s"I received: $message")
    }

    def anotherHandler: Receive = {
      case message => log.info(s"Another receive handler: $message")
    }

    override def preStart(): Unit = {
      log.info("I am starting")
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: RuntimeException => Restart
      case _ => Stop
    }
  }

  /* actor encapsulation
  * #1: you can only instantiate an actor through the actor system
  * #2: send messages
  *   - messages are sent asynchronously
  *   - many actors can share few dozen threads
  *   - each message is processed/ handled ATOMICALLY
  *   - no need for locks
  * */

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "Hello from Mhiloca!"

  // changing actor behaviour + stashing
  // actors can spawn other actors
  // guardians: /system, /user, / = root guardian

  // actors have a defined lifecycle: they can be started, stopped, suspended, resumed, restarted

  // stopping actors = context.stop
  /*simpleActor ! PoisonPill*/ // thes  e type of messages are handled in another mailbox

  // logging
  // supervision = how parent actor will handle a child failure

  // configure Akka infrastructure: dispatchers, routers, mailboxes

  // schedulers
  import scala.concurrent.duration._
  import scala.language.postfixOps
  import system.dispatcher

  system.scheduler.scheduleOnce(2 seconds) {
    simpleActor ! "Delayed happy birthday"
  }

  import akka.pattern.{ask, pipe}
  implicit val timeout = Timeout(3 seconds)
  // akka pattern including FSM + ask pattern
  val future = simpleActor ? "question"

  // the pipe pattern
  val anotherSimpleActor = system.actorOf(Props[SimpleActor], "anotherSimpleActor")
  future.mapTo[String].pipeTo(anotherSimpleActor)
}
