package users.api.test

import java.io.Closeable
import java.net.ServerSocket
import users.api.Application

/**
 * Spins up the API HTTP server for functional testing. Provides utility classes for making HTTP requests against the
 * server and cleaning up PostgreSQL data.
 */
class TestHarness : Closeable {

    private val application: Application
        get() {
            // this needs to be evaluated before the Application object is created
            // so that the system property is set for the random open server port
            ServerSocket(0)
                .use {
                    it.reuseAddress = true
                    it.localPort
                }
                .also {
                    System.setProperty("http.server.port", "$it")
                }

            return Application()
        }

    private val baseUri by lazy {
        "http://localhost:${application.server.config.port}"
    }

    val client by lazy {
        TestClient(baseUri)
    }

    val postgresHelper by lazy {
        PostgresHelper(application.postgresService.context)
    }

    init {
        application.start()
    }

    override fun close() {
        application.close()
        client.close()
    }
}
