package part2_event_sourcing

import akka.persistence.PersistentActor
import akka.actor.{ ActorLogging, ActorSystem, Props }

import scala.util.Random
import scala.collection.mutable

object PersistentActorsExercise extends App {

  /* Persistent actor for a voting station
        Keep:
            - the citizens who voted
            - the poll: mapping between a candidate and the number of received votes so far

        The actor must be able to recover its state if it's shut down or restarted
   */

  case class Vote(citizenPID: String, candidate: String)

  class VotingStation extends PersistentActor with ActorLogging {

    val citizensVoted = new mutable.HashSet[String]()
    val poll          = new mutable.HashMap[String, Int]()

    override def persistenceId: String = "voting-station"
    override def receiveCommand: Receive = {
      case vote @ Vote(citizenPID, candidate) =>
        if (!citizensVoted.contains(citizenPID)) {
          persist(vote) { e => // COMMAND sourcing
            handleInternalStateChange(e.citizenPID, e.candidate)
            log.info(s"[Vote Recorded]: $vote")
          }
        } else {
          log.warning(s"Citizen $citizenPID's trying to vote again")
        }
      case "print" =>
        log.info(s"\n[Voters]: $citizensVoted\n[Poll]: $poll")
    }
    override def receiveRecover: Receive = { case vote @ Vote(citizenPID, candidate) =>
      handleInternalStateChange(citizenPID, candidate)
      log.info(s"[Vote Recovered]: $vote")
    }

    private def handleInternalStateChange(citizenPID: String, candidate: String): Unit = {
      citizensVoted.add(citizenPID)
      poll.put(candidate, poll.getOrElse(candidate, 0) + 1)
    }
  }

  val system        = ActorSystem("PersistentActors")
  val votingStation = system.actorOf(Props[VotingStation], "votingStation")

  // val chars = ('0' to '9') ++ ('A' to 'Z')
  // val candidates = List("Asindu", "Daniel", "Chamika", "Ashan")

  // def getPersonID =
  //   (1 to 2).map(_ => chars(Random.nextInt(chars.length))).mkString
  // def getCandidate = candidates(Random.nextInt(candidates.length))

  // for {
  //   _ <- 1 to 100000
  //   pID = getPersonID
  //   candidate = getCandidate
  // } yield {
  //   votingStation ! Vote(pID, candidate)
  // }

  // Danis
  val votesMap = Map[String, String](
    "Alice"   -> "Martin",
    "Bob"     -> "Roland",
    "Charlie" -> "Martin",
    "David"   -> "Jonas",
    "Daniel"  -> "Martin"
  )

  // votesMap.keys.foreach { citizen =>
  //   votingStation ! Vote(citizen, votesMap(citizen))
  // }

  votingStation ! Vote("Daniel", "Daniel")
  votingStation ! "print"
  // Danis

}
