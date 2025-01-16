package part2_event_sourcing

import akka.persistence.PersistentActor
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem

object PersistAsyncDemo extends App {

  case class Command(contents: String)
  case class Event(contents: String)

  object CriticalStreamProcessor {
    def props(eventAggregator: ActorRef) = Props(
      new CriticalStreamProcessor(eventAggregator)
    )
  }

  class CriticalStreamProcessor(eventAggregator: ActorRef)
      extends PersistentActor
      with ActorLogging {
    override def persistenceId: String = "critical-stream-processor"

    override def receiveCommand: Receive = { case Command(contents) =>
      eventAggregator ! s"Processing $contents"
      // mutate
      persistAsync(Event(contents)) /*  TIME GAP */ { e =>
        eventAggregator ! e
        // mutate
      }

      // some actual computation
      val processedContents = contents + "_processed"
      persistAsync(Event(processedContents)) /*  TIME GAP */ { e =>
        eventAggregator ! e
      }
    }

    override def receiveRecover: Receive = { case message =>
      log.info(s"Recovered: $message")
    }
  }

  class EventAggregator extends Actor with ActorLogging {
    override def receive: Receive = { case message =>
      log.info(s"$message")
    }
  }

  val system = ActorSystem("PersistAsyncDemo")
  val eventAggregator =
    system.actorOf(Props[EventAggregator], "eventAggregator")
  val streamProcessor = system.actorOf(
    CriticalStreamProcessor.props(eventAggregator),
    "streamProcessor"
  )

  streamProcessor ! Command("command1")
  streamProcessor ! Command("command2")

  /*
    persistAsync vs persist
        - perf: high-throughput environments

    persit vs persistAsync
        - ordering guarantees
   */

}
