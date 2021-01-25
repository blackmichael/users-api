package users.api

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import java.io.Closeable
import kotlin.system.exitProcess
import users.api.domain.service.UsersService
import users.api.postgres.PostgresService
import users.api.server.Server

/**
 * Main application responsible for starting up the HTTP server and orchestrating dependencies.
 *
 * The lack of dependency injection is intentional. There are a few reasons for omitting it:
 *  - Reduced code complexity
 *  - Improved readability
 *  - Slightly improved performance
 */
class Application : Closeable {
    private val config = ConfigFactory.load()

    val postgresService = PostgresService(config.extract("postgres"))
    private val usersService = UsersService(postgresService)
    val server = Server(config.extract("http.server"), usersService)

    fun start() {
        server.start()
    }

    override fun close() {
        server.close()
    }
}

fun main() {
    val app = Application()

    try {
        // shutdown gracefully
        Runtime.getRuntime().addShutdownHook(Thread {
            app.close()
            exitProcess(0)
        })

        // start up the app
        app.start()
    } catch (e: Exception) {
        exitProcess(1)
    }
}
