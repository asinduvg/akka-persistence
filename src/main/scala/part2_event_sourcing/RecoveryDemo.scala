package part2_event_sourcing

import akka.actor.{ ActorLogging, ActorSystem, Props }
import akka.persistence.{ PersistentActor, RecoveryCompleted }

object RecoveryDemo extends App {

  case class Command(contents: String)
  case class Event(id: Int, contents: String)

  class RecoveryActor extends PersistentActor with ActorLogging {

    // var latestPersistedEventId = 0

    override def persistenceId: String = "recovery-actor"

    override def receiveCommand: Receive = online(0)

    def online(latestPersistedEventId: Int): Receive = { case Command(contents) =>
      persist(Event(latestPersistedEventId, contents)) { event =>
        log.info(
          s"Successfully persisted $event, recovery is ${if (this.recoveryFinished) ""
            else "NOT"} finished."
        )
        context.become(online(latestPersistedEventId + 1))
      }
    }

    override def receiveRecover: Receive = {
      case Event(id, contents) =>
        //   if (contents.contains("314"))
        //     throw new RuntimeException("I can't take this anymore")

        log.info(
          s"Recovered: $contents, recovery is ${if (this.recoveryFinished) ""
            else "NOT"} finished."
        )
        context.become(
          online(id + 1)
        ) /* This will NOT change the event handler during recovery */
      /* AFTER recovery the "normal" handler will be the result of ALL the stacking of context becomes */
      case RecoveryCompleted =>
        // additinoal initialization
        log.info("I have finished recovering")
    }

    override protected def onRecoveryFailure(
      cause: Throwable,
      event: Option[Any]
    ): Unit = {
      log.error(s"I failed at recovery")
      super.onRecoveryFailure(cause, event)
    }

    // override def recovery: Recovery = Recovery(toSequenceNr = 100)

    // override def recovery: Recovery = Recovery(fromSnapshot = SnapshotSelectionCriteria.Latest)

    // override def recovery: Recovery = Recovery.none

  }

  val system        = ActorSystem("RecoveryDemo")
  val recoveryActor = system.actorOf(Props[RecoveryActor], "recoveryActor")

  /* Stashing commands */
//   for (i <- 1 to 1000) {
//     recoveryActor ! Command(s"command $i")
//   }
  // ALL COMMANDS SENT DURING RECOVERY ARE STASHED

  /* 2
    - Failure during recovery
    - onRecoveryFailure + the actor is STOPPED */

  /* 3
    - Customizing recovery
    - DO NOT persist more events after a customized _incomplete_ recovery */

  /* 4
    - Recovery status or KNOWING when you're done recovery
    - Getting a signal when you're done recovering */

  /* 5
    - stateless actors */

}
