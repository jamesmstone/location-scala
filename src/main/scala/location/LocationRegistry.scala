package location

import org.apache.pekko
import pekko.actor.typed.ActorRef
import pekko.actor.typed.Behavior
import pekko.actor.typed.scaladsl.Behaviors
import scalikejdbc._

final case class Location(
                           _id: String,
                           acc: Int,
                           alt: Int,
                           batt: Int,
                           bs: Int,
                           conn: String,
                           created_at: Long,
                           lat: Double,
                           lon: Double,
                           m: Int,
                           t: String,
                           tid: String,
                           topic: String,
                           tst: Long,
                           vac: Int,
                           vel: Int
                         )

object LocationRegistry {

  sealed trait Command
  final case class SaveLocation(location: Location, replyTo: ActorRef[ActionPerformed]) extends Command

  def apply(): Behavior[Command] = registry()

  private def registry(): Behavior[Command] =
    Behaviors.receiveMessage {
      case SaveLocation(location, replyTo) =>
        saveLocationToDb(location)
        replyTo ! ActionPerformed(s"Location ${location._id} saved.")
        Behaviors.same
    }

  private def saveLocationToDb(location: Location): Unit = {
    DB localTx { implicit session =>
      sql"""
        INSERT INTO locations (_id, acc, alt, batt, bs, conn, created_at, lat, lon, m, t, tid, topic, tst, vac, vel)
        VALUES (${location._id}, ${location.acc}, ${location.alt}, ${location.batt}, ${location.bs}, ${location.conn},
        ${location.created_at}, ${location.lat}, ${location.lon}, ${location.m}, ${location.t}, ${location.tid},
        ${location.topic}, ${location.tst}, ${location.vac}, ${location.vel})
      """.update.apply()
    }
  }
}
