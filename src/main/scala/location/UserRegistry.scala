package location

//#user-registry-actor
import org.apache.pekko
import pekko.actor.typed.ActorRef
import pekko.actor.typed.Behavior
import pekko.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import scalikejdbc._

//#user-case-classes
final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: immutable.Seq[User])
//#user-case-classes

object UserRegistry {
  // actor protocol
  sealed trait Command
  final case class GetUsers(replyTo: ActorRef[Users]) extends Command
  final case class CreateUser(user: User, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetUser(name: String, replyTo: ActorRef[GetUserResponse]) extends Command
  final case class DeleteUser(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(users: Set[User]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        val users = DB localTx { implicit session =>
          sql"SELECT name, age, countryOfResidence FROM users"
            .map(rs => User(rs.string("name"), rs.int("age"), rs.string("countryOfResidence")))
            .list
            .apply()
        }
        replyTo ! Users(users.toSeq)
        Behaviors.same
      case CreateUser(user, replyTo) =>
        DB localTx { implicit session =>
          sql"""
          INSERT INTO users (name, age, countryOfResidence)
          VALUES (${user.name}, ${user.age}, ${user.countryOfResidence})
        """.update.apply()
        }
        replyTo ! ActionPerformed(s"User ${user.name} created.")
        Behaviors.same
        replyTo ! ActionPerformed(s"User ${user.name} created.")
        registry(users + user)
      case GetUser(name, replyTo) =>
        val maybeUser = DB localTx  { implicit session =>
          sql"SELECT name, age, countryOfResidence FROM users WHERE name = $name"
            .map(rs => User(rs.string("name"), rs.int("age"), rs.string("countryOfResidence")))
            .single
            .apply()
        }
        replyTo ! GetUserResponse(maybeUser)
        Behaviors.same

      case DeleteUser(name, replyTo) =>
        DB localTx { implicit session =>
          sql"DELETE FROM users WHERE name = $name".update.apply()
        }
        replyTo ! ActionPerformed(s"User $name deleted.")
        Behaviors.same

    }
}
//#user-registry-actor
