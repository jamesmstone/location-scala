package location

import com.typesafe.config.ConfigFactory
import org.apache.pekko
import pekko.actor.typed.ActorSystem
import pekko.actor.typed.scaladsl.Behaviors
import pekko.http.scaladsl.Http
import pekko.http.scaladsl.server.Route

import scala.util.Failure
import scala.util.Success
import scalikejdbc.*
import scalikejdbc.config.*

//#main-class
object hello {
  //#start-http-server
  private def startHttpServer(routes: Route, host: String, port :Int)(implicit system: ActorSystem[_]): Unit = {
    // Pekko HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt(host, port).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  //#start-http-server
  def main(args: Array[String]): Unit = {
    //#server-bootstrapping

    DBs.setupAll()
    // Create the users table if not exists
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE IF NOT EXISTS users (
          name TEXT PRIMARY KEY,
          age INTEGER,
          countryOfResidence TEXT
        )
      """.execute.apply()
    }
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE IF NOT EXISTS locations (
          _id TEXT PRIMARY KEY,
          acc INTEGER,
          alt INTEGER,
          batt INTEGER,
          bs INTEGER,
          conn TEXT,
          created_at INTEGER,
          lat REAL,
          lon REAL,
          m INTEGER,
          t TEXT,
          tid TEXT,
          topic TEXT,
          tst INTEGER,
          vac INTEGER,
          vel INTEGER
        )
      """.execute.apply()
    }
    val config = ConfigFactory.load()
    val port = config.getInt("my-app.routes.port")
    val host = config.getString("my-app.routes.host")

    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
      val locationRegistryActor = context.spawn(LocationRegistry(), "LocationRegistryActor")
      context.watch(userRegistryActor)
      context.watch(locationRegistryActor)

      val routes = new AllRoutes(userRegistryActor, locationRegistryActor)(context.system)
      startHttpServer(routes.allRoutes, host, port)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloPekkoHttpServer")
    //#server-bootstrapping
  }
}
//#main-class
