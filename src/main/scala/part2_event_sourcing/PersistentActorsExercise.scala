package part2_event_sourcing

import akka.persistence.PersistentActor
import akka.actor.{ActorLogging, Props, ActorSystem}

import scala.collection.mutable.Map
import scala.util.Random

object PersistentActorsExercise extends App {

  /* Persistent actor for a voting station
        Keep:
            - the citizens who voted
            - the poll: mapping between a candidate and the number of received votes so far

        The actor must be able to recover its state if it's shut down or restarted
   */

  case class Vote(citizenPID: String, candidate: String)

  class VotingStation extends PersistentActor with ActorLogging {

    var citizensVoted = List[String]()
    var poll = Map[String, Int]()

    override def persistenceId: String = "voting-station"
    override def receiveCommand: Receive = {
      case vote @ Vote(citizenPID, candidate) =>
        val isAlreadyVoted = citizensVoted.filter(_ == citizenPID)
        if (isAlreadyVoted.isEmpty) {
          persist(vote) { e =>
            citizensVoted = citizensVoted :+ e.citizenPID
            poll += (e.candidate -> (poll.getOrElse(e.candidate, 0) + 1))
            println(s"[Vote Recorded]: Currect Votees = $citizensVoted: Votes = $poll")
          }
        } else {
          println("Illegal operation")
        }
    }
    override def receiveRecover: Receive = { case Vote(citizenPID, candidate) =>
      citizensVoted = citizensVoted :+ citizenPID
      poll += (candidate -> (poll.getOrElse(candidate, 0) + 1))
      println(s"[Vote Recovered]: Currect Votees = $citizensVoted: Votes = $poll")
    }
  }

  val system = ActorSystem("PersistentActors")
  val votingStation = system.actorOf(Props[VotingStation], "votingStation")

//   votingStation ! Vote("001", "Asindu")

  val chars = ('0' to '9') ++ ('A' to 'Z')
  val candidates = List("Asindu", "Daniel", "Chamika", "Ashan")

  def getPersonID = (1 to 2).map(_ => chars(Random.nextInt(chars.length))).mkString
  def getCandidate = candidates(Random.nextInt(candidates.length))

  // for {
  //   _ <- 1 to 100000
  //   pID = getPersonID
  //   candidate = getCandidate
  // } yield {
  //   votingStation ! Vote(pID, candidate)
  // }

}
