package users.api

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import java.io.Closeable
import kotlin.system.exitProcess
import users.api.server.Server

class Application() : Closeable {

    val config = ConfigFactory.load()
    val server = Server(config.extract("http.server"))

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
        Runtime.getRuntime().addShutdownHook(Thread {
            app.close()
            exitProcess(0)
        })

        app.start()
    } catch (e: Exception) {
        exitProcess(1)
    }
}
